package com.allenxuan.xuanyihuang.littledownloadhelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by xuanyihuang on 09/03/2017.
 */

public class LittleDownloadHelper {
    private String downloadUrl = "";

    private Context serviceHostContext;
    private Class notificationTargetActivity;
    private int smallIconResId = -1;
    private int largeIconResId = -1;

    private boolean serviceConnectFlag = false;

    private DownloadService.DownloadBinder downloadBinder;

    private Intent serviceIntent;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            downloadBinder = (DownloadService.DownloadBinder) service;
            serviceConnectFlag = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    };

    public LittleDownloadHelper(){
    }

    public LittleDownloadHelper(Context context){
        serviceHostContext = context;
    }

    public LittleDownloadHelper initDownloadService(){
        return initDownloadService(serviceHostContext);
    }

    public LittleDownloadHelper initDownloadService(Context context){
        serviceHostContext = context;
        serviceIntent = new Intent(serviceHostContext, DownloadService.class);
        if(!serviceConnectFlag){
            serviceHostContext.startService(serviceIntent);
            serviceHostContext.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        }
        return this;
    }

    public void destroyDownloadService(){
            serviceHostContext.unbindService(serviceConnection);
            serviceHostContext.stopService(serviceIntent);
            downloadBinder.cancelDownload();
            downloadBinder = null;
            serviceConnectFlag = false;
    }

    public LittleDownloadHelper useNotificationTargetActivity(Class activity){
        notificationTargetActivity = activity;
        downloadBinder.useNotificationTargetActivity(notificationTargetActivity);
        return this;
    }

    public LittleDownloadHelper useNotificationSmallIcon(int resId){
        if(serviceConnectFlag){
            smallIconResId = resId;
            downloadBinder.useNotificationSmallIcon(smallIconResId);
        }
        return this;
    }

    public LittleDownloadHelper useNotificationLargeIcon(int resId){
        if(serviceConnectFlag){
            largeIconResId = resId;
            downloadBinder.useNotificationLargeIcon(largeIconResId);
        }
        return this;
    }

    public LittleDownloadHelper useCustomFileName(String fileName){
        if(serviceConnectFlag){
            downloadBinder.useCustomFileName(fileName);
        }
        return this;
    }

    public LittleDownloadHelper useDownloadProgressHintStyle(int style){
        if(serviceConnectFlag){
            downloadBinder.useDownloadProgressHintStyle(style);
        }
        return this;
    }

    public LittleDownloadHelper setDownloadUrl(String downloadUrlComeIn){
        downloadUrl = downloadUrlComeIn;
        return this;
    }

    public void startDownload(){
        startDownload(downloadUrl);
    }

    public void startDownload(String downloadUrlComeIn){
        downloadUrl = downloadUrlComeIn;
        if(serviceConnectFlag)
            downloadBinder.startDownload(downloadUrl);
    }

    public void pauseDownload(){
        if(serviceConnectFlag)
            downloadBinder.pauseDownload();
    }

    public void cancelDownload(){
        if(serviceConnectFlag)
            downloadBinder.cancelDownload();
    }

}
