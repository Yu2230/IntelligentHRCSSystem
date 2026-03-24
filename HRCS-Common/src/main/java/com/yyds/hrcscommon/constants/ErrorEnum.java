package com.yyds.hrcscommon.constants;

import lombok.Data;

/**
 * 自定义错误码

 */

public enum ErrorEnum {

    SUCCESS(0, "ok"),
    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_LOGIN_ERROR(40100, "未知错误"),
    NO_AUTH_ERROR(40101, "无权限"),
    NOT_FOUND_ERROR(40300, "请求数据不存在"),
    NOT_FOUND_USER(40302,"用户不存在"),
    ROLE_IS_ERROR(400301,"用户角色错误"),
    FORBIDDEN_ERROR(40400, "禁止访问"),
    CODE_ERROR(40500, "验证码错误"),
    REGISTER_ERROR(40600,"注册失败, 用户已存在"),
    LOGIN_ERROR(40700, "登录失败, 用户名或密码错误"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    MYSQL_ERROR(50001, "数据库异常"),
    OPERATION_ERROR(50001, "操作失败"),
    API_REQUEST_ERROR(50010, "接口调用失败"),
    UPDATE_AVATAR_ERROR(50011, "更新头像失败"),
    NO_PERMISSION(50013, "没有权限"),
    UPDATE_FAILED(50014, "更新失败"),
    PHONE_EXISTS(50015, "手机号已存在"),
    USER_NAME_EXISTS(50016, "用户名已存在"),
    ID_CARD_EXISTS(50017, "身份证已存在"),
    BANK_CARD_EXISTS(50018, "银行卡已存在"),
    FILE_EMPTY(50019, "文件不能为空"),
    FILE_TYPE_NOT_SUPPORTED(50020, "不支持的文件类型"),
    FILE_SIZE_EXCEED(50021, "文件大小超出限制"),
    UPLOAD_FAILED(50022, "文件上传失败"),
    DEPT_ID_IS_NULL(50023, "部门ID为空"),
    MANAGER_ID_IS_NULL(50024, "管理员ID为空"),
    DEPT_NOT_EXIST(50025, "部门不存在"),
    USER_NOT_EXIST(50026, "用户不存在"),
    USER_ID_IS_NULL(50027, "用户ID为空"),
    DEPT_NOT_FOUND(50028, "部门不存在"),
    USER_NOT_FOUND(50029, "用户不存在"),
    DEPT_CREATE_FAILED(50023, "部门创建失败"),
    USER_ALREADY_MANAGER(50029, "用户已是其他部门负责人，无法重复任命"),
    DEPT_HAS_USERS(50030, "部门下存在用户，无法删除"),
    USER_LIST_IS_EMPTY(50031, "用户列表不能为空"),
    USER_ALREADY_IN_DEPT(50032, "用户已在其他部门，无法重复添加"),
    USER_UPDATE_FAILED(50033, "用户部门更新失败"),
    USER_NOT_IN_DEPT(50034, "用户不在该部门，无法移除"),
    DEPT_HAS_NON_MANAGER_USERS(50035, "部门下存在非管理员用户，无法删除"),
    GET_LOCK_ERROR(50012, "请稍后再试"),
    CAN_NOT_DELETE_NOTICE_USING(50037, "公告正在使用中，无法删除"),
    NOTICE_NOT_FOUND(50036, "公告不存在"),
    UNAUTHORIZED(50037,"未许可" );
    /**
     * 状态码
     */
    private final int code;

    /**
     * 信息
     */
    private final String message;

    ErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
