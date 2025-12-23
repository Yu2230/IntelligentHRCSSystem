package com.yyds.hrcsserver.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcspojo.entity.UserSalaryProfile;

import com.yyds.hrcsserver.mapper.UserSalaryProfileMapper;
import com.yyds.hrcsserver.service.UserSalaryProfileService;
import org.springframework.stereotype.Service;

/**
* @author 21641
* @description 针对表【user_salary_profile(员工薪酬档案表)】的数据库操作Service实现
* @createDate 2025-12-20 16:37:41
*/
@Service
public class UserSalaryProfileServiceImpl extends ServiceImpl<UserSalaryProfileMapper, UserSalaryProfile>
    implements UserSalaryProfileService {

}




