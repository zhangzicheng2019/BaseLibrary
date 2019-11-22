package com.app.base.net.http;

import com.app.base.utils.HttpUtils;

import java.util.Map;

public class GetRequest extends Request{


    public GetRequest(String url) {
        super(url);
    }

    @Override
    public okhttp3.Request buildOkHttpRequest() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(HttpUtils.mergeUrlAndParams(mUrl, mParamMap));
        if (mHeaderMap != null) {
            for (Map.Entry<String, String> entry : mHeaderMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.addHeader(key, value);
            }
        }
        return builder.tag(mTag).get().build();
    }
}
