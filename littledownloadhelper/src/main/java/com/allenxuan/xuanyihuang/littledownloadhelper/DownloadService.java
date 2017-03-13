package com.allenxuan.xuanyihuang.littledownloadhelper;

import android.app.Activity;
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

public class DownloadService extends Service {
    private DownloadTask downloadTask;

    private String downloadUrl;

    private Long notificationCreateTime;

    private String inherentFileName = null;

    private String customFileName = null;

    private  DownloadListener downloadListener = new DownloadListener() {
        @Override
        public void onProgress(int progress) {
            getNotificationManager().notify(1, getNotification(getExhibitedFileName() + " (Downloading...)", progress));
        }

        @Override
        public void onSuccess() {
            downloadTask = null;
            //stop foreground download notification
            stopForeground(true);
            //create a download-success notification
            getNotificationManager().notify(1, getNotification(getExhibitedFileName() +" (Download Success)", -1));
            Toast.makeText(DownloadService.this, "Download Success", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onFailed() {
            downloadTask = null;
            //stop foreground download notification
            stopForeground(true);
            //create a download-failed notification
            getNotificationManager().notify(1, getNotification(getExhibitedFileName() + " (Download Failed)", -1));
            Toast.makeText(DownloadService.this, "Download Failed", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onPaused() {
            downloadTask = null;
            getNotificationManager().notify(1, getNotification(getExhibitedFileName() + " (Download Paused)", -1));
            Toast.makeText(DownloadService.this, "Paused", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCanceled() {
            downloadTask = null;
            stopForeground(true);
            Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_LONG).show();
        }
    };

    private DownloadBinder mBinder = new DownloadBinder();

    private Class notificationTargetActivity;
    private int notificationSmallIconResId = -1;
    private int notificationLargeIconResId = -1;

    private boolean useNotificationTargetActivity = false;
    private boolean useNotificationSmallIcon = false;
    private boolean useNotificationLargeIcon = false;


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
                startForeground(1, getNotification(getExhibitedFileName() + " (Downloading...)",0));
                Toast.makeText(DownloadService.this,"Downloading...",Toast.LENGTH_LONG).show();
            }
        }

        public void pauseDownload(){
            if(downloadTask != null)
                downloadTask.pauseDownload();
        }

        public void cancelDownload(){
            if(downloadTask != null)
                downloadTask.cancelDownload();
            else{
              //after paused, downloadTask is null
                if(downloadUrl != null){
                    String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
                    String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                    File file = new File(directory + fileName);
                    if(file.exists())
                        file.delete();
//                    getNotificationManager().cancel(1);
                    stopForeground(true);
                    Toast.makeText(DownloadService.this, "Canceled", Toast.LENGTH_SHORT).show();
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

        public void setAsyncTaskNull(){
            if(downloadTask != null)
                downloadTask = null;
        }

    }

    private NotificationManager getNotificationManager(){
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    private Notification getNotification(String title, int progress){
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
        if(progress > 0){
            builder.setContentText(progress + "%");
            builder.setProgress(100, progress, false);
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
