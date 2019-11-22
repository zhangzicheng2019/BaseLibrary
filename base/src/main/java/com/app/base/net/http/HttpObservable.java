package com.app.base.net.http;

import com.app.base.net.parser.Parser;
import com.app.base.utils.HttpUtils;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.internal.functions.ObjectHelper;
import io.reactivex.internal.observers.DeferredScalarDisposable;
import io.reactivex.plugins.RxJavaPlugins;
import okhttp3.Call;
import okhttp3.Response;

import java.util.concurrent.Callable;

public final class HttpObservable<T> extends Observable<T> implements Callable<T> {

    private final okhttp3.Request request;
    private final Parser<T> parser;

    private Call mCall;

    public HttpObservable(okhttp3.Request request, Parser<T> parser) {
        this.request = request;
        this.parser = parser;
    }

    @Override
    public void subscribeActual(Observer<? super T> observer) {
        HttpDisposable d = new HttpDisposable(observer);
        observer.onSubscribe(d);
        if (d.isDisposed()) {
            return;
        }
        T value;
        try {
            value = ObjectHelper.requireNonNull(execute(), "Callable returned null");
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            if (!d.isDisposed()) {
                observer.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
            return;
        }
        d.complete(value);
    }

    @Override
    public T call() throws Exception {
        return ObjectHelper.requireNonNull(execute(), "The callable returned a null value");
    }

    //执行请求
    private T execute() throws Exception {
        Call call = mCall = HttpUtils.getOkHttpClient().newCall(request);
        Response response = call.execute();
        return parser.parse(response);
    }

    class HttpDisposable extends DeferredScalarDisposable<T> {

        private static final long serialVersionUID = -6553295560504463237L;

        /**
         * Constructs a DeferredScalarDisposable by wrapping the Observer.
         *
         * @param downstream the Observer to wrap, not null (not verified)
         */
        HttpDisposable(Observer<? super T> downstream) {
            super(downstream);
        }

        @Override
        public void dispose() {
            //关闭请求
            if (mCall != null && !mCall.isCanceled()){
                mCall.cancel();
            }
            super.dispose();
        }


    }

}
