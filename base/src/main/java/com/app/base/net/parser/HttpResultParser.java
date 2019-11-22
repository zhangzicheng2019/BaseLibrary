package com.app.base.net.parser;

import com.app.base.net.HttpResult;
import com.app.base.net.RxHttpPlugins;
import com.app.base.net.http.Request;
import com.app.base.utils.JsonUtils;
import com.app.base.utils.LogUtils;

import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;

public class HttpResultParser<T> implements Parser<HttpResult<T>> {

    private Type mType;
    private Request mRequest;

    public HttpResultParser(Type type, Request request){
        mType = type;
        mRequest = request;
    }

    @SuppressWarnings("unchecked")
    @Override
    public HttpResult<T> parse(Response response) throws IOException {
        HttpResult<T> httpResult = null;
        if (response.isSuccessful() && response.body() != null) {

            Type type = new ParameterizedTypeImpl(HttpResult.class, mType);

            String httpJson = response.body().string();
            LogUtils.i("httpJson:" + httpJson);

            httpResult = JsonUtils.OBJECT_MAPPER.readValue(httpJson, new JacksonType<HttpResult<T>>(type));

            httpResult = RxHttpPlugins.preProcessResult(httpResult, mRequest, mType);

        } else {

            httpResult = new HttpResult<>(response.code(), response.message());

        }

        return httpResult;
    }
}
