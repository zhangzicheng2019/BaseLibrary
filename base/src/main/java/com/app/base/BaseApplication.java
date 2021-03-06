package com.app.base;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDex;

import com.app.base.utils.LogUtils;
import com.app.base.utils.UiUtils;

public class BaseApplication extends Application {

    @SuppressLint("StaticFieldLeak")
    private static BaseApplication sApplication;

    private Activity mCurActivity = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sApplication = this;
        if (android.os.Build.VERSION.SDK_INT < 21) {
            //矢量图 适配android5.0以下
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
        registerActLifecycle();
    }

    public static BaseApplication getApplication(){
        return sApplication;
    }

    @Nullable
    public Activity getTopActivity(){
        if(mCurActivity != null){
            boolean isActRunning = UiUtils.isActRunning(mCurActivity);
            LogUtils.d("getTopActivity() -> isActRunning=" + isActRunning);
            return isActRunning ? mCurActivity : null;
        }
        LogUtils.d("getTopActivity() -> null");
        return null;
    }

    public void recycleCurActivity(){
        mCurActivity = null;
    }

    private void registerActLifecycle(){
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                mCurActivity = activity;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
