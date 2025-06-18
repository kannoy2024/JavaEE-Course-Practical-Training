package com.example.exception;
//全局异常捕获器

import com.example.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;


@ControllerAdvice("com.example.controller")
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseBody//控制Result对象转换成json串的形式
    public Result error(Exception e){
        log.error("系统异常",e);
        return Result.error("系统异常");
    }

    @ExceptionHandler(CustomerException.class)
    @ResponseBody//控制Result对象转换成json串的形式
    public Result error(CustomerException e){
        log.error("自定义错误",e);
        return Result.error(e.getCode(),e.getMsg());
    }

}
