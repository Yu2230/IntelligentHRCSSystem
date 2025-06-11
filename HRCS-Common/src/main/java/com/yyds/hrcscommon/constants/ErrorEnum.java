package com.yyds.hrcscommon.constants;

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
    GET_LOCK_ERROR(50012, "请稍后再试");
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
