package com.yyds.hrcsserver.controller;

import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcscommon.utils.AliOssUtil;
import com.yyds.hrcspojo.data.user.CountINFO;
import com.yyds.hrcspojo.data.user.DailyStatsUserDTO;
import com.yyds.hrcspojo.notice.DaliyStateCountNoticeDTO;
import com.yyds.hrcsserver.service.NoticeService;
import com.yyds.hrcsserver.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    private final UserService userService;
    private final NoticeService noticeService;
    public CommonController(UserService userService, NoticeService noticeService) {
        this.userService = userService;
        this.noticeService = noticeService;
    }

    //文件上传路径为String
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传： {}",file);
        try {
            String originalFilename = file.getOriginalFilename();
            //截取文件名的后缀
            String extion = originalFilename.substring(originalFilename.lastIndexOf("."));//截取扩展名
            String objectName = UUID.randomUUID().toString() + extion;
            //文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.getSuccess("上传成功", filePath);

        } catch (IOException e) {
            log.info("文件上传失败");
            throw new RuntimeException(e);
        }
        //return null;
    }

    @Operation(summary = "获取用户角色")
    @GetMapping("/getRole")
    public Result getRole(@RequestParam String email){
        return Result.getSuccessResult(userService.getRole(email));
    }

    /**
     * 获取相关数量信息
     */
    @Operation(summary = "获取相关数量信息")
    @GetMapping("/getCount")
    public Result<CountINFO> getCount(){
        return Result.getSuccessResult(userService.getCount());
    }

    /**
     * 数据统计模块，
     */
    //显示每周的数据
    @Operation(summary = "数据统计模块")
    @GetMapping("/getDailyUserCountInfo")
    public Result<List<DailyStatsUserDTO>> getDailyUserCountInfo(){
        List<DailyStatsUserDTO> dailyStatsUserDTOS = userService.getDailyUserCountInfo();
        return Result.getSuccessResult(dailyStatsUserDTOS);
    }

    @Operation(summary = "公告数据统计模块")
    @GetMapping("/getDailyStateCountNoticeDTO")
    public Result<List<DaliyStateCountNoticeDTO>> getDaliyStateCountNoticeDTO(){
        List<DaliyStateCountNoticeDTO> daliyStateCountNoticeDTOS = noticeService.getDaliyStateCountNoticeDTO();
        return Result.getSuccessResult(daliyStateCountNoticeDTOS);
    }


}
