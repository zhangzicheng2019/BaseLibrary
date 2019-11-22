package com.app.base.ui.view;

import io.reactivex.disposables.Disposable;

public interface IBaseView {

    void showLoading(boolean showLoading);

    void hideLoading();

    void disposeOnDestroy(Disposable disposable);

    void showToastShort(String msg);

    void showToastLong(String msg);

}
