package com.hwadzan.ebook;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

import com.google.gson.Gson;
import com.hwadzan.ebook.lib.DownloadTask;
import com.hwadzan.ebook.lib.IDownloaderSerialQueue;
import com.hwadzan.ebook.lib.MyDownloadListener;
import com.hwadzan.ebook.lib.SimpleDownloaderSerialQueue;
import com.hwadzan.ebook.lib.StringResult;
import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookManager;
import com.hwadzan.ebook.model.ibook_config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DownloadService extends Service {

    public enum DownloadConfigState{
        NoStart, NoNetWork, Downloading, Fail, Sucess
    }

    public static final String TAG = "DownloadService";

    File parentFile;
    IDownloaderSerialQueue serialQueue;
    BookManager bookManager;
    BookApplication app;
    DownloadServiceBinderListener downloadServiceBinderListener;

    public BookManager getBookManager() {
        return bookManager;
    }
    /**
     * 0 未下载默认， 1 正在下载, 2 下载出错,  3 下载成功
     */
    public DownloadConfigState downloadConfigState = DownloadConfigState.NoStart;

    @Override
    public void onCreate() {
        super.onCreate();

        app =(BookApplication) getApplication();
        bookManager = new BookManager(this);
        
        String type = "pdf";
        File fileDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            fileDir = getExternalFilesDir(type);
            if(fileDir==null)
                fileDir = getFilesDir();
        } else {
            fileDir = getFilesDir();
        }

        parentFile = new File(fileDir, "book");
        if (!parentFile.exists())
            parentFile.mkdirs();

        serialQueue = new SimpleDownloaderSerialQueue();
        serialQueue.init(downloadListener, parentFile);

        if (app.isNetworkConnected()) {
            downloadConfigState = DownloadConfigState.Downloading; //开始下载，正在下载中
            //真正的开始下载配制文件
            String urls[] = {
                    Constants.IBOOK_CONFIG_URL_TW,
                    Constants.IBOOK_CONFIG_URL_CN
            };

            app.http.asyncTakeFastStringResult(urls, downConfigCallback);
        } else {
            downloadConfigState = DownloadConfigState.NoNetWork;
        }
    }

    MyDownloadListener downloadListener = new MyDownloadListener() {
        @Override
        public void notifyMsg(List<DownloadTask> downloadTaskList) {
            if (downloadTaskList!=null && downloadTaskList.size() > 0) {
                for (DownloadTask task : downloadTaskList) {
                    if(task.book.downloaded){
                        bookManager.saveBook(task.book);
                    } else {
                        if(task.status == DownloadTask.Status.RUNNING) {
                            if (task.downloadedBytes > 0 && task.totalBytes > 0) {
                                if (task.downloadedBytes >= task.totalBytes) {
                                    task.book.downloaProcess = "100%";
                                } else {
                                    task.book.downloaProcess = String.valueOf((int) ((double) task.downloadedBytes / task.totalBytes * 100)) + "%";
                                }
                            }
                        } else if(task.status == DownloadTask.Status.SUCCESSFUL) {
                            task.book.downloaProcess = "100%";
                        } else if(task.status == DownloadTask.Status.PAUSED) {
                            task.book.downloaProcess = getString(R.string.paused);
                        } else if(task.status == DownloadTask.Status.FAILED) {
                            task.book.downloaProcess = getString(R.string.download_error);
                        } else if(task.status == DownloadTask.Status.PENDING) {
                            task.book.downloaProcess = getString(R.string.download_pending);
                        }
                    }
                }
                //引发事件才对
                downloadServiceBinderListener.notifyAdapterDataSetChanged();
            }
        }
    };



    /**
     * 开启下载一本书的任务，并不通知更新UI
     * @param b
     */
    protected void dwonBook(Book b){
        b.downloadId = serialQueue.download(b);
        b.downloaProcess = getString(R.string.downloading);
        bookManager.saveBook(b);
    }

    /**
     * 下载配制信息的回调函数
     */
    StringResult downConfigCallback = new StringResult() {
        @Override
        public void takeStringResult(String json) {
            if (json == null) { //失败
                downloadConfigState = DownloadConfigState.Fail; //下载出错
                downloadServiceBinderListener.downloadConfigStateChange(downloadConfigState);
            } else { //成功
                ibook_config config = new Gson().fromJson(json, ibook_config.class);

                if (config == null || config.domain == null) {
                    downloadConfigState = DownloadConfigState.Fail; //下载出错
                    downloadServiceBinderListener.downloadConfigStateChange(downloadConfigState);
                } else {
                    Constants.domain = config.domain;
                    Constants.download = config.download;
                    downloadConfigState = DownloadConfigState.Sucess; //下载成功

                    List<Book> bookList = new ArrayList<>();
                    //开始下载未下载完成的电子图书
                    for (Book b : bookManager.getBookList()) {
                        File pdfFile = getBookPdfFile(b);
                        // 这里是为了防止清空缓存之类的操作，把电子书都删除了。默认情况下已下载电子的downloaded=true
                        if (!pdfFile.exists()) {
                            b.downloaded = false;
                            bookManager.saveBook(b);
                            bookList.add(b);
                        }
                    }

                    downloadServiceBinderListener.downloadConfigStateChange(downloadConfigState);

                    for (Book b : bookList) {
                        File pdfFile = getBookPdfFile(b);
                        if(!b.downloaded && !pdfFile.exists()){
                            dwonBook(b);
                        }
                    }
                    downloadServiceBinderListener.notifyAdapterDataSetChanged();
                }
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        DownloadServiceBinder binder = new DownloadServiceBinder(this);
        return binder;
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(bookManager.isDownOver()){
            Intent stopIntent = new Intent(this, DownloadService.class);
            stopService(stopIntent);
        }

        serialQueue.close();
    }

    public void setDownloadServiceBinderListener(DownloadServiceBinderListener downloadServiceBinderListener) {
        this.downloadServiceBinderListener = downloadServiceBinderListener;
        if(downloadServiceBinderListener != null){
            downloadServiceBinderListener.downloadConfigStateChange(downloadConfigState);
        }
    }

    public File getBookPdfFile(Book book) {
        return new File(parentFile, book.fileName);
    }

    public void deleteBook(Book book) {
        File file = getBookPdfFile(book);
        if (file.exists())
            file.delete();
        bookManager.removeBook(book);
    }

    public void downNewBook(Book b) {
        //下载新书
        if (bookManager.addNewBook(b)) {
            //开启新的下载
            dwonBook(b);
            downloadServiceBinderListener.notifyAdapterDataSetChanged();
        }
    }
}