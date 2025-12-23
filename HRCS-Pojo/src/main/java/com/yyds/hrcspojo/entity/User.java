package com.yyds.hrcspojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 用户表
 * @TableName user
 */
@Data
@TableName(value ="user")
public class User {
    private Long id;

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
    @TableField(fill = FieldFill.INSERT)
    private String createBy;  // 创建人ID

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String updateBy;  // 修改人ID

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;  // 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;  // 更新时间

    private Long departmentId;

}