package com.app.base.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.app.base.ui.presenter.BasePresenter;
import com.app.base.ui.view.LoadingDialog;
import com.app.base.utils.UiUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseActivity<P extends BasePresenter> extends AppCompatActivity {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected P mPresenter;

    private LoadingDialog mLoadingDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int layoutId = getLayoutId();
        if(layoutId > 0){
            setContentView(layoutId);
        }
        Intent intent = getIntent();
        if(intent != null){
            handleIntent(intent);
        }
        mPresenter = initPresenter();
        if(mPresenter != null){
            getLifecycle().addObserver(mPresenter);
        }
        initView();
        initData();
    }

    protected abstract int getLayoutId();

    protected abstract void initView();

    protected void handleIntent(@NonNull Intent intent) {}

    protected void initData(){}

    protected P initPresenter(){
        return null;
    }

    public void disposeOnDestroy(@Nullable Disposable disposable) {
        if(disposable != null){
            mCompositeDisposable.add(disposable);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mPresenter != null){
            getLifecycle().removeObserver(mPresenter);
            mPresenter = null;
        }
        if(mCompositeDisposable != null){
            mCompositeDisposable.clear();
            mCompositeDisposable = null;
        }
        hideLoading();
    }

    public void showLoading(){
        showLoading(true);
    }

    public void showLoading(boolean cancelable){
        if(mLoadingDialog == null || !mLoadingDialog.isShowing()){
            mLoadingDialog = new LoadingDialog(this, cancelable);
            mLoadingDialog.show();
        }
    }

    public void hideLoading(){
        if(mLoadingDialog != null && mLoadingDialog.isShowing()){
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }
    public void showToastShort(String msg){
        UiUtils.showToastShort(this, msg);
    }

    public void showToastLong(String msg){
        UiUtils.showToastLong(this, msg);
    }

}

