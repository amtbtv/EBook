package com.hwadzan.ebook.lib;

import android.app.DownloadManager;

import com.hwadzan.ebook.R;
import com.hwadzan.ebook.model.Book;

public class DownloadTask {

    public enum Status{
        PENDING,RUNNING,PAUSED,SUCCESSFUL,FAILED,UNKNOW
    }

    public Long downloadId = -1L;
    public String downloadUrl = "";
    public long downloadedBytes = -1;
    public long totalBytes = -1;
    public Status status = Status.PENDING;
    public String localFileUri;

    public Book book;
}
