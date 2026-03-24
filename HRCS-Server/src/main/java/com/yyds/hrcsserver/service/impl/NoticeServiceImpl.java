package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcscommon.constants.ErrorEnum;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcscommon.result.UploadResult;
import com.yyds.hrcscommon.utils.AliOssUtil;
import com.yyds.hrcscommon.utils.UserContext;

import com.yyds.hrcspojo.notice.DaliyStateCountNoticeDTO;
import com.yyds.hrcspojo.entity.Notice;

import com.yyds.hrcspojo.entity.NoticeReceiver;
import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.mapper.NoticeMapper;
import com.yyds.hrcsserver.repository.NoticeRepository;
import com.yyds.hrcsserver.repository.UserRepository;
import com.yyds.hrcsserver.service.NoticeReceiverService;
import com.yyds.hrcsserver.service.NoticeService;

import com.yyds.hrcscommon.webSocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
* @author 21641
* @description 针对表【notice(公告表)】的数据库操作Service实现
* @createDate 2025-12-17 20:22:38
*/
@Slf4j
@Service
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice>
    implements NoticeService {

    private final AliOssUtil aliOssUtil;
    private final NoticeMapper noticeMapper;
    private final UserRepository userRepository;
    private final NoticeReceiverService noticeReceiverService;
    private final WebSocketService webSocketService;
    private final NoticeRepository noticeRepository;

    public NoticeServiceImpl(AliOssUtil aliOssUtil, NoticeMapper noticeMapper, UserRepository userRepository, NoticeReceiverService noticeReceiverService, WebSocketService webSocketService, WebSocketService webSocketService1, NoticeRepository noticeRepository) {
        this.aliOssUtil = aliOssUtil;
        this.noticeMapper = noticeMapper;
        this.userRepository = userRepository;
        this.noticeReceiverService = noticeReceiverService;
        this.webSocketService = webSocketService1;

        this.noticeRepository = noticeRepository;
    }

    /**
     * 添加公告
     * @param notice
     * @param file
     * @return
     * @throws BusinessException
     */
    /**
     * 添加公告（草稿）
     */
    @Override
    public boolean saveWithFile(Notice notice, MultipartFile file) throws BusinessException {
        // 1. 上传文件到OSS
        if (file != null && !file.isEmpty()) {
            AliOssUtil result = aliOssUtil;
            UploadResult uploadResult = result.uploadFile(file, "notice");
            notice.setFileUrl(uploadResult.getUrl());
            notice.setOssObjectName(uploadResult.getObjectName());
        }

        // 2. 设置基础信息
        Long currentUserId = Long.valueOf(UserContext.getCurrentUserId());
        User user = userRepository.getById(currentUserId);
        if (user == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
        }

        notice.setCreateBy(user.getName());
        notice.setCreateTime(new Date());
        notice.setUpdateBy(user.getName());
        notice.setUpdateTime(new Date());
        notice.setDeleted(0);
        notice.setStatus(1); // 草稿状态

        return noticeMapper.insert(notice) > 0;
    }

    /**
     * 发布公告（核心：自动分配接收人）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishNotice(Long id) throws BusinessException {

        Notice notice = noticeRepository.getById(id);
        // 2. 更新公告状态为已发布
        notice.setStatus(2);
        notice.setPublishTime(new Date());
        notice.setUpdateTime(new Date());
        noticeMapper.updateById(notice);

        // 3. 查询并分配接收人
        List<Long> receiverUserIds = this.queryReceivers(notice, notice.getId());
        if (!receiverUserIds.isEmpty()) {
            this.batchInsertReceivers(notice.getId(), receiverUserIds);
        }

        // 4. 重要/紧急通知异步推送
        if (notice.getPriority() == 2) {
            this.sendUrgentNotificationAsync(notice, receiverUserIds);
        }

        log.info("公告发布成功，ID:{}, 接收人数量:{}", notice.getId(), receiverUserIds.size());
        return true;
    }

    /**
     * 查询接收人列表
     */
    private List<Long> queryReceivers(Notice notice, Long deptId) {
        List<Long> receiverUserIds = new ArrayList<>();

        if (notice.getType() == 2) { // 部门公告
            if (deptId == null) {
                throw new BusinessException(ErrorEnum.DEPT_ID_IS_NULL);
            }
            // 查询部门成员
            List<User> deptUsers = userRepository.selectList(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getDepartmentId, deptId)
            );
            receiverUserIds = deptUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());

        } else { // 系统公告/紧急通知
            // 查询所有有效用户
            List<User> allUsers = userRepository.selectList(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getStatus, 1)
            );
            receiverUserIds = allUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
        }

        return receiverUserIds;
    }

    /**
     * 批量插入接收人记录
     */
    private void batchInsertReceivers(Long noticeId, List<Long> userIds) {
        List<NoticeReceiver> receivers = userIds.stream()
                .map(userId -> {
                    NoticeReceiver receiver = new NoticeReceiver();
                    receiver.setNoticeId(noticeId);
                    receiver.setUserId(userId);
                    receiver.setIsRead(0);
                    receiver.setCreateTime(new Date());
                    return receiver;
                })
                .collect(Collectors.toList());

        // MyBatis-Plus批量插入
        noticeReceiverService.saveBatch(receivers);
    }

    /**
     * 异步发送重要通知（WebSocket/站内信/邮件）
     */
    @Async
    protected void sendUrgentNotificationAsync(Notice notice, List<Long> userIds) {
        try {
            String message = String.format("【%s】%s",
                    notice.getPriority() == 3 ? "紧急通知" : "重要通知",
                    notice.getTitle()
            );

            // WebSocket推送示例
            webSocketService.sendToUsers(userIds, message);

            // 站内信（可选）
            // messageService.sendSystemMessage(userIds, notice.getId(), message);

        } catch (Exception e) {
            log.error("重要通知推送失败", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithFile(Long id) throws BusinessException {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        if(notice.getStatus()!=3){
            throw new BusinessException(ErrorEnum.CAN_NOT_DELETE_NOTICE_USING);
        }
        // 删除OSS文件
        if (StringUtils.isNotBlank(notice.getOssObjectName())) {
            aliOssUtil.delete(notice.getOssObjectName());
        }

        // 逻辑删除
        notice.setDeleted(1);
        noticeRepository.updateById( notice);
        return noticeRepository.removeById( notice)? true : false ;
    }

    //根据关键字分页查询
    @Override
    public IPage<Notice> listNotice(Integer pageNum, Integer pageSize, String keyword) {
        return noticeRepository.listNotice(pageNum, pageSize, keyword);

    }

    @Override
    public boolean updateNotice(Long id) {
        Notice notice = noticeRepository.getById(id);
        notice.setStatus(3);
        return noticeMapper.updateById(notice) > 0;
    }

    @Override
    public IPage<Notice> getNoticeByDepartmentId(Integer pageNum, Integer pageSize, Long id) {
        return noticeRepository.getNoticeByDepartmentId(pageNum, pageSize, id);
    }

    @Override
    public Notice getDetail(Long id) {
        return noticeRepository.getById(id);
    }

    @Override
    public List<DaliyStateCountNoticeDTO> getDaliyStateCountNoticeDTO() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);
        List<Map<String, Object>> dailyCounts = noticeRepository.getDailyUserCountNotice(startDate, today);
        // 3. 转换为Map结构，key为日期字符串，value为数量
        Map<String, Long> countMap = dailyCounts.stream()
                .collect(Collectors.toMap(
                        map -> map.get("date").toString(), // 日期格式: 2024-01-15
                        map -> (Long) map.get("count"),
                        (oldVal, newVal) -> newVal // 处理重复key的情况
                ));

        // 4. 生成8天的完整数据（包含无数据的日期）
        List<DaliyStateCountNoticeDTO> result = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.toString();

            DaliyStateCountNoticeDTO dto = new DaliyStateCountNoticeDTO();
            dto.setDate(dateStr);
            dto.setUserCount(countMap.getOrDefault(dateStr, 0L)); // 无数据则默认为0

            result.add(dto);
        }

        return result;
    }


}




