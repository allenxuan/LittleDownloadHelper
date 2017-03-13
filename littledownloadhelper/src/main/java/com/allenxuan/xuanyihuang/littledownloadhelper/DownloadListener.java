package com.allenxuan.xuanyihuang.littledownloadhelper;

/**
 * Created by xuanyihuang on 08/03/2017.
 */

interface DownloadListener {
    void onProgress(int progress);

    void onSuccess();

    void onFailed();

    void onPaused();

    void onCanceled();
}
