package com.app.base.net.http;

import android.text.TextUtils;
import com.app.base.utils.HttpUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Map;

public class PostJsonRequest extends Request {

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json");

    private String mPostJson;

    public PostJsonRequest(String url){
        super(url);
    }

    public PostJsonRequest(String url, String postJson) {
        super(url);
        mPostJson = postJson;
    }

    @Override
    public okhttp3.Request buildOkHttpRequest() {
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(mUrl);
        if (mHeaderMap != null) {
            for (Map.Entry<String, String> entry : mHeaderMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                builder.addHeader(key, value);
            }
        }
        if(TextUtils.isEmpty(mPostJson)){
            mPostJson = HttpUtils.mapToJson(mParamMap);
        }

        return builder.tag(mTag).post(RequestBody.create(MEDIA_TYPE_JSON, mPostJson)).build();

    }

}
