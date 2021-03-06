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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;
import java.io.File;

/**
 * Created by xuanyihuang on 08/03/2017.
 */

public class DownloadService extends Service {
    private DownloadTask downloadTask;

    private String downloadUrl;

    private Long notificationCreateTime;

    private String inherentFileName = null;

    private String customFileName = null;

    private  DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress, float fileSize, String fileSizeUnit) {
            getNotificationManager().notify(1, getNotification(getExhibitedFileName(), progress, fileSize, fileSizeUnit));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            //stop foreground download notification
            stopForeground(true);
            //create a download-success notification
            getNotificationManager().notify(1, getNotification(getExhibitedFileName() +" (Download Success)"));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            //stop foreground download notification
            stopForeground(true);
            //create a download-failed notification
            getNotificationManager().notify(1, getNotification(getExhibitedFileName() + " (Download Failed)"));
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            getNotificationManager().notify(1, getNotification(getExhibitedFileName() + " (Download Paused)"));
            Toast.makeText(DownloadService.this, "Download Paused", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCanceled(boolean cancelAfterServiceDestroyed) {
            downloadTask = null;
            stopForeground(true);
            if(!cancelAfterServiceDestroyed)
                Toast.makeText(DownloadService.this, "Download Canceled", Toast.LENGTH_SHORT).show();
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();

    private Class notificationTargetActivity;
    private int notificationSmallIconResId = -1;
    private int notificationLargeIconResId = -1;

    private boolean useNotificationTargetActivity = false;
    private boolean useNotificationSmallIcon = false;
    private boolean useNotificationLargeIcon = false;
    private int downloadProgressHintStyle = DownloadProgressHintStyle.FRACTION_STYLE;


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    class DownloadBinder extends Binder{

        public void startDownload(String url){
            if(downloadTask == null){
                downloadUrl = url;
                inherentFileName= getInherentFileName(downloadUrl);
                downloadTask = new DownloadTask(downloadListener);
                downloadTask.execute(downloadUrl);
                notificationCreateTime = System.currentTimeMillis();
                startForeground(1, getNotification(getExhibitedFileName() + " (Downloading...)"));
                Toast.makeText(DownloadService.this,"Downloading...",Toast.LENGTH_SHORT).show();
            }
        }

        public void pauseDownload(){
            if(downloadTask != null)
                downloadTask.pauseDownload();
        }

        public void cancelDownload(boolean cancelAfterServiceDestroyed){
            if(downloadTask != null)
                downloadTask.cancelDownload(cancelAfterServiceDestroyed);
            else{
              //after paused, downloadTask is null
                if(downloadUrl != null  && !cancelAfterServiceDestroyed){
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if(file.exists())
                        file.delete();
//                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "Download Canceled", Toast.LENGTH_SHORT).show();
                }
            }
            resetNotificationConfig();
        }

        public void useNotificationTargetActivity(Class activity){
            notificationTargetActivity = activity;
            useNotificationTargetActivity = true;
        }

        public void useNotificationSmallIcon(int resourceId){
            notificationSmallIconResId = resourceId;
            useNotificationSmallIcon = true;
        }

        public void useNotificationLargeIcon(int resourceId){
            notificationLargeIconResId = resourceId;
            useNotificationLargeIcon = true;
        }

        public void useCustomFileName(String filename){
            customFileName = filename;
        }

        public void useDownloadProgressHintStyle(int style){
            if(style != DownloadProgressHintStyle.FRACTION_STYLE
                    && style != DownloadProgressHintStyle.PERCENT_STYLE
                    && style != DownloadProgressHintStyle.FRACTION_AND_PERCENT_TOGETHER)
                downloadProgressHintStyle = DownloadProgressHintStyle.FRACTION_STYLE;
            else
                downloadProgressHintStyle = style;
        }

        public void setAsyncTaskNull(){
            if(downloadTask != null)
                downloadTask = null;
        }

    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }


    private Notification getNotification(String title){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if(useNotificationTargetActivity  && notificationTargetActivity != null) {
            Intent intent = new Intent(this, notificationTargetActivity);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentIntent(pi);
        }
        if(useNotificationSmallIcon){
            builder.setSmallIcon(notificationSmallIconResId);
        }
        if(useNotificationLargeIcon){
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), notificationLargeIconResId));
        }
        builder.setContentTitle(title);
        builder.setWhen(notificationCreateTime);
        return  builder.build();
    }

    private Notification getNotification(String title, int progress, float fileSize, String fileSizeUnit){
        String stringFileSize = String.format("%.2f", fileSize);
        String stringDownloadedFileSize = String.format("%.2f", fileSize * progress / 100);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        if(useNotificationTargetActivity  && notificationTargetActivity != null) {
            Intent intent = new Intent(this, notificationTargetActivity);
            PendingIntent pi = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentIntent(pi);
        }
        if(useNotificationSmallIcon){
            builder.setSmallIcon(notificationSmallIconResId);
        }
        if(useNotificationLargeIcon){
            builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), notificationLargeIconResId));
        }
        builder.setContentTitle(title);
        if(downloadProgressHintStyle == DownloadProgressHintStyle.FRACTION_STYLE){
            builder.setContentText(stringDownloadedFileSize + fileSizeUnit + "/" + stringFileSize+ fileSizeUnit);
            if(progress > 0)
                builder.setProgress(100, progress, false);
        }else if(downloadProgressHintStyle == DownloadProgressHintStyle.PERCENT_STYLE){
            if(progress > 0) {
                builder.setContentText(progress + "%");
                builder.setProgress(100, progress, false);
            }
        }else if(downloadProgressHintStyle == DownloadProgressHintStyle.FRACTION_AND_PERCENT_TOGETHER){
            StringBuffer stringBuffer = new StringBuffer(stringDownloadedFileSize + fileSizeUnit + "/" + stringFileSize + fileSizeUnit);
            if(progress > 0){
                stringBuffer.append(" (" + progress + "%)");
                builder.setProgress(100, progress, false);
            }
            builder.setContentText(stringBuffer.toString());
        }
        builder.setWhen(notificationCreateTime);
        return  builder.build();
    }

    private void resetNotificationConfig(){
        notificationTargetActivity = null;
        useNotificationTargetActivity = false;
        notificationSmallIconResId = -1;
        useNotificationSmallIcon = false;
        notificationLargeIconResId = -1;
        useNotificationLargeIcon = false;
    }

    private String getInherentFileName(String url){
        String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
        return fileName.substring(1);
    }

    private String getExhibitedFileName(){
        if(customFileName != null)
            return  customFileName;
        else
            return inherentFileName;
    }
}
