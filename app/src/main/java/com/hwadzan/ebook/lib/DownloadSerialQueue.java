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

import com.hwadzan.ebook.model.Book;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DownloadSerialQueue {
    public static final int HANDLE_DOWNLOAD = 0x001;

    private Context context;
    private MyDownloadListener downloadListener;
    private DownLoadBroadcast downLoadBroadcast = new DownLoadBroadcast();
    DownloadManager downloadManager;
    File parentDir;
    Timer mTimer;
    TimerTask mTimerTask;

    HashMap<Long, DownloadTask> downloadTaskHashMap;

    public DownloadSerialQueue(Context context, MyDownloadListener downloadListener, File parentDir) {
        this.context = context;
        this.downloadListener = downloadListener;
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downLoadBroadcast = new DownLoadBroadcast();
        this.parentDir = parentDir;
        registerBroadcast();
        downloadTaskHashMap = new HashMap<>();
        mTimer = new Timer();

    }



    /**
     * 在download函数中启动，如果已经启动就不再启动
     */
    private void startTimer(){
        if (mTimer == null) {
            mTimer = new Timer();
        }

        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    //这里定时查询，查询所有任务
                    List<DownloadTask> downloadTaskList = getDownloadTaskStatus(-1);
                    if(downloadTaskList.size()==0){
                        stopTimer();
                    } else {
                        //任务有二、更新状态，通知UI更新
                        if(downloadListener!=null)
                            downloadListener.notifyMsg(downloadTaskList);
                    }
                }
            };
        }
        mTimer.schedule(mTimerTask, 100, 1500);//延迟0.1秒开始运行，每隔1.5秒运行一次
    }

    /**
     * 在每次循环中检测，如果所有任务都已经下载完成，则停止循环
     */
    private void stopTimer(){
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

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

    public void addExistsTask(DownloadTask task) {
        downloadTaskHashMap.put(task.downloadId, task);
        if(mTimer == null){
            startTimer();
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
            stopDownloadTask(downId);
            downloadTaskHashMap.remove(downId);
            if(downloadTaskHashMap.size()==0){
                stopTimer();
            }
            for(DownloadTask t : downloadTaskList){
                t.book.downloaded = true;
            }
            if(downloadListener!=null) downloadListener.notifyMsg(downloadTaskList);
        }
    }

    /**
     * 下载系统中所有正在下载的任务，仅在下载服务初始化时用到，返回的DownloadTask中Book为null
     * @return
     */
    public List<DownloadTask> getAllDownloadTaskStatus() {
        Cursor cursor = null;
        List<DownloadTask> downloadTaskList = new ArrayList<>();
        try {
            DownloadManager.Query query = new DownloadManager.Query();
            cursor = downloadManager.query(query);
            while (cursor != null && cursor.moveToNext()) {
                DownloadTask task = new DownloadTask();
                task.downloadId = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                task.downloadUrl = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_URI));
                task.downloadedBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                task.totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                task.status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                task.localFileUri = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));
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
     * 通过query查询下载状态，包括已下载数据大小，总大小，下载状态，仅返回本APP创建的下载任务
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
                cursor = downloadManager.query(query);
            }
            while (cursor != null && cursor.moveToNext()){
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_ID));
                if(downloadTaskHashMap.containsKey(id)){ //如果是其它的程序建立的下载任务就不管它们了
                    DownloadTask task = downloadTaskHashMap.get(id);
                    task.downloadedBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    task.totalBytes = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    task.status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                    downloadTaskList.add(task);
                }
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
     * @param b   下载地址
     * @param pdfFile  文件路径
     */
    public long download(Book b, File pdfFile) {
        String url = b.url;
        String title = b.fabo_title;
        String desc = b.fabo_content;
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

        long id = downloadManager.enqueue(request);
        DownloadTask task = new DownloadTask();
        task.book = b;
        task.downloadUrl = url;
        task.downloadId = id;
        //task.status = D
        task.localFileUri = pdfFile.getAbsolutePath();
        downloadTaskHashMap.put(id, task);

        if(mTimer == null){
            startTimer();
        }

        return id;
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
    }

}
