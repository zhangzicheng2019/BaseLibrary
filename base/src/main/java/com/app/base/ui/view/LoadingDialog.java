package com.app.base.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.app.base.R;

public class LoadingDialog extends Dialog{

    public LoadingDialog(@NonNull Context context, boolean cancelable) {
        this(context, R.style.LoadingDialogTheme, cancelable);
    }

    public LoadingDialog(@NonNull Context context, int themeResId, boolean cancelable) {
        super(context, themeResId);
        setCancelable(cancelable);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
    }
}
