package com.yyds.hrcsserver.user.repository.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yyds.hrcspojo.entity.User;
import com.yyds.hrcsserver.user.mapper.UserMapper;
import com.yyds.hrcsserver.user.repository.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public class UserRepositoryImpl extends ServiceImpl<UserMapper, User> implements UserRepository {

    @Override
    public Optional<User> selectOptByEmail(String email) {
        return lambdaQuery()
                .eq(User::getEmail, email)
                .oneOpt();
    }
}
