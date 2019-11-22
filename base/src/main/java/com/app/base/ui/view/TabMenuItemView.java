package com.app.base.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.base.R;

public class TabMenuItemView extends RelativeLayout {

    private TextView tvBadge;
    private ImageView ivBadge;

    public TabMenuItemView(Context context) {
        super(context);
        init(context, null);
    }

    public TabMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TabMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.layout_tab_menu_item, this);
        setGravity(Gravity.BOTTOM);
        setClipChildren(false);
        ImageView iv = findViewById(R.id.iv);
        TextView tv = findViewById(R.id.tv);
        tvBadge = findViewById(R.id.tv_badge);
        ivBadge = findViewById(R.id.iv_badge);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TabMenuItemView);
            Drawable drawable = a.getDrawable(R.styleable.TabMenuItemView_tabMenuIcon);
            String text = a.getString(R.styleable.TabMenuItemView_tabMenuText);
            ColorStateList colorStateList = a.getColorStateList(R.styleable.TabMenuItemView_tabMenuTextColor);
            if (drawable != null) {
                iv.setImageDrawable(drawable);
            }
            if (text != null) {
                tv.setText(text);
            }
            if(colorStateList != null){
                tv.setTextColor(colorStateList);
            }
            a.recycle();
        }
    }

    @Override
    public void setOnClickListener(final OnClickListener onClickListener) {
        OnClickListener clickListenerWrapper = new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(v);
                }
                setSelected();
            }
        };
        super.setOnClickListener(clickListenerWrapper);
    }

    public void setBadge(int badgeNumber) {
        if (badgeNumber > 0) {
            tvBadge.setVisibility(VISIBLE);
            tvBadge.setText(String.valueOf(badgeNumber));
        } else {
            tvBadge.setVisibility(GONE);
        }
    }

    public void setBadgeDrawable(Drawable drawable) {
        if (drawable != null) {
            ivBadge.setImageDrawable(drawable);
            ivBadge.setVisibility(VISIBLE);
        } else {
            ivBadge.setImageDrawable(null);
            ivBadge.setVisibility(GONE);
        }
    }

    public void setBadgeWithRedCircle(boolean withRedCircle) {
        setBadgeDrawable(withRedCircle ? ContextCompat.getDrawable(getContext(), R.drawable.bg_circle_red) : null);
    }

    public void setSelected() {
        setSelected(true);
        ViewGroup parent = (ViewGroup) getParent();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View sibling = parent.getChildAt(i);
            if (!sibling.equals(this)) {
                sibling.setSelected(false);
            }
        }
    }

    public boolean isBadgeVisible() {
        return ivBadge.getVisibility() == View.VISIBLE;
    }
}
