package com.app.base.common;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.app.base.utils.LogUtils;
import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PermissionHolder {

    private RxPermissions mPermissions;
    private Context mContext;
    private CompositeDisposable mCompositeDisposable;

    private PermissionHolder() {
    }

    public PermissionHolder(@NonNull FragmentActivity activity) {
        mContext = activity;
        mPermissions = new RxPermissions(activity);
    }

    public PermissionHolder(@NonNull Fragment fragment) {
        mContext = fragment.getContext();
        mPermissions = new RxPermissions(fragment);
    }

    public void request(final PermissionResultCallback permissionResultCallback, String... permissions) {
        if (mPermissions == null) {
            LogUtils.e("RxPermissions is cancel!");
            return;
        }
        Disposable disposable = mPermissions.request(permissions).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (!aBoolean) {
                    showSettingPermissionDialog();
                }
                if (permissionResultCallback != null) {
                    permissionResultCallback.callback(aBoolean);
                }
            }
        });
        if(mCompositeDisposable == null){
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    public void cancel() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
            mCompositeDisposable = null;
        }
        if (mPermissions != null) {
            mPermissions = null;
        }
    }

    /**
     * 显示提示对话框
     */
    private void showSettingPermissionDialog() {
        if (mContext == null) {
            return;
        }
        new AlertDialog.Builder(mContext)
                .setTitle("提示信息")
                .setMessage("当前应用缺少必要权限，该功能暂时无法使用。如若需要，请单击【确定】按钮前往设置中心进行授权。")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        startAppSettings();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    /**
     * 启动当前应用设置页面
     */
    private void startAppSettings() {
        if (mContext != null) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + mContext.getPackageName()));
            mContext.startActivity(intent);
        }
    }

    public interface PermissionResultCallback {
        void callback(boolean result);
    }
}
