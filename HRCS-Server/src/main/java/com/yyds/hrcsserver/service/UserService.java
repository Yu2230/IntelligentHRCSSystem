package com.yyds.hrcsserver.service;

import com.yyds.hrcspojo.data.user.CountINFO;
import com.yyds.hrcspojo.data.user.DailyStatsUserDTO;
import com.yyds.hrcspojo.data.user.login.LoginByCodeDTO;
import com.yyds.hrcspojo.data.user.login.LoginDTO;
import com.yyds.hrcspojo.data.user.login.LoginVO;
import com.yyds.hrcspojo.data.user.RegisterDTO;

import com.yyds.hrcspojo.data.user.login.UserInfoVO;
import com.yyds.hrcspojo.data.user.update.UpdateDTO;
import com.yyds.hrcspojo.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author 21641
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-06-10 15:48:46
*/
public interface UserService {

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


    int getRole(String email);

    void  updateUserInfo(UpdateDTO updateUserInfo);


    User getUser(String name);

    List<User> getAllUser(Integer pageNum, Integer pageSize,String name);

    User getCurrentUserInfo(String id);

    void updateAvatar(String id, String avatar);

    List<User> getAnyUser();

    UserInfoVO getUserInfo(String id);

    CountINFO getCount();

    List<DailyStatsUserDTO> getDailyUserCountInfo();
}
