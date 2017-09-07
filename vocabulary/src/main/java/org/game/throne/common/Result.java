package org.game.throne.common;

/**
 * Created by lvtu on 2017/7/29.
 */
public class Result<T> {
    T data;
    int code;//表示业务结果
    String message;

    public static <T> Result createSuccessResult(){
        Result r = new Result();
        r.data = null;
        r.code = 200;
        r.message = "success";
        return r;
    }

    public static <T> Result createSuccessResult(T data){
        Result<T> r = new Result<T>();
        r.data = data;
        r.code = 200;
        r.message = "success";
        return r;
    }

    public static <T> Result createErrorResult(){
        Result r = new Result();
        r.data = null;
        r.code = 500;
        r.message = "error";
        return r;
    }


    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
