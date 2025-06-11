package com.yyds.hrcsserver.user.repository;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yyds.hrcspojo.entity.User;

import java.util.Optional;

public interface UserRepository extends IService<User> {
    Optional<User> selectOptByEmail(String email);
}
