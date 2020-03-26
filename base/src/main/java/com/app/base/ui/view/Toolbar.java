package com.app.base.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import android.os.Build;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.base.R;
import com.app.base.utils.UiUtils;

public class Toolbar extends FrameLayout {

    private ImageView ivNav;
    private TextView tvTitle;
    private Activity mRootActivity;

    private OnClickListener mNavOnClickListener;

    public Toolbar(@NonNull Context context) {
        this(context, null);
    }

    public Toolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Toolbar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, R.layout.layout_toolbar);
    }

    public Toolbar(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @LayoutRes int layout) {
        super(context, attrs, defStyleAttr);
        inflate(context, layout, this);

        ivNav = findViewById(R.id.iv_nav);
        tvTitle = findViewById(R.id.tv_title);

        if (context instanceof Activity) {
            mRootActivity = (Activity) context;
        } else if (context instanceof ContextWrapper) {
            //Since Calligraphy library will wrap our activity's context. We need to unwrap once.
            //https://github.com/chrisjenx/Calligraphy
            Context contextTemp = ((ContextWrapper) context).getBaseContext();
            if (contextTemp instanceof Activity) {
                mRootActivity = (Activity) contextTemp;
            }
        }

        ivNav.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mNavOnClickListener != null){
                    mNavOnClickListener.onClick(v);
                } else if(mRootActivity != null){
                    mRootActivity.onBackPressed();
                }
            }
        });

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Toolbar);
            String title = a.getString(R.styleable.Toolbar_titleText);
            ColorStateList tintColorList = a.getColorStateList(R.styleable.Toolbar_tintColor);
            int icon  = a.getResourceId(R.styleable.Toolbar_navIcon, -1);
            int navIconPadding  = a.getDimensionPixelSize(R.styleable.Toolbar_navIconPadding, -1);
            int titleColor  = a.getColor(R.styleable.Toolbar_titleColor, -1);
            float titleSize  = a.getDimension(R.styleable.Toolbar_titleSize, -1);
            boolean showNavIcon  = a.getBoolean(R.styleable.Toolbar_showNavIcon, true);
            boolean showShadowLine  = a.getBoolean(R.styleable.Toolbar_showShadowLine, false);
            if(showNavIcon){
                if(icon != -1){
                    ivNav.setImageResource(icon);
                }
                ivNav.setVisibility(View.VISIBLE);
            } else {
                ivNav.setVisibility(View.GONE);
            }
            if(navIconPadding > 0){
                ivNav.setPadding(navIconPadding, 0, navIconPadding, 0);
            }

            tvTitle.setText(title);
            if(tintColorList != null){
                setChildTintColor(tintColorList);
            }

            if(titleColor != -1){
                tvTitle.setTextColor(titleColor);
            }

            if(titleSize != -1){
                tvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
            }

            if(showShadowLine){
                if (Build.VERSION.SDK_INT >= 21){
                    setElevation(UiUtils.dpToPx(context, 1f));
                } else {
                    findViewById(R.id.v_shadow_line).setVisibility(VISIBLE);
                }
            } else {
                findViewById(R.id.v_shadow_line).setVisibility(GONE);
            }

            a.recycle();
        }


    }

    public void setTitle(String title){
        tvTitle.setText(title);
    }

    public void setTitle(@StringRes int stringRes){
        tvTitle.setText(stringRes);
    }

    public void setNavOnClickListener(OnClickListener onClickListener){
        mNavOnClickListener = onClickListener;
    }

    public void setNavDrawable(Drawable navDrawable){
        ivNav.setImageDrawable(navDrawable);
    }

    public void setNavDrawable(@DrawableRes int drawableRes){
        ivNav.setImageResource(drawableRes);
    }

    public void setChildTintColor(@NonNull ColorStateList tintColorList){
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(child.getVisibility() == View.GONE){
                continue;
            }
            if(child instanceof ImageView){
                UiUtils.setTintColor(((ImageView) child).getDrawable(), tintColorList);
            } else if(child instanceof TextView){
                ((TextView) child).setTextColor(tintColorList);
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

    }
}
