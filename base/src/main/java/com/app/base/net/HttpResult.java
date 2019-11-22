package com.app.base.net;

import android.text.TextUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class HttpResult<T> {

    @JsonProperty("errorCode")
    private String code;

    private String msg;

    private T data;

    public HttpResult(){}

    public HttpResult(ErrorCode errorCode) {
        this.code = String.valueOf(errorCode.getCode());
        this.msg = errorCode.getDesc();
    }

    public HttpResult(int code, String msg) {
        this.code = String.valueOf(code);
        this.msg = msg;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        if(TextUtils.isEmpty(msg)){
            return code;
        }
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "HttpResult{" +
                "msg='" + msg + '\'' +
                ", code=" + code +
                ", data=" + data +
                '}';
    }
}


