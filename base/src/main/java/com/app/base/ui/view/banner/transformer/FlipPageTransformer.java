package com.app.base.ui.view.banner.transformer;

import androidx.core.view.ViewCompat;
import android.view.View;

public class FlipPageTransformer extends BannerPageTransformer {
    private static final float ROTATION = 180.0f;

    @Override
    public void handleInvisiblePage(View view, float position) {
    }

    @Override
    public void handleLeftPage(View view, float position) {
        ViewCompat.setTranslationX(view, -view.getWidth() * position);
        float rotation = (ROTATION * position);
        ViewCompat.setRotationY(view, rotation);

        if (position > -0.5) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void handleRightPage(View view, float position) {
        ViewCompat.setTranslationX(view, -view.getWidth() * position);
        float rotation = (ROTATION * position);
        ViewCompat.setRotationY(view, rotation);

        if (position < 0.5) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.INVISIBLE);
        }
    }

}