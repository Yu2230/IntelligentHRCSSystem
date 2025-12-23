package com.yyds.hrcsserver.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yyds.hrcspojo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* @author 21641
* @description 针对表【user(用户表)】的数据库操作Mapper
* @createDate 2025-06-10 15:48:46
* @Entity com.yyds.hrcspojo.entity.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

    void removeUserDepartment(@Param("userId") Long userId);
}




