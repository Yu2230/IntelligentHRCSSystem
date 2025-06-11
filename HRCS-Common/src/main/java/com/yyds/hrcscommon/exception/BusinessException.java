package com.yyds.hrcscommon.exception;


import com.yyds.hrcscommon.constants.ErrorEnum;

public class BusinessException extends RuntimeException {


    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage());
    }

    public BusinessException(ErrorEnum errorEnum, String message) {
        super(message);
    }

}
