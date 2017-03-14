/*
 * Copyright 2017 Xuanyi Huang.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.allenxuan.xuanyihuang.littledownloadhelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

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
            downloadBinder.cancelDownload(true);
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
            downloadBinder.cancelDownload(false);
    }

}
