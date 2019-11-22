package com.app.base.ui.presenter;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.app.base.ui.view.IBaseView;
import com.app.base.utils.LogUtils;

public class BasePresenter<V extends IBaseView> implements LifecycleObserver {

    private static String TAG = BasePresenter.class.getSimpleName();

    protected V mView;

    private BasePresenter(){}

    public BasePresenter(V view){
        mView = view;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void onCreate() {
        LogUtils.i(TAG, "onCreate()");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        LogUtils.i(TAG, "onStart()");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        LogUtils.i(TAG, "onResume()");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        LogUtils.i(TAG, "onPause()");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        LogUtils.i(TAG, "onStop()");
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        mView = null;
        LogUtils.i(TAG, "onDestroy()");
    }

}

