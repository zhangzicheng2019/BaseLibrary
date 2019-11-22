package com.app.base.ui.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.app.base.ui.activity.BaseActivity;
import com.app.base.ui.presenter.BasePresenter;
import com.app.base.utils.UiUtils;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public abstract class BaseFragment<P extends BasePresenter> extends Fragment {

    private CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    protected FragmentActivity mActivity;

    protected P mPresenter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutId(), container, false);
        mPresenter = initPresenter();
        if(mPresenter != null){
            getLifecycle().addObserver(mPresenter);
        }
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView(getView());
        initData();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
            mActivity = (FragmentActivity) activity;
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (FragmentActivity) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity = null;
    }

    protected abstract int getLayoutId();

    protected abstract void initView(View view);

    protected P initPresenter(){
        return null;
    }

    protected void initData(){}

    public void showLoading(){
        showLoading(true);
    }

    public void showLoading(boolean cancelable){
        if (!isAdded()) {
            return;
        }
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).showLoading(cancelable);
        }
    }

    public void hideLoading(){
        if (!isAdded()) {
            return;
        }
        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).hideLoading();
        }
    }

    public void disposeOnDestroy(Disposable disposable) {
        if(disposable != null){
            mCompositeDisposable.add(disposable);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mPresenter != null){
            getLifecycle().removeObserver(mPresenter);
            mPresenter = null;
        }
        if(mCompositeDisposable != null){
            mCompositeDisposable.dispose();
            mCompositeDisposable = null;
        }
        hideLoading();
    }

    public void showToastShort(String msg){
        UiUtils.showToastShort(getContext(), msg);
    }

    public void showToastLong(String msg){
        UiUtils.showToastLong(getContext(), msg);
    }
}
