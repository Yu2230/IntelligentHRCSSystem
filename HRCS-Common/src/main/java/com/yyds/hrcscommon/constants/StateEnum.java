package com.yyds.hrcscommon.constants;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StateEnum {
    /**
     * 请求状态枚举
     */
    SUCCESS(0,"请求成功"),
    FAIL(1,"操作失败"),
    ERROR(2,"服务异常");
    private final int code;
    private final String msg;
}
