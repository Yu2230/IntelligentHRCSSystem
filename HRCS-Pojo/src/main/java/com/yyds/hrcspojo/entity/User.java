package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.yyds.hrcspojo.base.BaseEntity;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@Data
@Builder
@TableName(value ="user")
public class User  extends BaseEntity {

    /**
     * 用户名
     */
    private String userName;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 密码
     */
    private String password;

    /**
     * 电话号码
     */
    private String phone;

    /**
     * 性别
     */
    private Integer sex;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 职位
     */
    private Integer post;

    /**
     * 角色
     */
    private Integer role;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 身份证号码
     */
    private String idCard;

    /**
     * 银行卡号
     */
    private String bankCard;
    /**
     * 住址
     */
    private String address;

}