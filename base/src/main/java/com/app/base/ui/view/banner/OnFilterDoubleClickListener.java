package com.app.base.ui.view.banner;

import android.view.View;

/**
 * OnFilterDoubleClickListener
 * <p>
 * 过滤快速点击事件
 */
public abstract class OnFilterDoubleClickListener implements View.OnClickListener {
    private int mThrottleFirstTime = 1000;
    private long mLastClickTime = 0;

    public OnFilterDoubleClickListener() {
    }

    public OnFilterDoubleClickListener(int throttleFirstTime) {
        mThrottleFirstTime = throttleFirstTime;
    }

    @Override
    public void onClick(View v) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - mLastClickTime > mThrottleFirstTime) {
            mLastClickTime = currentTime;
            onFilterDoubleClick(v);
        }
    }

    public abstract void onFilterDoubleClick(View v);
}
