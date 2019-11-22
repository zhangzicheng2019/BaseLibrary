package com.app.base.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.*;
import android.text.InputType;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.app.base.R;
import com.app.base.utils.LogUtils;
import com.app.base.utils.UiUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * AuthCodeInputView
 * <p>
 * Created by zzc on 17/6/23.
 */
public class AuthCodeInputView extends View implements View.OnClickListener, View.OnKeyListener {

    public static final int INPUT_STATE_ERROR = -1;
    public static final int INPUT_STATE_LOSE_FOCUS = 0;
    public static final int INPUT_STATE_NORMAL = 1;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({INPUT_STATE_ERROR, INPUT_STATE_NORMAL, INPUT_STATE_LOSE_FOCUS})
    public @interface InputState {
    }

    /**
     * 输入框个数
     */
    private int mBoxCount = 6;
    /**
     * 单个输入框的宽度
     */
    private int mBoxWidth;
    /**
     * 单个输入框的高度
     */
    private int mBoxHeight;
    /**
     * 文本大小
     */
    private int mTextSize;
    /**
     * 文本颜色
     */
    private int mTextColor;
    /**
     * 圆点颜色
     */
    private int mDotColor;
    /**
     * 光标颜色
     */
    private int mCursorColor;
    /**
     * 边框间距
     */
    private int mBoxSpace;
    /**
     * 边框颜色
     */
    private int mBoxColor;
    /**
     * 圆点半径
     */
    private int mDotRadius;

    private InputNumbersListener mInputNumbersListener;
    private InputCompleteListener mInputCompleteListener;
    private StringBuilder mInputNumber = new StringBuilder();

    private InputMethodManager mInputMethodManager;
    private Paint mPaint = new Paint();
    private TextPaint mTextPaint = new TextPaint();

    private float mTextBaseY = 0f;
    private int mInputState = INPUT_STATE_LOSE_FOCUS;

    public AuthCodeInputView(@NonNull Context context) {
        this(context, null);
    }

