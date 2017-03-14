package com.allenxuan.xuanyihuang.littledownloadhelper;

/**
 * Created by xuanyihuang on 08/03/2017.
 */

interface DownloadListener {
    void onProgress(int progress, float fileSize, String fileSizeUnit);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
