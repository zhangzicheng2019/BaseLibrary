package com.app.base.ui.view.banner.transformer;

import androidx.core.view.ViewCompat;
import android.view.View;

public class RotatePageTransformer extends BannerPageTransformer {
    private float mMaxRotation = 15.0f;

    public RotatePageTransformer() {
    }

    public RotatePageTransformer(float maxRotation) {
        setMaxRotation(maxRotation);
    }

    @Override
    public void handleInvisiblePage(View view, float position) {
        ViewCompat.setPivotX(view, view.getMeasuredWidth() * 0.5f);
        ViewCompat.setPivotY(view, view.getMeasuredHeight());
        ViewCompat.setRotation(view, 0);
    }

    @Override
    public void handleLeftPage(View view, float position) {
        float rotation = (mMaxRotation * position);
        ViewCompat.setPivotX(view, view.getMeasuredWidth() * 0.5f);
        ViewCompat.setPivotY(view, view.getMeasuredHeight());
        ViewCompat.setRotation(view, rotation);
    }

    @Override
    public void handleRightPage(View view, float position) {
        handleLeftPage(view, position);
    }

    public void setMaxRotation(float maxRotation) {
        if (maxRotation >= 0.0f && maxRotation <= 40.0f) {
            mMaxRotation = maxRotation;
        }
    }
}