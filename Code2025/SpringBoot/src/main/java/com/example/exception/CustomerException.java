package com.example.exception;

//自定义异常，运行时异常
public class CustomerException extends RuntimeException {
    private String code;
    private String msg;

    public CustomerException(String code, String message) {
        this.msg = message;
        this.code = code;
    }

    public CustomerException(String message) {
        this.msg = message;
        this.code = "500";
    }

    public CustomerException() {
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
