package com.app.base.net.http;

import com.app.base.utils.HttpUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostFormRequest extends Request{

    private List<UpFile> mUpFileList;

    public PostFormRequest(String url) {
        super(url);
    }

    @Override
    public void file(String key, List<String> pathList) {
        mUpFileList = new ArrayList<>();
        for (String path: pathList) {
            UpFile upFile = new UpFile(key, path);
            mUpFileList.add(upFile);
        }
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
        if(mUpFileList != null && mUpFileList.size() > 0){
            builder.post(HttpUtils.buildFormRequestBody(mParamMap, mUpFileList));
        } else {
            builder.post(HttpUtils.buildFormRequestBody(mParamMap));
        }
        return builder.build();
    }
}
