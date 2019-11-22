package com.app.base.ui.view.banner.transformer;

import androidx.core.view.ViewCompat;
import android.view.View;

public class AccordionPageTransformer extends BannerPageTransformer {

    @Override
    public void handleInvisiblePage(View view, float position) {
    }

    @Override
    public void handleLeftPage(View view, float position) {
        ViewCompat.setPivotX(view, view.getWidth());
        ViewCompat.setScaleX(view, 1.0f + position);
    }

    @Override
    public void handleRightPage(View view, float position) {
        ViewCompat.setPivotX(view, 0);
        ViewCompat.setScaleX(view, 1.0f - position);
        ViewCompat.setAlpha(view, 1);
    }

}