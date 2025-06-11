package com.yyds.hrcscommon.result;

import com.yyds.hrcscommon.constants.StateEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> implements Serializable {
    private int code;

    private String msg;

    private T data;

    public static <T>Result<T> getSuccessResult(){
        return getSuccessResult(null);
    }

    public static <T>Result<T> getSuccessResult(T data){
        return Result.<T>builder()
                .code(StateEnum.SUCCESS.getCode())
                .msg(StateEnum.SUCCESS.getMsg())
                .data(data)
                .build();
    }

    public static <T> Result<T> getSuccessResultByMsg(String msg){
        return Result.<T>builder()
                .code(StateEnum.SUCCESS.getCode())
                .msg(StateEnum.SUCCESS.getMsg())
                .build();
    }

    public static <T> Result<T> getErrorResultByMsg(String msg){
        return Result.<T>builder()
                .code(StateEnum.ERROR.getCode())
                .msg(StateEnum.ERROR.getMsg())
                .build();
    }

    public static <T> Result<T> getErrorResult(T data){
        return Result.<T>builder()
                .code(StateEnum.ERROR.getCode())
                .msg(StateEnum.ERROR.getMsg())
                .data(data)
                .build();
    }

    public static <T> Result<T> getErrorResult(){
        return getErrorResult(null);
    }

    public static <T> Result<T> getFailureResultByMsg(String msg){
        return Result.<T>builder()
                .code(StateEnum.FAIL.getCode())
                .msg(StateEnum.FAIL.getMsg())
                .build();
    }

    public static <T> Result<T> getFailureResult(T data){
        return Result.<T>builder()
                .code(StateEnum.FAIL.getCode())
                .msg(StateEnum.FAIL.getMsg())
                .data(data)
                .build();
    }

    public static <T> Result<T> getFailureResult(){
        return getFailureResult(null);
    }

}