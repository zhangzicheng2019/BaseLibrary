package com.app.base.ui.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.AccelerateInterpolator;
import android.widget.SeekBar;

import androidx.annotation.Nullable;

import com.app.base.utils.LogUtils;

@SuppressLint("AppCompatCustomView")
public class WebViewSeekBar extends SeekBar {
    private static final String TAG = "WebViewSeekBar";

    private ValueAnimator mCurrentAnimator;

    public WebViewSeekBar(Context context) {
        this(context, null);
    }

    public WebViewSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WebViewSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void start() {
        LogUtils.d(TAG, "start()");
        setVisibility(VISIBLE);
        startFrom0To20();
    }

    public void end() {
        if (isEnd()) {
            return;
        }

        LogUtils.d(TAG, "end()");
        setVisibility(VISIBLE);
        cancelAnimation();
        OnAnimationEndListener listener = new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                setVisibility(GONE);
            }
        };
        animProgress(getProgress(), getMax(), 500, listener);
    }

    public boolean isEnd() {
        return getProgress() >= getMax();
    }

    public void release() {
        cancelAnimation();
    }

    // 500ms, 0-20%
    private void startFrom0To20() {
        LogUtils.d(TAG, "startFrom0To20()");
        cancelAnimation();
        OnAnimationEndListener listener = new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                startFrom20To60();
            }
        };
        animProgress(0, (int) (getMax() * 0.2), 500, listener);
    }

    // 20%/1000ms, 20%-60%
    private void startFrom20To60() {
        LogUtils.d(TAG, "startFrom20To60()");
        cancelAnimation();
        OnAnimationEndListener listener = new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                startFrom60To80();
            }
        };
        animProgress((int) (getMax() * 0.2), (int) (getMax() * 0.6), 1000 * 2, listener);
    }

    // 5%/1000ms, 60%-80%
    private void startFrom60To80() {
        LogUtils.d(TAG, "startFrom60To80()");
        cancelAnimation();
        OnAnimationEndListener listener = new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                startFrom80To90();
            }
        };
        animProgress((int) (getMax() * 0.6), (int) (getMax() * 0.8), 1000 * 4, listener);
    }

    // 1%/1000ms, 80%-90%
    private void startFrom80To90() {
        LogUtils.d(TAG, "startFrom80To90()");
        cancelAnimation();
        OnAnimationEndListener listener = new OnAnimationEndListener() {
            @Override
            public void onAnimationEnd() {
                // stop at 90% util end
                LogUtils.d(TAG, "startFrom80To90() onAnimationEnd()");
            }
        };
        animProgress((int) (getMax() * 0.8), (int) (getMax() * 0.9), 1000 * 10, listener);
    }

    private void animProgress(int minProgress, int maxProgress, int duration,
                              @Nullable final OnAnimationEndListener listener) {
        mCurrentAnimator = ValueAnimator.ofObject(new IntEvaluator(), minProgress, maxProgress);
        mCurrentAnimator.setDuration(duration);
        mCurrentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = ((Number) animation.getAnimatedValue()).intValue();
                setProgress(value);
            }
        });
        mCurrentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (listener != null) {
                    listener.onAnimationEnd();
                }
            }
        });
        mCurrentAnimator.setInterpolator(new AccelerateInterpolator());
        mCurrentAnimator.start();
    }

    private void cancelAnimation() {
        if (mCurrentAnimator != null) {
            LogUtils.d(TAG, "cancelAnimation()");
            mCurrentAnimator.removeAllListeners();
            mCurrentAnimator.cancel();
        }
    }

    private interface OnAnimationEndListener {
        void onAnimationEnd();
    }
}
