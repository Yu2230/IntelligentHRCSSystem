package com.yyds.hrcscommon.exception;

import com.yyds.hrcscommon.constants.ErrorEnum;
import com.yyds.hrcscommon.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常（验证码错误、用户已存在等）
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常 - 错误码: {}, 消息: {}", e.getCode(), e.getMessage());

        // 直接返回带错误码和消息的Result
        return Result.<Void>builder()
                .code(e.getCode())
                .msg(e.getMessage())
                .build();
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();
        log.warn("参数校验失败: {}", message);

        return Result.getFailureResultByMsg(message);
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.getErrorResultByMsg("系统错误，请联系管理员");
    }
}
