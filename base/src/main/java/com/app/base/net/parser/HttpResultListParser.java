package com.app.base.net.parser;

import com.app.base.net.HttpResult;
import com.app.base.net.RxHttpPlugins;
import com.app.base.net.http.Request;
import com.app.base.utils.JsonUtils;
import com.app.base.utils.LogUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import okhttp3.Response;

public class HttpResultListParser<T> implements Parser<HttpResult<List<T>>> {

    private Type mType;
    private Request mRequest;

    public HttpResultListParser(Type type, Request request) {
        mType = type;
        mRequest = request;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HttpResult<List<T>> parse(Response response) throws IOException {
        HttpResult<List<T>> httpResult = null;
        if (response.isSuccessful() && response.body() != null) {
            mType = new ParameterizedTypeImpl(List.class, mType);
            Type type = new ParameterizedTypeImpl(HttpResult.class, mType);

            String httpJson = response.body().string();
            LogUtils.i("httpJson:" + httpJson);
            httpResult = JsonUtils.OBJECT_MAPPER.readValue(httpJson, new JacksonType<HttpResult<List<T>>>(type));

            httpResult = RxHttpPlugins.preProcessResult(httpResult, mRequest, mType);

        } else {

            httpResult = new HttpResult<>(response.code(), response.message());

        }

        return httpResult;
    }
}
