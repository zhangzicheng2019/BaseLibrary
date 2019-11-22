package com.app.base.net;


import android.text.TextUtils;

import com.app.base.net.http.*;
import com.app.base.net.parser.HttpResultListParser;
import com.app.base.net.parser.HttpResultParser;
import com.app.base.net.parser.Parser;
import com.app.base.utils.AESUtils;
import com.app.base.utils.HttpUtils;
import com.app.base.utils.RSAUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Response;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class RxHttp {

    private Request request;
    private String aesKey;

    private RxHttp(Request request) {
        this.request = request;
    }

    public static RxHttp get(String url) {
        Request getRequest = new GetRequest(url);
        return new RxHttp(getRequest);
    }

    public static RxHttp request(Request request) {
        return new RxHttp(request);
    }

    public static RxHttp postJson(String url) {
        return new RxHttp(new PostJsonRequest(url));
    }

    public static RxHttp postJson(String url, String json) {
        return new RxHttp(new PostJsonRequest(url, json));
    }

    public static RxHttp postForm(String url) {
        return new RxHttp(new PostFormRequest(url));
    }

    public RxHttp addEncryptParam(String key, String value, String publicKey) {
        if (TextUtils.isEmpty(aesKey)) {
            aesKey = AESUtils.generateKey();
        }
        String aesValue = AESUtils.encryptData(aesKey, value);
        this.request.param(key, aesValue);
        try {
            String encryptKey = RSAUtils.encryptByPublicKey(aesKey.getBytes(), publicKey);
            this.request.header(HttpUtils.HEADER_KEY_AES_KEY, encryptKey.replace("\n", ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }

    public RxHttp addParam(String key, Object value) {
        this.request.param(key, value);
        return this;
    }

    public RxHttp addParams(Map<String, Object> params) {
        this.request.params(params);
        return this;
    }

    public RxHttp addHeader(String name, String value) {
        this.request.header(name, value);
        return this;
    }

    public RxHttp addFile(String key, List<String> pathList) {
        this.request.file(key, pathList);
        return this;
    }


    public <T> HttpResult<T> execute(Class<T> clazz) throws IOException {
        RxHttpPlugins.setCommonParams(request);
        Response response = HttpUtils.getOkHttpClient().newCall(request.buildOkHttpRequest()).execute();
        return new HttpResultParser<T>(clazz, request).parse(response);
    }

    public <T> T execute(Parser<T> parser) throws IOException {
        RxHttpPlugins.setCommonParams(request);
        Response response = HttpUtils.getOkHttpClient().newCall(request.buildOkHttpRequest()).execute();
        return parser.parse(response);
    }

    public <T> Observable<HttpResult<T>> applyParser(Class<T> clazz) {
        RxHttpPlugins.setCommonParams(request);
        return new HttpObservable<>(request.buildOkHttpRequest(), new HttpResultParser<T>(clazz, request)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public <T> Observable< HttpResult<List<T>>> applyListParser(Class<T> clazz) {
        RxHttpPlugins.setCommonParams(request);
        return new HttpObservable<>(request.buildOkHttpRequest(), new HttpResultListParser<T>(clazz, request)).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }

    public <T> Observable<T> applyParser(Parser<T> parser) {
        RxHttpPlugins.setCommonParams(request);
        return new HttpObservable<>(request.buildOkHttpRequest(), parser).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }


}
