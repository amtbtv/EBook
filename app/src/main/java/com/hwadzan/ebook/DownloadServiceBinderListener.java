package com.hwadzan.ebook;

import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookManager;

public interface DownloadServiceBinderListener {
    void downloaded(Book book);
    void downloadConfigStateChange(DownloadService.DownloadConfigState downloadConfigState);
    void notifyAdapterDataSetChanged();
}
