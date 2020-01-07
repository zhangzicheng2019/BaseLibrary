package com.app.base.common.listener;

import android.app.Dialog;
import android.content.DialogInterface;
import android.view.ViewTreeObserver;
import android.view.Window;

import com.app.base.utils.LogUtils;

/**
 * @ClassName DialogClickListener
 * @Description 防止HanderThread 内存泄露
 * @Author zhangzicheng
 * @Date 2020/1/7 14:20
 */

public final class DialogClickListener implements DialogInterface.OnClickListener {

    public static DialogClickListener wrap(DialogInterface.OnClickListener onClickListener) {
        return new DialogClickListener(onClickListener);
    }

    private DialogInterface.OnClickListener onClickListener;

    private DialogClickListener(DialogInterface.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    @Override public void onClick(DialogInterface dialog, int which) {
        if (onClickListener != null) {
            onClickListener.onClick(dialog, which);
        }
    }

    public void recycleOnDetach(Dialog dialog) {
        Window window = dialog.getWindow();
        if(window != null){
            window.getDecorView()
                    .getViewTreeObserver()
                    .addOnWindowAttachListener(new ViewTreeObserver.OnWindowAttachListener() {
                        @Override public void onWindowAttached() { }
                        @Override public void onWindowDetached() {
                            onClickListener = null;
                            LogUtils.d("DialogClickObserver --> onWindowDetached");
                        }
                    });
        }
    }
}
