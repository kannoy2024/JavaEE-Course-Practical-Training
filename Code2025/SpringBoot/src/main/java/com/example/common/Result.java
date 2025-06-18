package com.example.common;
//包装类，用来统一后端返回的数据类型
//Code是前端判断请求成功的依据
//msg是错误信息
//data是数据
public class Result {
    private String code;
    private Object data;
    private String msg;

    public static Result success(Object data){
        Result result = new Result();
        result.setCode("200");
        result.setData(data);
        result.setMsg("请求成功");
        return result;
    }

    public static Result success(){
        Result result = new Result();
        result.setCode("200");
        result.setMsg("请求成功");
        return result;
    }

    public static Result error(String msg){
        Result result = new Result();
        result.setCode("500");
        result.setMsg(msg);
        return result;
    }


    public static Result error(String code,String msg){
        Result result = new Result();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