    public AuthCodeInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AuthCodeInputView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (null != attrs) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AuthCodeInputView);
            mBoxCount = typedArray.getInteger(R.styleable.AuthCodeInputView_boxCount, 6);
            mTextColor = typedArray.getColor(R.styleable.AuthCodeInputView_textColor,
                    ContextCompat.getColor(context, R.color.colorBlue));
            mDotRadius = typedArray.getDimensionPixelSize(R.styleable.AuthCodeInputView_dotRadius, UiUtils.dpToPx(context, 4));
            mDotColor = typedArray.getColor(R.styleable.AuthCodeInputView_dotColor,
                    ContextCompat.getColor(context, R.color.transparent));
            mCursorColor = typedArray.getColor(R.styleable.AuthCodeInputView_cursorColor,
                    ContextCompat.getColor(context, R.color.colorBlue));
            mBoxWidth = typedArray.getDimensionPixelSize(R.styleable.AuthCodeInputView_boxWidth, 0);
            mBoxSpace = typedArray.getDimensionPixelSize(R.styleable.AuthCodeInputView_boxSpace, 0);
            mBoxHeight = typedArray.getDimensionPixelSize(R.styleable.AuthCodeInputView_boxHeight, 0);
            mBoxColor = typedArray.getColor(R.styleable.AuthCodeInputView_boxColor, ContextCompat.getColor(context, R.color.divider_dark));
            mTextSize = typedArray.getDimensionPixelSize(R.styleable.AuthCodeInputView_textSize, 0);
            typedArray.recycle();
        }
        init(context);
    }

    private void init(Context context) {
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(UiUtils.dpToPx(context, 1));
        mPaint.setStyle(Paint.Style.STROKE);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setColor(mTextColor);
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setFocusableInTouchMode(true);
        setOnClickListener(this);
        setOnKeyListener(this);
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecModel = MeasureSpec.getMode(widthMeasureSpec);
        int heightSpecModel = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width = 0;
        int height = 0;
        switch (widthSpecModel) {
            case MeasureSpec.EXACTLY:
                width = widthSize + mBoxSpace * (mBoxCount - 1);
                mBoxWidth = widthSize / mBoxCount + mBoxSpace;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                width = mBoxCount * mBoxWidth + mBoxSpace * (mBoxCount - 1);
                break;
        }
        switch (heightSpecModel) {
            case MeasureSpec.EXACTLY:
                mBoxHeight = heightSize;
                height = heightSize;
                break;
            case MeasureSpec.AT_MOST:
            case MeasureSpec.UNSPECIFIED:
                height = mBoxHeight;
                break;
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cursorIndex = mInputNumber.length();
        drawUnderline(canvas, cursorIndex);
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        float fontHeight = fontMetrics.bottom - fontMetrics.top;
        if (mTextBaseY == 0)
            mTextBaseY = getHeight() - (getHeight() - fontHeight) / 2 - fontMetrics.bottom;
        int y = (int) mTextBaseY;

        if (!TextUtils.isEmpty(mInputNumber)) {
            if (mInputNumber.length() > mBoxCount) {
                mInputNumber.delete(mBoxCount, mInputNumber.length() - 1);
            }
            for (int i = 0; i < mInputNumber.length(); i++) {
                canvas.drawText("" + mInputNumber.charAt(i),
                        (mBoxWidth + mBoxSpace) * i + (mBoxWidth / 2),
                        y, mTextPaint);
            }
        }
        mPaint.setColor(mDotColor);
        for (int i = mInputNumber.length(); i < mBoxCount; i++) {
            if (mInputState != INPUT_STATE_LOSE_FOCUS) {
                if (i != cursorIndex) {
                    canvas.drawCircle((mBoxWidth + mBoxSpace) * i + (mBoxWidth / 2),
                            mBoxHeight / 2, mDotRadius, mPaint);
                }
            } else {
                canvas.drawCircle((mBoxWidth + mBoxSpace) * i + (mBoxWidth / 2),
                        mBoxHeight / 2, mDotRadius, mPaint);
            }
        }
    }

    /**
     * 绘制输入框
     *
     * @param canvas Canvas
     */
    private void drawUnderline(Canvas canvas, int cursorIndex) {
        int underlineColor;
        if (mInputState == INPUT_STATE_ERROR) {
            underlineColor = ContextCompat.getColor(getContext(), R.color.colorRed);
        } else {
            underlineColor = mBoxColor;
        }
        for (int i = 0; i < mBoxCount; i++) {
            if (mInputState != INPUT_STATE_LOSE_FOCUS) {
                if (cursorIndex == i) {
                    mPaint.setColor(mCursorColor);
                } else {
                    mPaint.setColor(underlineColor);
                }
            } else {
                mPaint.setColor(underlineColor);
            }
            canvas.drawRect((mBoxWidth + mBoxSpace) * i, 0,
                    (mBoxWidth + mBoxSpace) * i + mBoxWidth, mBoxHeight, mPaint);
        }
    }

    @Override
    public void onClick(View v) {
        showSoftInput();
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        if(gainFocus){
            setState(INPUT_STATE_NORMAL);
            showSoftInput();
        } else {
            setState(INPUT_STATE_LOSE_FOCUS);
        }
        LogUtils.e("gainFocus=" + gainFocus);
    }

    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) {
            return false;
        }
        int keyCode = keyEvent.getKeyCode();
        if (keyCode == KeyEvent.KEYCODE_DEL && mInputNumber.length() > 0) {
            mInputNumber.deleteCharAt(mInputNumber.length() - 1);
            if (mInputNumbersListener != null) {
                mInputNumbersListener.inputNumbers(mInputNumber.toString());
            }
            setState(INPUT_STATE_NORMAL);
            //7-16就是数字0-9
        } else if (keyCode >= 7 && keyCode <= 16 && mInputNumber.length() < mBoxCount) {
            mInputNumber.append(keyCode - 7);
            if (mInputNumbersListener != null) {
                mInputNumbersListener.inputNumbers(mInputNumber.toString());
            }
            if (mInputNumber.length() == mBoxCount && mInputCompleteListener != null) {
                mInputCompleteListener.inputComplete(mInputNumber.toString());
            }
            setState(INPUT_STATE_NORMAL);
        }
        return false;
    }


    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        outAttrs.inputType = InputType.TYPE_CLASS_NUMBER;//定义软键盘样式为数字键盘
        return super.onCreateInputConnection(outAttrs);
    }

    public void setInputNumbersListener(InputNumbersListener inputNumbersListener) {
        this.mInputNumbersListener = inputNumbersListener;
    }

    public void setInputCompleteListener(InputCompleteListener inputCompleteListener) {
        this.mInputCompleteListener = inputCompleteListener;
    }

    public void setState(@InputState int inputState) {
        this.mInputState = inputState;
        invalidate();
    }

    public void setTextTypeface(Typeface typeface) {
        mTextPaint.setTypeface(typeface);
    }

    /**
     * 获取当前已输入文本
     *
     * @return String
     */
    public String getInputNumber() {
        return mInputNumber.toString();
    }

    /**
     * 设置当前显示验证码
     *
     * @param inputNumber String
     */
    public void setInputNumber(String inputNumber) {
        if (!TextUtils.isEmpty(this.mInputNumber)) {
            this.mInputNumber.delete(0, mInputNumber.length());
        }
        if (!TextUtils.isEmpty(inputNumber)) {
            if (inputNumber.length() > mBoxCount) {
                inputNumber.substring(0, mBoxCount);
            }
            this.mInputNumber.append(inputNumber);
        }
        mInputState = INPUT_STATE_NORMAL;
        invalidate();
    }

    /**
     * 打开输入法
     */
    public void showSoftInput() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mInputMethodManager != null) {
                    mInputMethodManager.showSoftInput(AuthCodeInputView.this,
                            InputMethodManager.SHOW_FORCED);
                    setState(INPUT_STATE_NORMAL);
                }
            }
        }, 200);
    }

    /**
     * 关闭输入法
     */
    public void hideSoftInput() {
        if (mInputMethodManager != null && getWindowToken() != null) {
            mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        hideSoftInput();
    }

    public interface InputCompleteListener {

        void inputComplete(String numbers);
    }

    public interface InputNumbersListener {

        void inputNumbers(String numbers);
    }
}

