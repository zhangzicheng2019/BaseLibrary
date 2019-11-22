package com.app.base.function;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;

public class RetryWithRangeTime implements Function<Observable<Throwable>, ObservableSource<?>> {
    private static final String TAG = RetryWithRangeTime.class.getSimpleName();

    private int mRetryCount = 0;
    private static final int[] RETRY_RANGE_TIME = new int[]{1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048};

    private String mTag;

    public RetryWithRangeTime(String tag){
        this.mTag = tag;
    }

    @Override
    public Observable<?> apply(Observable<Throwable> throwableObservable) {
        return throwableObservable.flatMap(new Function<Throwable, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Throwable throwable) throws Exception {
                if(mRetryCount == RETRY_RANGE_TIME.length){
                    return Observable.error(new RuntimeException("请求" + mTag + "接口异常：" + throwable.getMessage() + "，重试次数达到上限！mRetryCount=" + mRetryCount));
                } else {
                    int delayTime = RETRY_RANGE_TIME[mRetryCount];
                    Log.e(TAG, "请求" + mTag + "接口异常：" + throwable.getMessage() + "开始重试 " + ++mRetryCount + " 次，delayTime=" + delayTime);
                    return Observable.timer(delayTime, TimeUnit.SECONDS);
                }
            }
        });
    }

    public static class Observer<T> extends DisposableObserver<T> {

        private String mTag;
        private boolean isUploadError;

        public Observer(String tag){
            this.mTag = tag;
        }

        public Observer(String tag, boolean isUploadError){
            this.mTag = tag;
            this.isUploadError = isUploadError;
        }

        @Override
        public void onComplete() {
            Log.i(TAG,"请求" + mTag + "接口完成");
        }

        @Override
        public void onNext(T t) {

        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG,"请求" + mTag + "接口异常：" + e.toString());
        }
    }
}