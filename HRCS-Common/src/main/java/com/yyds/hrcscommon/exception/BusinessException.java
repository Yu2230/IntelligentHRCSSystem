package com.yyds.hrcscommon.exception;


import com.yyds.hrcscommon.constants.ErrorEnum;
import lombok.Data;


@Data
public class BusinessException extends RuntimeException {
    private Integer code;  // 添加错误码

    public BusinessException(String message) {
        super(message);
        this.code = 500;  // 默认系统错误
    }

    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
        this.code = errorEnum.getCode();  // 从枚举获取错误码
    }

    public BusinessException(ErrorEnum errorEnum, String message) {
        super(message);
        this.code = errorEnum.getCode();
    }



}
