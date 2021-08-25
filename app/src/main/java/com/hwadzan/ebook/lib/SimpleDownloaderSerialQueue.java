package com.hwadzan.ebook.lib;

import android.app.DownloadManager;

import com.hwadzan.ebook.model.Book;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SimpleDownloaderSerialQueue implements IDownloaderSerialQueue {
    private MyDownloadListener downloadListener;
    File parentDir;
    Timer mTimer;
    TimerTask mTimerTask;
    List<SimpleDownloader> downloaderList = new ArrayList<>();
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
                    List<DownloadTask> downloadTaskList = new ArrayList<>();
                    Iterator<SimpleDownloader> iterator = downloaderList.iterator();
                    while (iterator.hasNext()){
                        SimpleDownloader downloader = iterator.next();
                        DownloadTask task = downloader.getTask();
                        downloadTaskList.add(downloader.getTask());
                        if(task.book.downloaded){
                            iterator.remove();
                        }
                    }
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

    @Override
    public void close() {
        if(mTimer!=null){
            stopTimer();
        }
    }

    @Override
    public long download(Book b) {
        File tmpFile = new File(parentDir, b.fileName + ".tmp");
        if(tmpFile.exists())
            tmpFile.delete();
        File pdfFile =  new File(parentDir, b.fileName);
        if(pdfFile.exists())
            pdfFile.delete();
        SimpleDownloader downloader = new SimpleDownloader(b, tmpFile, pdfFile);
        downloader.downloadFile();
        downloaderList.add(downloader);
        if(mTimer==null){
            startTimer();
        }
        return downloader.getTask().downloadId;
    }

    @Override
    public void init(MyDownloadListener downloadListener, File parentFile) {
        this.downloadListener = downloadListener;
        this.parentDir = parentFile;
    }
}
