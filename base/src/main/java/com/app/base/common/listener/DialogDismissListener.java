package com.app.base.common.listener;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.app.base.utils.LogUtils;

public class DialogDismissListener implements DialogInterface.OnDismissListener {

    public static DialogDismissListener wrap(DialogInterface.OnDismissListener onDismissListener) {
        return new DialogDismissListener(onDismissListener);
    }

    private DialogInterface.OnDismissListener onDismissListener;

    private DialogDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }

    public DialogDismissListener recycleOnDetach(Dialog dialog) {
        Window window = dialog.getWindow();
        if(window != null){
            window.getDecorView()
                    .getViewTreeObserver()
                    .addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
                        @Override public void onWindowAttached() { }
                        @Override public void onWindowDetached() {
                            onDismissListener = null;
                            window.getDecorView().getViewTreeObserver().removeOnWindowAttachListener(this);
                            LogUtils.d("DialogDismissListener --> onWindowDetached");
                        }
                    });
        }
        return this;
    }

}
