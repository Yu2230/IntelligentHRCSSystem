package com.yyds.hrcsserver.controller;

import com.yyds.hrcscommon.constants.ErrorEnum;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcscommon.result.PageResult;
import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcscommon.utils.AliOssUtil;
import com.yyds.hrcscommon.utils.UserContext;
import com.yyds.hrcspojo.login.LoginByCodeDTO;
import com.yyds.hrcspojo.login.LoginDTO;
import com.yyds.hrcspojo.data.user.RegisterDTO;
import com.yyds.hrcspojo.login.UserInfoVO;
import com.yyds.hrcspojo.search.UserListItemVO;
import com.yyds.hrcspojo.update.UpdateDTO;
import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@Tag(name = "用户模块", description = "用户相关接口")  // ✅ 替换 @Api
@RestController
@RequestMapping("/user")
public class UserController {

    public final UserService userService;
    private final AliOssUtil aliOssUtil;

    public UserController(UserService userService, AliOssUtil aliOssUtil) {
        this.userService = userService;
        this.aliOssUtil = aliOssUtil;
    }
    @Operation(summary = "发送邮箱验证码")  // ✅ 替换 @ApiOperation
    @PostMapping("/sendCode")
    public Result sendCode(@Valid @RequestBody String email){
        log.info("send code, email {}", email);
        userService.sendCode(email);
        return Result.getSuccessResult("验证码发送成功");
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO){
        userService.register(registerDTO);
        return Result.getSuccessResult("用户注册成功");
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO){
        return Result.getSuccessResult(userService.login(loginDTO));
    }

    @Operation(summary = "用户登录(验证码)")
    @PostMapping("/loginByCode")
    public Result loginByCode(@RequestBody LoginByCodeDTO loginByCodeDTO){
        return Result.getSuccessResult(userService.loginByCode(loginByCodeDTO));
    }



    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/updateUserInfo")
    public Result updateUserInfo(@RequestBody UpdateDTO updateUserInfo){
        try {
            userService.updateUserInfo(updateUserInfo);
            return Result.getSuccessResult();
        } catch (BusinessException e) {
            return Result.getFailureResultByMsg(e.getMessage());
        }
    }


    /**
     * 获取所有用户分页，根据name模糊查询并且按照createTime降序排序
     */
    @Operation(summary = "获取所有用户分页，根据name模糊查询并且按照createTime降序排序")
    @GetMapping("/getAllUser")
    public Result getAllUser(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestParam String name){
        return Result.getSuccessResult(userService.getAllUser(pageNum, pageSize, name));
    }

    /**
     * 用户列表聚合视图（部门/岗位/在职状态）
     */
    @Operation(summary = "用户列表聚合视图")
    @GetMapping("/listView")
    public Result<PageResult<UserListItemVO>> getListView(@RequestParam Integer pageNum,
                                                           @RequestParam Integer pageSize,
                                                           @RequestParam(required = false) String name) {
        return Result.getSuccessResult(userService.getUserListView(pageNum, pageSize, name));
    }

    /**
     * 获取所有用户
     */
    @Operation(summary = "获取所有用户")
    @GetMapping("/getAll")
    public Result getAll(){
        return Result.getSuccessResult(userService.getAnyUser());
    }


    /**
     * 根据id获取用户信息
     */
    @Operation(summary = "根据id获取用户信息")
    @GetMapping("/getUserInfo")
    public Result<UserInfoVO> getUserInfo(@RequestParam String id){
        return Result.getSuccessResult(userService.getUserInfo(id));
    }

    /**
     * 获取指定用户
     */
    @Operation(summary = "获取指定用户")
    @GetMapping("/getUser")
    public Result getUser(@RequestParam String name){
        return Result.getSuccessResult(userService.getUser(name));
    }

    /**
     * 根据当前用户ID获取用户信息
     */
    @Operation(summary = "根据当前用户ID获取用户信息")
    @GetMapping("/getCurrentUserInfo")
    public Result<User> getCurrentUserInfo(){
        String id = UserContext.getCurrentUserId();
        return Result.getSuccessResult(userService.getCurrentUserInfo(id));
    }

    /**
     * 更新头像
     */
    @Operation(summary = "更新头像")
    @PostMapping("/uploadAvatar")
    public Result<String> uploadAvatar(@RequestParam("file") MultipartFile file,
                                       @RequestParam("userId") Long userId) {
        // 1. 校验文件
        if (file.isEmpty()) {
            throw new BusinessException(ErrorEnum.FILE_EMPTY);
        }

        // 2. 校验文件类型（只允许图片）
        String contentType = file.getContentType();
        if (!contentType.startsWith("image/")) {
            throw new BusinessException(ErrorEnum.FILE_TYPE_NOT_SUPPORTED);
        }

        // 3. 校验文件大小（例如限制5MB）
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new BusinessException(ErrorEnum.FILE_SIZE_EXCEED);
        }

        try {
            // 4. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String extension = StringUtils.getFilenameExtension(originalFilename);
            String objectName = "avatar/" + userId + "/" + System.currentTimeMillis() + "." + extension;

            // 5. 上传文件到OSS
            byte[] bytes = file.getBytes();
            String avatarUrl = aliOssUtil.upload(bytes, objectName);

            // 6. 更新用户头像URL
            userService.updateAvatar(String.valueOf(userId), avatarUrl);

            return Result.getSuccessResult(avatarUrl);
        } catch (IOException e) {
            log.error("头像上传失败", e);
            throw new BusinessException(ErrorEnum.UPLOAD_FAILED);
        }
    }
    /**
     * 获取所有用户数量
     */
}

