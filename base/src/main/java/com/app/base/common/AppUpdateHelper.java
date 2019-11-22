package com.app.base.common;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.app.base.R;
import com.app.base.utils.LogUtils;

import java.io.File;

public class AppUpdateHelper {

    private static final String TAG = AppUpdateHelper.class.getSimpleName();

    //下载器
    private DownloadManager mDownloadManager;
    private Context mContext;
    //下载的ID
    private long mApkDownloadId = -1;
    private String mApkName;
    private boolean downloadComplete = false;

    public AppUpdateHelper(Context context, String apkName) {
        this.mContext = context;
        this.mApkName = apkName;
    }

    //下载apk
    public void download(String apkUrl) {
        //已经下载完成，直接安装
       if(downloadComplete){
           installAPK();
           return;
       }
        removeApkFile();

        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apkUrl));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle(mContext.getString(R.string.app_name));
        request.setDescription("新版本下载中...");
        request.setVisibleInDownloadsUi(true);
        //设置下载路径
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, mApkName);

        //获取DownloadManager
        if (mDownloadManager == null){
            mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        }

        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        if (mDownloadManager != null) {
            mApkDownloadId = mDownloadManager.enqueue(request);
        }

        LogUtils.i(TAG, "mApkDownloadId=" + mApkDownloadId + ", request=" + request.toString());

        //注册广播接收者，监听下载状态
        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                checkStatus();
            }
        };
        mContext.registerReceiver(mReceiver,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private BroadcastReceiver mReceiver;
    private void unregisterReceiver() {
        if (mReceiver != null && mContext != null) {
            mContext.unregisterReceiver(mReceiver);
            mReceiver = null;
        }
    }

    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(mApkDownloadId);
        Cursor cursor = mDownloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    //下载完成安装APK
                    downloadComplete = true;
                    installAPK();
                    cursor.close();
                    unregisterReceiver();
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    //失败原因
                    int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));
                    Toast.makeText(mContext, "下载失败:" + reason, Toast.LENGTH_SHORT).show();
                    cursor.close();
                    mDownloadManager.remove(mApkDownloadId);
                    break;
            }
        }
    }

    private void installAPK() {
        File apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), mApkName);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // 由于没有在Activity环境下启动Activity,设置下面的标签
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //Android 7.0以上要使用FileProvider
        if (Build.VERSION.SDK_INT >= 24) {
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            Uri apkUri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".fileprovider", apkFile);
            //添加这一句表示对目标应用临时授权该Uri所代表的文件
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        mContext.startActivity(intent);
    }


    public void removeApkFile(){
        if(mApkDownloadId != -1){
            mDownloadManager.remove(mApkDownloadId);
        }
        unregisterReceiver();
    }

}