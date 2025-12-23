package com.yyds.hrcsserver.controller;


import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcscommon.utils.FileParserUtils;
import com.yyds.hrcscommon.utils.UserContext;

import com.yyds.hrcspojo.entity.Notice;
import com.yyds.hrcsserver.repository.UserRepository;
import com.yyds.hrcsserver.service.NoticeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/notice")
@Api(tags = "公告模块")
public class NoticeController {
    private final NoticeService noticeService;
    private final UserRepository userRepository;
    public NoticeController(NoticeService noticeService, UserRepository userRepository) {
        this.noticeService = noticeService;
        this.userRepository = userRepository;
    }

    //todo 带附件创建公告
    /**
     * 添加公告（草稿）
     */
    @PostMapping(value = "/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation("添加公告")
    public Result addNotice(
            @RequestParam String title,
            @RequestParam Integer type,
            @RequestParam Integer priority,
            @RequestParam(required = false) String content,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) MultipartFile file) {

        try {
            String username = userRepository.getById(Long.valueOf(UserContext.getCurrentUserId())).getName();

            Notice notice = new Notice();
            notice.setTitle(title);
            notice.setDepartmentId(departmentId);
            notice.setCreateBy(username);
            // 文件内容优先
            if (file != null && !file.isEmpty()) {
                String parsedContent = FileParserUtils.extractText(file.getInputStream(),
                        FileUtil.getSuffix(file.getOriginalFilename()));
                notice.setContent(parsedContent);
            } else {
                notice.setContent(content);
            }

            notice.setType(type);
            notice.setPriority(priority);

            boolean saved = noticeService.saveWithFile(notice, file);
            return saved ? Result.getSuccessResult(notice.getId()) : Result.getErrorResultByMsg("保存失败");

        } catch (BusinessException e) {
            return Result.getErrorResultByMsg(e.getMessage());
        } catch (Exception e) {
            log.error("添加公告异常", e);
            return Result.getErrorResultByMsg("系统异常");
        }
    }

    /**
     * 发布公告
     */
    @PostMapping("/publish")
    @ApiOperation("发布公告")
    public Result<Boolean> publishNotice(@RequestParam Long id) {
        try {
            boolean success = noticeService.publishNotice(id);
            return success ? Result.getSuccessResult(true) : Result.getErrorResultByMsg("发布失败");
        } catch (BusinessException e) {
            return Result.getErrorResultByMsg(e.getMessage());
        }
    }

    /**
     * 展示所有公告,分页，模糊查询
     */
    @GetMapping("/list")
    @ApiOperation("分页展示所有公告，模糊查询")
    public Result<IPage<Notice>> listNotice(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize, @RequestParam(required = false) String keyword){
       IPage<Notice> noticeList = noticeService.listNotice(pageNum, pageSize, keyword);
        return Result.getSuccessResult(noticeList);
    }

    /**
     * 删除公告
     */
    @RequestMapping("/delete")
    @ApiOperation(value = "删除公告", notes = "删除公告")
    public Result deleteNotice(@RequestParam Long id) {
        boolean flag = noticeService.deleteWithFile(id);
        return flag ? Result.getSuccessResult("删除成功") : Result.getErrorResult("删除失败");
    }
    /**}
     * 下架公告
     */
    @RequestMapping("/offline")
    @ApiOperation(value = "下架公告", notes = "下架公告")
    public Result offlineNotice(@RequestParam Long id) {
        return noticeService.updateNotice(id) ? Result.getSuccessResult("下架成功") : Result.getErrorResult("下架失败");
    }

    /**
     * 修改公告
     */
    @RequestMapping("/update")
    @ApiOperation(value = "修改公告", notes = "修改公告")
    public Result updateNotice(@RequestParam Long id) {
        return noticeService.updateNotice(id) ? Result.getSuccessResult("修改成功") : Result.getErrorResult("修改失败");
    }

    /**
     * 根据id获取本部门的公告
     */
    @GetMapping("/getDepartmentId")
    @ApiOperation(value = "根据id获取本部门的公告", notes = "根据id获取本部门的公告")
    public Result<IPage<Notice>> getDepartmentId(@RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize,@RequestParam Long id) {
        return Result.getSuccessResult(noticeService.getNoticeByDepartmentId(pageNum, pageSize, id));
    }
    /**
     * 根据id获得详情
     */
    @GetMapping("/getDetail")
    @ApiOperation(value = "根据id获得详情", notes = "根据id获得详情")
    public Result<Notice> getDetail(@RequestParam Long id) {
        return Result.getSuccessResult(noticeService.getDetail(id));
    }
}
