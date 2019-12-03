package com.app.base.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;

import com.app.base.R;

import java.nio.charset.Charset;

/**
 * 带有删除按钮的EditText并可限制输入lenght和表情符, 默认不限制
 *
 * Created by zhangzicheng on 2016/11/14.
 */
public class ClearEditText extends AppCompatEditText implements TextWatcher {

    private static final int DRAWABLE_LEFT = 0;
    private static final int DRAWABLE_TOP = 1;
    private static final int DRAWABLE_RIGHT = 2;
    private static final int DRAWABLE_BOTTOM = 3;
    private Drawable mClearDrawable;
    private int maxLength = -1;
    private boolean allowEmoticon = false;

    public ClearEditText(Context context) {
        super(context);
        init(context, null);
    }

    public ClearEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public ClearEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray ta = ctx.obtainStyledAttributes(attrs, R.styleable.ClearEditText);
            maxLength = ta.getInt(R.styleable.ClearEditText_clearMaxLength, -1);
            allowEmoticon = ta.getBoolean(R.styleable.ClearEditText_clearAllowEmoticon, false);
            int clearDrawableId = ta.getResourceId(R.styleable.ClearEditText_clearDrawable, -1);
            if(clearDrawableId > 0){
                mClearDrawable = ContextCompat.getDrawable(ctx, clearDrawableId);
            }
            ta.recycle();
        }
        if(!allowEmoticon){
            setFilters(new InputFilter[]{new EmojiExcludeFilter()});
        }
        addTextChangedListener(this);

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        setClearIconVisible(hasFocus() && text.length() > 0);
    }

    @Override
    public void afterTextChanged(Editable s) {
        if(maxLength == -1 || s == null){
           return;
        }

        String ss = s.toString().trim();
        if(ss.length() > maxLength){
            setTextKeepState(ss.substring(0, maxLength));
        }
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        setClearIconVisible(focused && length() > 0);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Drawable drawable = getCompoundDrawables()[DRAWABLE_RIGHT];
            if (drawable != null && event.getX() <= (getWidth() - getPaddingRight())
                    && event.getX() >= (getWidth() - getPaddingRight() - drawable.getBounds().width())) {
                setText("");
            }
        }
        return super.onTouchEvent(event);
    }

    private void setClearIconVisible(boolean visible) {
        if(mClearDrawable != null){
            setCompoundDrawablesWithIntrinsicBounds(getCompoundDrawables()[DRAWABLE_LEFT], getCompoundDrawables()[DRAWABLE_TOP],
                    visible ? mClearDrawable : null, getCompoundDrawables()[DRAWABLE_BOTTOM]);
        }
    }

    /**
     * 设置不允许输入表情符
     *
     */
    public void setNotAllowEmoticon(){
        allowEmoticon = false;
        setFilters(new InputFilter[]{new EmojiExcludeFilter()});
    }

    /**
     * 添加搜索icon
     *
     * @param drawable icon
     */
    public void setSearchDrawable(Drawable drawable){
        setCompoundDrawables(drawable, null, null, null);
    }

    /**
     * 判断字符串长度(utf-8编码)是否超过限制
     *
     * @return String
     */
     private String getLimitSubstring(String inputStr) {
        int orignLen = inputStr.length();
        int resultLen = 0;
        String temp = null;
        for (int i = 0; i < orignLen; i++) {
            temp = inputStr.substring(i, i + 1);
            if (temp.getBytes(Charset.forName("UTF-8")).length == 3) {
                resultLen += 2;
            } else {
                resultLen++;
            }
            if (resultLen > maxLength) {
                return inputStr.substring(0, i);
            }
        }
        return null;
    }

    /**
     * 防止内存泄漏
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeTextChangedListener(this);
    }

    public static class EmojiExcludeFilter implements InputFilter {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                int type = Character.getType(source.charAt(i));
//                Log.e("EmojiExcludeFilter", "type:" + type + "source.charAt(i):" + source.charAt(i));
                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    }

    public static class SimpleTextChangedListener implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    }
}