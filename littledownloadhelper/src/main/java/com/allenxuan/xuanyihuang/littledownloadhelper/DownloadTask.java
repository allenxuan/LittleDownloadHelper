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

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by xuanyihuang on 08/03/2017.
 */

class DownloadTask extends AsyncTask<String, Integer, Integer> {
    private final int TYPE_SUCCESS = 0;
    private final int TYPE_FAILED = 1;
    private final int TYPE_PAUSED = 2;
    private final int TYPE_CANCELED = 3;

    private DownloadListener listener;

    private boolean isCanceled = false;

    private boolean cancelAfterServiceDestroyed = false;

    private boolean isPaused = false;

    private int lastProgress;

    private float fileSize;

    private String fileSizeUnit;

    DownloadTask(DownloadListener listener){
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Integer doInBackground(String... params) {
        InputStream is = null;
        RandomAccessFile savedFile = null;
        File file = null;
        try {
            long downloadedLength = 0; // length of downloaded file part.
            String downloadUrl = params[0]; // url of file to be downloaded.
            String fileName = downloadUrl.substring(downloadUrl.lastIndexOf("/"));
            String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
            file = new File(directory + fileName);
            if(file.exists()){
                downloadedLength = file.length();
            }
            long contentLength = getContentLength(downloadUrl);
            if(contentLength == 0){
                return TYPE_FAILED;
            }
            if(contentLength == downloadedLength){
                return TYPE_SUCCESS;
            }
            setFileSizeAndUnit(contentLength);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    //breakpoint download
                    .addHeader("RANGE", "bytes=" + downloadedLength + "-")
                    .url(downloadUrl)
                    .build();
            Response response = client.newCall(request).execute();
            if(response != null){
                is = response.body().byteStream();
                savedFile = new RandomAccessFile(file, "rw");
                savedFile.seek(downloadedLength);
                byte[] b = new byte[1024];
                int total = 0;
                int len;
                while ((len = is.read(b)) != -1){
                    if(isCanceled) {
                        return TYPE_CANCELED;
                    }else if(isPaused){
                        return TYPE_PAUSED;
                    }else {
                        total += len;
                        savedFile.write(b, 0, len);
                        // calculate download percentage
                        int progress = (int) ((total + downloadedLength) * 100 / contentLength);
                        publishProgress(progress);
                    }
                }
                response.body().close();
                return TYPE_SUCCESS;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                if(is != null){
                    is.close();
                }
                if(savedFile != null){
                    savedFile.close();
                }
                if(isCanceled && file !=null && !cancelAfterServiceDestroyed){
                    file.delete();
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return TYPE_FAILED;
    }

    @Override
    protected void onPostExecute(Integer status) {
        switch (status){
            case TYPE_SUCCESS:
                listener.onSuccess();
                break;
            case TYPE_FAILED:
                listener.onFailed();
                break;
            case TYPE_PAUSED:
                listener.onPaused();
                break;
            case TYPE_CANCELED:
                listener.onCanceled(cancelAfterServiceDestroyed);
                cancelAfterServiceDestroyed = false;
                break;
            default:
                break;
        }
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        int progress = values[0];
        if(progress > lastProgress){
            listener.onProgress(progress, fileSize, fileSizeUnit);
            lastProgress = progress;
        }
    }

    public void pauseDownload(){
        isPaused = true;
    }

    public void cancelDownload(boolean b){
        isCanceled = true;
        cancelAfterServiceDestroyed = b;
    }

    private long getContentLength(String downloadUrl) throws IOException{
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(downloadUrl)
                .build();
        Response response = client.newCall(request).execute();
        if(response != null && response.isSuccessful()){
            long contentLength = response.body().contentLength();
            response.close();
            return  contentLength;
        }
        return  0;
    }

    private void setFileSizeAndUnit(long contentLength){
        if(contentLength / 1024 == 0){
            fileSize = contentLength;
            fileSizeUnit = "B";
        }
        else if((contentLength / 1024 > 0) && (contentLength / 1024 / 1024 == 0)){
            fileSize = (float) (contentLength / 1024.0);
            fileSizeUnit = "K";
        }
        else if((contentLength / 1024 / 1024 > 0) && (contentLength / 1024 / 1024 / 1024 == 0)){
            fileSize = (float) (contentLength / 1024.0 / 1024);
            fileSizeUnit = "M";
        }
        else if(contentLength / 1024 / 1024 / 1024 > 0){
            fileSize = (float) (contentLength / 1024.0 / 1024 / 1024);
            fileSizeUnit = "G";
        }
    }
}
