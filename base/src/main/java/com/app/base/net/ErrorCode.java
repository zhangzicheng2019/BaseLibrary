package com.app.base.net;

public enum ErrorCode {

    HTTP_UNKNOWN(-2, "未知异常"),
    HTTP_NO_NETWORK(2000, "无网络连接"),
    HTTP_IO_ERROR(2001, "网络异常"),
    HTTP_SERVER_ERROR(2002, "服务端异常"),
    HTTP_RESPONSE_ERROR(2003, "服务端返回数据无效"),
    HTTP_HOST_ERROR(2004, "域名解析异常"),
    HTTP_DATA_PARSE_ERROR(2005, "数据解析异常");

    private int code;
    private String desc;

    private int httpCode;

    ErrorCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public ErrorCode setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    @Override
    public String toString() {
        return "ErrorCode{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                ", httpCode=" + httpCode +
                '}';
    }
}



