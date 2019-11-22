package com.app.base.net;

import com.app.base.net.http.Request;

import java.lang.reflect.Type;

import io.reactivex.annotations.NonNull;
import io.reactivex.annotations.Nullable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.internal.util.ExceptionHelper;

public class RxHttpPlugins {

    private static Function<? super Request, ? extends Request> mRequestFunc;
    private static Function3<HttpResult, Request, Type, HttpResult> mResponseFunc;

    //设置公共参数装饰
    public static void setRequestFunc(@Nullable Function<? super Request, ? extends Request> requestFunc) {
        mRequestFunc = requestFunc;
    }

    /**
     * <P>对Param参数添加一层装饰,可以在该层做一些与业务相关工作，
     * <P>例如：添加公共参数/请求头信息
     *
     * @param source Param
     * @return 装饰后的参数
     */
    public static Request setCommonParams(Request source) {
//        if (source == null || !source.isAssemblyEnabled()) return source;
        Function<? super Request, ? extends Request> f = mRequestFunc;
        if (f != null) {
            return apply(f, source);
        }
        return source;
    }

    /**
     * 预处理请求结果
     *
     **/
    public static HttpResult preProcessResult(HttpResult httpResult, Request request, Type type){
        Function3<HttpResult,Request, Type, HttpResult> f = mResponseFunc;
            if (f != null) {
            return apply(f, httpResult, request, type);
        }
        return httpResult;
    }

    public static void setResponseFunc(Function3<HttpResult, Request, Type, HttpResult> responseFunc) {
        mResponseFunc = responseFunc;
    }

    @NonNull
    private static <T, R> R apply(@NonNull Function<T, R> f, @NonNull T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

    @NonNull
    private static <T1, T2, T3, R> R apply(@NonNull Function3<T1, T2, T3, R> f, @NonNull T1 t1, T2 t2, T3 t3) {
        try {
            return f.apply(t1, t2, t3);
        } catch (Throwable ex) {
            throw ExceptionHelper.wrapOrThrow(ex);
        }
    }

}

