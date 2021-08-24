package com.hwadzan.ebook.lib;

import com.hwadzan.ebook.model.Book;

public class DownloadTask {
    public Long downloadId = -1L;
    public String downloadUrl = "";
    public int downloadedBytes = -1;
    public int totalBytes = -1;
    public int status = 0;
    public String localFileUri;

    public Book book;
}
