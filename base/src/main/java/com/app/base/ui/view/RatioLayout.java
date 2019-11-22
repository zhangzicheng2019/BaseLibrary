package com.app.base.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.app.base.R;

public class RatioLayout extends FrameLayout {

    private final static float DEFAULT_WIDTH = 4.0f;
    private final static float DEFAULT_HEIGHT = 3.0f;
    private float mRatioWidth;
    private float mRatioHeight;

    public RatioLayout(Context context) {
        super(context);
        init(context, null);
    }

    public RatioLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public RatioLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.RatioLayout);
        mRatioWidth = a.getFloat(R.styleable.RatioLayout_ratioWidth, DEFAULT_WIDTH);
        mRatioHeight = a.getFloat(R.styleable.RatioLayout_ratioHeight, DEFAULT_HEIGHT);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        int calculatedHeight = (int) (originalWidth * mRatioHeight / mRatioWidth);

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(originalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(calculatedHeight, MeasureSpec.EXACTLY));
    }

    public void setRatioAspect(float ratioWidth, float ratioHeight) {
        this.mRatioWidth = ratioWidth;
        this.mRatioHeight = ratioHeight;
        requestLayout();
    }
}

