package com.app.base.net.http;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Request {

    String mUrl;

    Object mTag;

    Map<String, Object> mParamMap = new HashMap<>();

    Map<String, String> mHeaderMap;

    public Request(String url){
        mUrl = url;
    }

    public void header(String name, String value) {
        if(mHeaderMap == null){
            mHeaderMap = new HashMap<>();
        }
        mHeaderMap.put(name, value);
    }

    public void param(String key, Object value){
        mParamMap.put(key, value);
    }

    public void params(Map<String, Object> params){
        mParamMap.putAll(params);
    }

    public void tag(Object tag){
        mTag = tag;
    }

    public void tag(UpFile tag){
        mTag = tag;
    }

    public String getUrl(){
        return mUrl;
    }

    public boolean containsHeader(String key){
        if(mHeaderMap != null){
            return mHeaderMap.containsKey(key);
        }
        return false;
    }

    public boolean containsParam(String key){
        return mParamMap.containsKey(key);
    }

    /**
     * 预留方法，子类实现
     *
     * */
    public void file(String key, List<String> pathList){
        throw new IllegalArgumentException("不支持的参数类型！");
    }

    public abstract okhttp3.Request buildOkHttpRequest();

}


