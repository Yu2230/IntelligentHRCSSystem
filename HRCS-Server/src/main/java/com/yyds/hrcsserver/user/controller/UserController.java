package com.yyds.hrcsserver.user.controller;

import com.yyds.hrcscommon.result.Result;
import com.yyds.hrcspojo.data.user.LoginByCodeDTO;
import com.yyds.hrcspojo.data.user.LoginDTO;
import com.yyds.hrcspojo.data.user.LoginVO;
import com.yyds.hrcspojo.data.user.RegisterDTO;
import com.yyds.hrcsserver.user.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(tags = "用户模块")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("发送邮箱验证码")
    @PostMapping("/sendCode")
    public Result sendCode(@RequestBody String email){
        userService.sendCode(email);
        return Result.getSuccessResult("验证码发送成功");
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result register(@RequestBody RegisterDTO registerDTO){
        userService.register(registerDTO);
        return Result.getSuccessResult("用户注册成功");
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result login(@RequestBody LoginDTO loginDTO){
        return Result.getSuccessResult(userService.login(loginDTO));
    }

    @ApiOperation("用户登录(验证码)")
    @PostMapping("/loginByCode")
    public Result loginByCode(@RequestBody LoginByCodeDTO loginByCodeDTO){
        return Result.getSuccessResult(userService.loginByCode(loginByCodeDTO));
    }

    public Result forgetPassword(@RequestBody String email){
        userService.forgetPassword(email);
        return Result.getSuccessResult();
    }
}
