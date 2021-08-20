package com.hwadzan.ebook.lib;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadSerialQueue {
    public static final int HANDLE_DOWNLOAD = 0x001;

    private Context context;
    private MyDownloadListener downloadListener;
    private DownLoadBroadcast downLoadBroadcast = new DownLoadBroadcast();
    DownloadManager downloadManager;
    File parentDir;

    public DownloadSerialQueue(Context context, MyDownloadListener downloadListener, File parentDir) {
        this.context = context;
        this.downloadListener = downloadListener;
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downLoadBroadcast = new DownLoadBroadcast();
        this.parentDir = parentDir;
        registerBroadcast();
    }

    /**
     * 发送信息
     */
    private Handler downLoadHandler = new Handler(Looper.myLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            downloadListener.notifyMsg(msg);
        }
    };

    /**
     * 注册广播
     */
    private void registerBroadcast() {
        /**注册service 广播 1.任务完成时 2.进行中的任务被点击*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        context.registerReceiver(downLoadBroadcast, intentFilter);
    }

    /**
     * 注销广播
     */
    public void unregisterBroadcast() {
        if (downLoadBroadcast != null) {
            context.unregisterReceiver(downLoadBroadcast);
            downLoadBroadcast = null;
        }
    }

    /**
     * 接受下载完成广播
     */
    private class DownLoadBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            List<DownloadTask> downloadTaskList = getDownloadTaskStatus(downId);
            downLoadHandler.sendMessage(downLoadHandler.obtainMessage(HANDLE_DOWNLOAD, downloadTaskList.size(), 0, downloadTaskList));
        }
    }

    public List<DownloadTask> getDownloadTaskStatus() {
        return getDownloadTaskStatus(-1);
    }

    /**
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态
     * @param downloadId
     * @return
     */
    public List<DownloadTask> getDownloadTaskStatus(long downloadId) {
        Cursor cursor = null;
        List<DownloadTask> downloadTaskList = new ArrayList<>();
        try {
            if(downloadId>0) {
                DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
                cursor = downloadManager.query(query);
            } else {
                DownloadManager.Query query = new DownloadManager.Query();
                //query.setFilterByStatus(DownloadManager.STATUS_FAILED|DownloadManager.STATUS_PAUSED|DownloadManager.STATUS_SUCCESSFUL|DownloadManager.STATUS_RUNNING|DownloadManager.STATUS_PENDING);
                cursor = downloadManager.query(query);
            }
            while (cursor != null && cursor.moveToNext()){
                DownloadTask task = new DownloadTask();
                task.downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));

                task.downloadUrl = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI));
                //已经下载文件大小
                task.downloadedBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //下载文件的总大小
                task.totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //下载状态 大于等于1000 为出错
                task.status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));

                task.localFileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

                System.out.println(task.localFileUri);

                downloadTaskList.add(task);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return downloadTaskList;
    }

    /**
     * 比较实用的升级版下载功能
     *
     * @param url   下载地址
     * @param title 文件名字
     * @param desc  文件路径
     */
    public long download(String url, String title, String desc, File pdfFile) {

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // 仅允许在WIFI连接情况下下载
        // request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
        // 通知栏中将出现的内容
        request.setTitle(title);
        request.setDescription(desc);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);

        request.setShowRunningNotification(true);

        //7.0以上的系统适配
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            request.setRequiresDeviceIdle(false);
            request.setRequiresCharging(false);
        }

        //制定下载的文件类型为APK
        //request.setMimeType("application/vnd.android.package-archive");

        // 下载过程和下载完成后通知栏有通知消息。
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE | DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 指定下载文件地址，使用这个指定地址可不需要WRITE_EXTERNAL_STORAGE权限。
        request.setDestinationUri(Uri.fromFile(pdfFile));

        //大于11版本手机允许扫描
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //表示允许MediaScanner扫描到这个文件，默认不允许。
            request.allowScanningByMediaScanner();
        }

        return downloadManager.enqueue(request);
    }


    /**
     * 下载前先移除前一个任务，防止重复下载
     *
     * @param downloadId
     */
    public void stopDownloadTask(long downloadId) {
        try {
            downloadManager.remove(downloadId);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }


    /**
     * 关闭定时器，线程等操作
     */
    private void close() {
        if (downLoadHandler != null) {
            downLoadHandler.removeCallbacksAndMessages(null);
        }
    }

}
