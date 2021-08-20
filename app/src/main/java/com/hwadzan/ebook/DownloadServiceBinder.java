package com.hwadzan.ebook;

import android.os.Binder;

import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookManager;

import java.io.File;

public class DownloadServiceBinder extends Binder {
    DownloadService downloadService;
    public DownloadServiceBinder(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    public void downloadOver(){

    }

    public File getBookPdfFile(Book book){
        return downloadService.getBookPdfFile(book);
    }

    public void removeBook(Book book){

    }

    public void setDownloadServiceBinderListener(DownloadServiceBinderListener downloadServiceBinderListener) {
        downloadService.setDownloadServiceBinderListener(downloadServiceBinderListener);
    }

    public void deleteBook(Book book) {
        downloadService.deleteBook(book);
    }

    public void downNewBook(Book b) {
        downloadService.downNewBook(b);
    }

    public BookManager getBookManager() {
        return downloadService.getBookManager();
    }

    public DownloadService.DownloadConfigState getDownloadedConfigState() {
        return downloadService.downloadConfigState;
    }
}
