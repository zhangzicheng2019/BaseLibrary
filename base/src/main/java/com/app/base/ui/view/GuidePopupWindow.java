package com.app.base.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;

import com.app.base.R;
import com.app.base.utils.UiUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by zzc on 17/7/13.
 */

public class GuidePopupWindow extends PopupWindow {

    public static final int TYPE_GUIDE_ARROW_TOP = 2;
    public static final int TYPE_GUIDE_ARROW_BOTTOM = 4;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({TYPE_GUIDE_ARROW_TOP, TYPE_GUIDE_ARROW_BOTTOM})
    public @interface GuideArrowType {
    }

    private View parent;
    private ImageView ivTopArrow;
    private ImageView ivBottomArrow;
    private long mDelayDismissTime = 3000L;
    private Context mContent;
    private int mGuideArrowType = TYPE_GUIDE_ARROW_TOP;

    private GuidePopupWindow() {
    }

    public GuidePopupWindow(Context context, @StringRes int contentResId) {
        this(context, contentResId, 0);
    }

    public GuidePopupWindow(Context context, @StringRes int contentResId, @StyleRes int resId) {
        this(context, contentResId, TYPE_GUIDE_ARROW_TOP, resId);
    }

    public GuidePopupWindow(Context context, @StringRes int contentResId, @GuideArrowType int guideArrowType, @StyleRes int resId) {
        super(context);
        mContent = context;
        mGuideArrowType = guideArrowType;
        View popView = LayoutInflater.from(mContent).inflate(R.layout.layout_pop_guide, null);
        parent = popView.findViewById(R.id.parent);
        ivTopArrow = popView.findViewById(R.id.iv_top_arrow);
        ivBottomArrow = popView.findViewById(R.id.iv_bottom_arrow);
        FrameLayout flContent = popView.findViewById(R.id.fl_content);
        if (mGuideArrowType == TYPE_GUIDE_ARROW_TOP) {
            ivTopArrow.setVisibility(View.VISIBLE);
            ivBottomArrow.setVisibility(View.GONE);
        } else if (mGuideArrowType == TYPE_GUIDE_ARROW_BOTTOM) {
            ivTopArrow.setVisibility(View.GONE);
            ivBottomArrow.setVisibility(View.VISIBLE);
        }
        TextView tvGuide = popView.findViewById(R.id.tv_guide);
        tvGuide.setText(contentResId);
        setContentView(popView);
        setBackgroundDrawable(new ColorDrawable());
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.AnimFadeInOutShort);

        final TypedArray ta = context.obtainStyledAttributes(resId, R.styleable.GuidePopupWindow);
        int textColor = ta.getColor(R.styleable.GuidePopupWindow_guideTextColor, 0);
        int arrowColor = ta.getColor(R.styleable.GuidePopupWindow_arrowColor, 0);
        int bgResId = ta.getResourceId(R.styleable.GuidePopupWindow_backgroundResource, 0);
        if(textColor != 0){
            tvGuide.setTextColor(textColor);
        }
        if(arrowColor != 0){
            if (mGuideArrowType == TYPE_GUIDE_ARROW_TOP) {
                UiUtils.setTintColor(ivTopArrow.getDrawable(), arrowColor);
            } else if (mGuideArrowType == TYPE_GUIDE_ARROW_BOTTOM) {
                UiUtils.setTintColor(ivBottomArrow.getDrawable(), arrowColor);
            }
        }
        if(bgResId != 0){
            flContent.setBackgroundResource(bgResId);
        }
        ta.recycle();
    }

    public void setArrowLocation(int gravity) {
        setArrowLocation(gravity, UiUtils.dpToPx(mContent, 12), 0, UiUtils.dpToPx(mContent, 12), 0);
    }

    public void setArrowLocation(int gravity, int marginLeft, int marginTop, int marginRight, int marginBottom) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = gravity;
        layoutParams.setMargins(marginLeft, marginTop, marginRight, marginBottom);
        if (mGuideArrowType == TYPE_GUIDE_ARROW_TOP) {
            ivTopArrow.setLayoutParams(layoutParams);
        } else if (mGuideArrowType == TYPE_GUIDE_ARROW_BOTTOM) {
            ivBottomArrow.setLayoutParams(layoutParams);
        }
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        if(isActivityRunning()){
            super.showAsDropDown(anchor, xoff, yoff);
            delayDismiss();
        }
    }

    /**
     * 此方法有坑，Android 4.3 及以下不存在这个方法
     */
    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff, int gravity) {
        if (isActivityRunning()) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                showAsDropDown(anchor, xoff, yoff);
            } else {
                super.showAsDropDown(anchor, xoff, yoff, gravity);
            }
            delayDismiss();
        }
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        if (isActivityRunning()) {
            super.showAtLocation(parent, gravity, x, y);
            delayDismiss();
        }
    }

    private void delayDismiss() {
        parent.postDelayed(new Runnable() {
            @Override
            public void run() {
                dismiss();
            }
        }, mDelayDismissTime);
    }

    public void setParentPadding(int left, int top, int right, int bottom) {
        parent.setPadding(left, top, right, bottom);
    }

    public void setDelayDismissTime(long delayDismissTime) {
        this.mDelayDismissTime = delayDismissTime;
    }

    @Override
    public void dismiss() {
       if(isActivityRunning()){
           super.dismiss();
       }
    }

    private boolean isActivityRunning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return mContent instanceof Activity && !((Activity) mContent).isDestroyed();
        } else {
            return mContent instanceof Activity && !((Activity) mContent).isFinishing();
        }
    }
}
