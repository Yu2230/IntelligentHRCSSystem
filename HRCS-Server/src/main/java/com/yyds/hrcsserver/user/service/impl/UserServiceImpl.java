package com.yyds.hrcsserver.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcscommon.client.MailClient;
import com.yyds.hrcscommon.client.PasswordClient;
import com.yyds.hrcscommon.constants.ConfigEnum;
import com.yyds.hrcscommon.constants.ErrorEnum;
import com.yyds.hrcscommon.constants.PostConstants;
import com.yyds.hrcscommon.constants.TimeOutEnum;
import com.yyds.hrcscommon.exception.BusinessException;
import com.yyds.hrcscommon.utils.JwtUtils;
import com.yyds.hrcscommon.utils.ThrowUtils;
import com.yyds.hrcspojo.data.user.LoginByCodeDTO;
import com.yyds.hrcspojo.data.user.LoginDTO;
import com.yyds.hrcspojo.data.user.LoginVO;
import com.yyds.hrcspojo.data.user.RegisterDTO;
import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.user.mapper.UserMapper;
import com.yyds.hrcsserver.user.repository.UserRepository;
import com.yyds.hrcsserver.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
* @author 21641
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-06-10 15:48:46
*/
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailClient  mailClient;

    @Autowired
    private PasswordClient  passwordClient;
    @Override
    public void sendCode(String email){
        String code = UUID.randomUUID().toString().substring(0, 6);
        mailClient.sendMail(email, code);
        redisTemplate.opsForValue().set(email, code, TimeOutEnum.TOKEN_TIME_OUT.getTimeOut(), TimeUnit.MINUTES);
    }

    /**
     * 注册
     * @param registerDTO
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        log.info("用户注册: {}",  registerDTO);
        String email = registerDTO.getEmail();
        String code = redisTemplate.opsForValue().get(email);
        //检查email是否已经被注册
        userRepository.selectOptByEmail(email).orElseThrow(() -> new BusinessException(ErrorEnum.REGISTER_ERROR));
        //验证传过来的code和Redis的code一致性
        String codeRedis = redisTemplate.opsForValue().get(email);
        ThrowUtils.throwIf(code == null || !code.equals(codeRedis), ErrorEnum.CODE_ERROR);
        String password = passwordClient.hashPassword(registerDTO.getPassword());
        User user = getUser(registerDTO,  password);
        userRepository.save(user);
    }

    private User getUser(RegisterDTO registerDTO,  String password) {
       return User.builder()
                .email(registerDTO.getEmail())
                .userName(registerDTO.getUserName())
                .password(password)
                .phone(registerDTO.getPhone())
                .sex(registerDTO.getSex())
                .address(registerDTO.getAddress())
                .avatar(ConfigEnum.DEFAULT_AVATAR.getText())
                .post(PostConstants.DEFAULT_POST)
                .role(PostConstants.user)
                .build();
    }
    /**
     * 邮箱密码登录
     * @param loginDTO
     * @return
     */
    @Override
    public LoginVO login(LoginDTO loginDTO){
        User user = userRepository.selectOptByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorEnum.NOT_FOUND_USER));
        int role = user.getRole();
        if(role != loginDTO.getRole()){
            throw new BusinessException(ErrorEnum.ROLE_IS_ERROR);
        }
        if(!passwordClient.verifyPassword(loginDTO.getPassword(), user.getPassword())){
            throw new BusinessException(ErrorEnum.LOGIN_ERROR);
        }
        return new LoginVO(user, JwtUtils.generate(user.getId().toString()));
    }

    /**
     * 验证码登录
     * @param loginByCodeDTO
     * @return
     */
    @Override
    public LoginVO loginByCode(LoginByCodeDTO loginByCodeDTO){
        User user = userRepository.selectOptByEmail(loginByCodeDTO.getEmail())
                .orElseThrow(() -> new BusinessException(ErrorEnum.NOT_FOUND_USER));
        if(Objects.isNull(user)){
            throw new BusinessException("用户不存在,请先注册");
        }
        int role = user.getRole();
        if(role != loginByCodeDTO.getRole()){
            throw new BusinessException(ErrorEnum.ROLE_IS_ERROR);
        }
        if (!redisTemplate.opsForValue().get(loginByCodeDTO.getEmail()).equals(loginByCodeDTO.getCode())){
            throw new BusinessException(ErrorEnum.LOGIN_ERROR);
        }
        return new LoginVO(user, JwtUtils.generate(user.getId().toString()));
    }

    @Override
    public void forgetPassword(String email) {

    }


}




