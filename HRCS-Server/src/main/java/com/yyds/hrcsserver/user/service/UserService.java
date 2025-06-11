package com.yyds.hrcsserver.user.service;

import com.yyds.hrcspojo.data.user.LoginByCodeDTO;
import com.yyds.hrcspojo.data.user.LoginDTO;
import com.yyds.hrcspojo.data.user.LoginVO;
import com.yyds.hrcspojo.data.user.RegisterDTO;
import com.yyds.hrcspojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
* @author 21641
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-06-10 15:48:46
*/
public interface UserService extends IService<User> {

    /**
     * 发送邮箱验证码
     * @param email
     */
    void sendCode(String email);

    /**
     * 用户注册
     * @param registerDTO
     */
    void register(RegisterDTO registerDTO);

    /**
     * 邮箱密码登录
     * @param loginDTO
     * @return
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 邮箱验证码登录
     * @param loginByCodeDTO
     * @return
     */
    LoginVO loginByCode(LoginByCodeDTO loginByCodeDTO);


    void forgetPassword(String email);
}
