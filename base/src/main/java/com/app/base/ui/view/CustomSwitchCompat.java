package com.app.base.ui.view;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.SwitchCompat;

import com.app.base.utils.UiUtils;

/**
 * CustomSwitchCompat
 * <p>
 * Created by zzc on 17/10/19.
 */
public class CustomSwitchCompat extends SwitchCompat {

    private CheckedChangeCallback mCheckedChangeCallback;

    public void setCheckedChangeCallback(CheckedChangeCallback checkedChangeCallback) {
        this.mCheckedChangeCallback = checkedChangeCallback;
    }

    public CustomSwitchCompat(Context context) {
        super(context);
    }

    public CustomSwitchCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSwitchCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCheckedNotCallback(boolean checked) {
        super.setChecked(checked);
    }

    @Override
    public void setChecked(final boolean checked) {
        if (mCheckedChangeCallback != null && checked != isChecked()) {
            super.setChecked(checked);
            if (!mCheckedChangeCallback.call(checked)) {
                super.setChecked(!checked);
            }
        }
    }

    private long mLastUpdateSmsTimes = 0;

    @Override
    public void toggle() {
        if (System.currentTimeMillis() - mLastUpdateSmsTimes < 1000) {
            UiUtils.showToastShort(getContext(), "Your operation is too frequent! Please try again later.");
            return;
        }
        mLastUpdateSmsTimes = System.currentTimeMillis();
        setChecked(!isChecked());
    }

    public interface CheckedChangeCallback {
        /**
         * return false: Restore the state before switch
         * */
        boolean call(boolean checked);
    }
}
