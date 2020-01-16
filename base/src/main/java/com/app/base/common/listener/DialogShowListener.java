package com.app.base.common.listener;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.app.base.utils.LogUtils;

public class DialogShowListener implements DialogInterface.OnShowListener {

    public static DialogShowListener wrap(DialogInterface.OnShowListener onShowListener) {
        return new DialogShowListener(onShowListener);
    }

    private DialogInterface.OnShowListener onShowListener;

    private DialogShowListener(DialogInterface.OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        if (onShowListener != null) {
            onShowListener.onShow(dialog);
        }
    }

    public DialogShowListener recycleOnDetach(Dialog dialog) {
        Window window = dialog.getWindow();
        if(window != null){
            window.getDecorView()
                    .getViewTreeObserver()
                    .addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
                        @Override public void onWindowAttached() { }
                        @Override public void onWindowDetached() {
                            onShowListener = null;
                            window.getDecorView().getViewTreeObserver().removeOnWindowAttachListener(this);
                            LogUtils.d("DialogShowListener --> onWindowDetached");
                        }
                    });
        }
        return this;
    }
}
