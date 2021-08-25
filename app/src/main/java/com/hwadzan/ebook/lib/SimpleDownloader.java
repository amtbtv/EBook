package com.hwadzan.ebook.lib;

import android.util.Log;

import com.hwadzan.ebook.model.Book;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SimpleDownloader {
    private static final String TAG = "SimpleDownloader";
    private static long _ID = 0;
    private String url;
    private File tmpFile;
    private File pdfFile;
    private int progress;
    private int status;
    private DownloadTask task;

    public SimpleDownloader(Book b, File tmpFile, File pdfFile){
        this.url = b.url;
        this.tmpFile = tmpFile;
        this.pdfFile = pdfFile;
        this.progress = 0;

        this.task = new DownloadTask();
        task.book = b;
        task.downloadUrl = url;
        task.localFileUri = pdfFile.getAbsolutePath();

        task.status = DownloadTask.Status.PENDING;

        _ID++;
        task.downloadId = _ID;
        task.book.downloadId = task.downloadId;
        //task.status = D
        task.localFileUri = pdfFile.getAbsolutePath();
    }

    public void downloadFile() {
        final long startTime = System.currentTimeMillis();
        Log.i(TAG,"startTime="+startTime);
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Connection", "close")
                .build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                Log.i(TAG,"download failed");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                task.status = DownloadTask.Status.RUNNING;
                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                try {
                    is = response.body().byteStream();
                    task.totalBytes = response.body().contentLength();
                    fos = new FileOutputStream(tmpFile);
                    task.downloadedBytes = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        task.downloadedBytes += len;
                        progress = (int) (task.downloadedBytes * 1.0f / task.totalBytes * 100);
                        task.book.downloaProcess = String.valueOf(progress)+"%";
                        Log.e(TAG,"download progress : " + progress);
                    }
                    fos.flush();
                    Log.e(TAG,"download success");
                    Log.e(TAG,"totalTime="+ (System.currentTimeMillis() - startTime));
                    task.status = DownloadTask.Status.SUCCESSFUL;
                } catch (Exception e) {
                    task.status = DownloadTask.Status.FAILED;
                    e.printStackTrace();
                    Log.e(TAG,"download failed : "+e.getMessage());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                    }
                }
                if(task.status == DownloadTask.Status.SUCCESSFUL){
                    tmpFile.renameTo(pdfFile);
                    task.book.downloaded = true;
                }
            }
        });
    }

    public DownloadTask getTask() {
        return task;
    }
}
