package com.yyds.hrcsserver.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcscommon.client.MailClient;
import com.yyds.hrcscommon.client.PasswordClient;
import com.yyds.hrcscommon.constants.ErrorEnum;
import com.yyds.hrcscommon.constants.TimeOutEnum;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcscommon.utils.AliOssUtil;
import com.yyds.hrcscommon.utils.JwtUtils;
import com.yyds.hrcscommon.utils.ThrowUtils;
import com.yyds.hrcspojo.data.user.CountINFO;
import com.yyds.hrcspojo.data.user.DailyStatsUserDTO;
import com.yyds.hrcspojo.data.user.login.LoginByCodeDTO;
import com.yyds.hrcspojo.data.user.login.LoginDTO;
import com.yyds.hrcspojo.data.user.login.LoginVO;
import com.yyds.hrcspojo.data.user.RegisterDTO;

import com.yyds.hrcspojo.data.user.login.UserInfoVO;
import com.yyds.hrcspojo.data.user.update.UpdateDTO;
import com.yyds.hrcspojo.entity.Department;
import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.mapper.UserMapper;
import com.yyds.hrcsserver.repository.DepartmentRepository;
import com.yyds.hrcsserver.repository.NoticeRepository;
import com.yyds.hrcsserver.repository.UserRepository;
import com.yyds.hrcsserver.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * @author 21641
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-06-10 15:48:46
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final StringRedisTemplate redisTemplate;

    private final UserRepository userRepository;

    private final MailClient mailClient;

    private final PasswordClient passwordClient;
    private final AliOssUtil aliOssUtil;
    private final DepartmentRepository departmentRepository;
    private final NoticeRepository noticeRepository;

    public UserServiceImpl(StringRedisTemplate redisTemplate,
                           UserRepository userRepository,
                           MailClient mailClient,
                           PasswordClient passwordClient,
                           AliOssUtil aliOssUtil,
                           DepartmentRepository departmentRepository, NoticeRepository noticeRepository){
        this.redisTemplate = redisTemplate;
        this.userRepository = userRepository;
        this.mailClient = mailClient;
        this.aliOssUtil = aliOssUtil;
        this.passwordClient = passwordClient;
        this.departmentRepository = departmentRepository;
        this.noticeRepository = noticeRepository;
    }

    @Override
    public void sendCode(String email) {
        String code = UUID.randomUUID().toString().substring(0, 6);
        mailClient.sendMail(email, code);
        redisTemplate.opsForValue().set(email, code, TimeOutEnum.TOKEN_TIME_OUT.getTimeOut(), TimeUnit.MINUTES);
    }

    /**
     * 注册
     *
     * @param registerDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 添加事务
    public void register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO);
        String email = registerDTO.getEmail();

        // 1. 检查邮箱是否已注册
        userRepository.selectOptByEmail(email).ifPresent(user -> {
            throw new BusinessException(ErrorEnum.REGISTER_ERROR);
        });

        // 2. 验证验证码,如果验证码错误提示重新输入

        String codeRedis = redisTemplate.opsForValue().get(email);
        String codeInput = registerDTO.getCode(); // 从DTO获取用户输入的code
        ThrowUtils.throwIf(codeRedis == null
                || !codeRedis.equals(codeInput), ErrorEnum.CODE_ERROR);

        // 3. 删除已使用的验证码（防止重复使用）
        redisTemplate.delete(email);
        // 4. 加密密码并保存
        String password = passwordClient.hashPassword(registerDTO.getPassword());
        User user = getUser(registerDTO, password);
        userRepository.save(user);
    }

    private User getUser(RegisterDTO registerDTO, String password) {
        User user = new User();
        BeanUtil.copyProperties(registerDTO, user);
        user.setPassword(password);
        return user;
    }

    /**
     * 邮箱密码登录
     *
     * @param loginDTO
     * @return
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        User user = userRepository.selectOptByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorEnum.NOT_FOUND_USER));

        if (!passwordClient.verifyPassword(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR);
        }
        return new LoginVO(user, JwtUtils.generate(user.getId().toString()));
    }

    /**
     * 验证码登录
     *
     * @param loginByCodeDTO
     * @return
     */
    @Override
    public LoginVO loginByCode(LoginByCodeDTO loginByCodeDTO) {
        User user = userRepository.selectOptByEmail(loginByCodeDTO.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorEnum.NOT_FOUND_USER));
        if (Objects.isNull(user)) {
            throw new BusinessException("用户不存在,请先注册");
        }
        int role = user.getRole();
        if (role != loginByCodeDTO.getRole()) {
            throw new BusinessException(ErrorEnum.ROLE_IS_ERROR);
        }
        if (!redisTemplate.opsForValue().get(loginByCodeDTO.getEmail()).equals(loginByCodeDTO.getCode())) {
            throw new BusinessException(ErrorEnum.LOGIN_ERROR);
        }
        return new LoginVO(user, JwtUtils.generate(user.getId().toString()));
    }



    @Override
    public int getRole(String email) {
        User user = userRepository.selectOptByEmail(email).orElseThrow(() -> new BusinessException(ErrorEnum.NOT_FOUND_USER));
        return user.getRole();
    }

    // Service
    @Override
    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void updateUserInfo(UpdateDTO updateDTO) {
        log.info("【更新用户】开始，用户ID: {}", updateDTO.getId());

        // 1. 查询并锁定原用户记录（防止并发）
        User dbUser = userRepository.selectForUpdate(updateDTO.getId());
        if (dbUser == null) {
            throw new BusinessException(ErrorEnum.NOT_FOUND_USER);
        }
        log.debug("【更新用户】原始数据: {}", dbUser);

        // 2. 检查唯一性（带排他锁，修复空字符串问题）
        checkDuplicateWithLock(updateDTO, dbUser);

        // 3. 更新非空字段（包括空字符串处理）
        updateNonNullFields(updateDTO, dbUser);

        // 4. 执行更新
        boolean success = userRepository.updateById(dbUser);
        if (!success) {
            throw new BusinessException(ErrorEnum.UPDATE_FAILED);
        }
        log.info("【更新用户】成功，用户ID: {}", updateDTO.getId());
    }

    /**
     * 使用排他锁的重复性检查（修复空值和并发问题）
     */
    private void checkDuplicateWithLock(UpdateDTO updateDTO, User dbUser) {
        // 检查手机号：排除null和空字符串
        if (isNotBlank(updateDTO.getPhone()) && !updateDTO.getPhone().equals(dbUser.getPhone())) {
            if (userRepository.existsByPhone(updateDTO.getPhone())) {
                throw new BusinessException(ErrorEnum.PHONE_EXISTS);
            }
        }

        // 检查用户名
        if (isNotBlank(updateDTO.getUserName()) && !updateDTO.getUserName().equals(dbUser.getUserName())) {
            if (userRepository.existsByUserName(updateDTO.getUserName())) {
                throw new BusinessException(ErrorEnum.USER_NAME_EXISTS);
            }
        }

        // 检查身份证号
        if (isNotBlank(updateDTO.getIdCard()) && !updateDTO.getIdCard().equals(dbUser.getIdCard())) {
            if (userRepository.existsByIdCard(updateDTO.getIdCard())) {
                throw new BusinessException(ErrorEnum.ID_CARD_EXISTS);
            }
        }

        // 检查银行卡号
        if (isNotBlank(updateDTO.getBankCard()) && !updateDTO.getBankCard().equals(dbUser.getBankCard())) {
            if (userRepository.existsByBankCard(updateDTO.getBankCard())) {
                throw new BusinessException(ErrorEnum.BANK_CARD_EXISTS);
            }
        }
    }

    /**
     * 更新非空字段（只更新有值的字段）
     */
    private void updateNonNullFields(UpdateDTO src, User dest) {
        if (src.getName() != null) dest.setName(src.getName());
        if (src.getUserName() != null) dest.setUserName(src.getUserName());
        if (src.getPhone() != null) dest.setPhone(src.getPhone());
        if (src.getIdCard() != null) dest.setIdCard(src.getIdCard());
        if (src.getBankCard() != null) dest.setBankCard(src.getBankCard());
        if (src.getAddress() != null) dest.setAddress(src.getAddress());
        if (src.getRole() != null) dest.setRole(src.getRole());
        if (src.getPost() != null) dest.setPost(src.getPost());
    }

    /**
     * 空字符串判断工具方法（不使用Apache Lang3）
     */
    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

    private boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }


    @Override
    public User getUser(String name) {
        return userRepository.selectOptByName(name).orElse(null);
    }
    /**
     * 获取所有用户分页，根据name模糊查询并且按照createTime降序排序
     */
    @Override
    public List<User> getAllUser(Integer pageNum, Integer pageSize,String name) {
        //获取所有用户分页，根据name模糊查询并且按照createTime降序排序
        return userRepository.selectAllUser(pageNum, pageSize, name);
    }

    @Override
    public User getCurrentUserInfo(String id) {
        return userRepository.getById(id);
    }

    @Override
    public void updateAvatar(String id, String avatar) {
        // 1. 查询用户
        User user = userRepository.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorEnum.NOT_FOUND_USER);
        }

        // 2. 获取旧头像URL（用于后续删除）
        String oldAvatarUrl = user.getAvatar();

        // 3. 更新头像字段
        user.setAvatar(avatar);
        boolean success = userRepository.updateById(user);

        if (!success) {
            throw new BusinessException(ErrorEnum.UPDATE_FAILED);
        }

        // 4. 【可选】删除OSS中的旧头像（推荐）
        if (StringUtils.hasText(oldAvatarUrl)) {
            deleteOldAvatar(oldAvatarUrl);
        }
    }

    @Override
    public List<User> getAnyUser() {
        return userRepository.getAnyUser();
    }

    @Override
    public UserInfoVO getUserInfo(String id) {
        User user = userRepository.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorEnum.USER_NOT_FOUND);
        }

        // 查询部门（添加空值检查）
        Department department = departmentRepository.getById(user.getDepartmentId());

        // ✅ 防御性编程：判断部门是否存在
        String deptName = null;
        if (department != null) {
            deptName = department.getDepartmentName();
        } else {
            log.warn("【用户信息】用户 {} 的部门 {} 不存在", id, user.getDepartmentId());
            deptName = "未分配部门"; // 或设为 null/空字符串
        }

       return    new UserInfoVO().builder()
                .user(user)
                .departmentName(deptName).build();
    }

    @Override
    public CountINFO getCount() {
        CountINFO count = new CountINFO();
        return  count.builder()
                .userCount(userRepository.getUserCount())
                .userCountMonth(userRepository.getUserCountForCurrentMonth())
                .noticeCount(noticeRepository.getNoticeCount())
                .noticeCountMonth(noticeRepository.getNoticeCountForCurrentMonth()).build();
    }

    @Override
    public List<DailyStatsUserDTO> getDailyUserCountInfo() {
        // 1. 计算日期范围（今天和过去7天，共8天）
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(7);

        // 2. 查询数据库中按天分组的用户数量（已修复）
        List<Map<String, Object>> dailyCounts = userRepository.getDailyUserCount(startDate, today);

        // 3. 转换为Map结构（日期 -> 数量）
        Map<String, Long> countMap = dailyCounts.stream()
                .collect(Collectors.toMap(
                        map -> map.get("date").toString(),
                        map -> (Long) map.get("count"),
                        (oldVal, newVal) -> newVal
                ));

        // 4. 生成8天的完整数据（包含无数据的日期）
        List<DailyStatsUserDTO> result = new ArrayList<>();
        for (int i = 0; i <= 7; i++) {
            LocalDate date = startDate.plusDays(i);
            String dateStr = date.toString();

            DailyStatsUserDTO dto = new DailyStatsUserDTO();
            dto.setDate(dateStr);
            dto.setUserCount(countMap.getOrDefault(dateStr, 0L));
            result.add(dto);
        }

        return result;
    }

    /**
     * 使用 lambdaQuery 查询 daily count
     */



    /**
     * 删除OSS中的旧头像
     */
    private void deleteOldAvatar(String avatarUrl) {
        try {
            // 从URL中提取objectName
            // URL格式: https://bucketName.endpoint/avatar/userId/timestamp.jpg
            String prefix = "https://" + aliOssUtil.getBucketName() + "." + aliOssUtil.getEndpoint() + "/";
            if (avatarUrl.startsWith(prefix)) {
                String objectName = avatarUrl.substring(prefix.length());
                // 创建OSSClient实例
                OSS ossClient = new OSSClientBuilder().build(aliOssUtil.getEndpoint(), aliOssUtil.getAccessKeyId(), aliOssUtil.getAccessKeySecret());
                try {
                    ossClient.deleteObject(aliOssUtil.getBucketName(), objectName);
                    log.info("成功删除旧头像: {}", objectName);
                } finally {
                    ossClient.shutdown();
                }
            }
        } catch (Exception e) {
            log.error("删除旧头像失败: {}", avatarUrl, e);
            // 不影响主业务，不抛出异常
        }
    }
}





