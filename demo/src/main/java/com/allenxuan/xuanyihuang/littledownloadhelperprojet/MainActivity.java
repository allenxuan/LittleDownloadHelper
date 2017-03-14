package com.allenxuan.xuanyihuang.littledownloadhelperprojet;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.allenxuan.xuanyihuang.littledownloadhelper.DownloadProgressHintStyle;
import com.allenxuan.xuanyihuang.littledownloadhelper.LittleDownloadHelper;

public class MainActivity extends AppCompatActivity {

    private LittleDownloadHelper littleDownloadHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        littleDownloadHelper = new LittleDownloadHelper();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case  1:
                if(grantResults.length>0 && grantResults[0] != PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(MainActivity.this, "Permission Denied", Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
            default:
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void initService(View v){
        littleDownloadHelper.initDownloadService(this);
    }

    public void startDownload(View v){
        String url = "http://dl.hdslb.com/mobile/latest/iBiliPlayer-bili.apk";

        littleDownloadHelper
        .setDownloadUrl(url)
                .useNotificationSmallIcon(R.drawable.ic_notification)
                .useNotificationTargetActivity(MainActivity.class)
                .useDownloadProgressHintStyle(DownloadProgressHintStyle.FRACTION_AND_PERCENT_TOGETHER)
                .startDownload();
    }

    public void pauseDownload(View v){
        littleDownloadHelper.pauseDownload();
    }

    public void cancelDownload(View v){
        littleDownloadHelper.cancelDownload();
    }

    public void destroyDownloadService(View v){
        littleDownloadHelper.destroyDownloadService(this);
    }


}
