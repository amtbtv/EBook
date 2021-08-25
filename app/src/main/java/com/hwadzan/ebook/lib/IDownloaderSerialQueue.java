package com.hwadzan.ebook.lib;

import com.hwadzan.ebook.model.Book;

import java.io.File;

public interface IDownloaderSerialQueue {
    void close();
    long download(Book b);
    void init(MyDownloadListener downloadListener, File parentDir);
}
