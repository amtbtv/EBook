package com.hwadzan.ebook.model;

import android.content.Context;

import com.hwadzan.ebook.lib.BookPreferencesHelper;
import com.hwadzan.ebook.lib.GlideApp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BookManager {

    Context context;
    BookPreferencesHelper bookPreferencesHelper;
    List<Book> bookList;

    public BookManager(Context context){
        this.context = context;
        bookPreferencesHelper = new BookPreferencesHelper(context);
        bookList = bookPreferencesHelper.getAll();
    }

    public Book takeBookByDownloadUrl(String url) {
        for (Book b : bookList)
            if (b.url.equals(url))
                return b;
        return null;
    }

    public void saveBook(Book book){
        bookPreferencesHelper.save(book);
    }

    public void sort(){
        Collections.sort(bookList, BookPreferencesHelper.comparator);
    }

    public List<Book> getBookList() {
        return bookList;
    }

    public void removeBook(Book book) {
        bookList.remove(book);
        bookPreferencesHelper.remove(book);
    }

    public boolean addNewBook(Book b) {
        boolean found = false;
        for (Book bb : bookList) {
            if (bb.fabo_serial.equals(b.fabo_serial)) {
                found = true;
            }
        }
        if (!found) {
            bookList.add(0, b);
            bookPreferencesHelper.save(b);
            return true;
        } else {
            return false;
        }
    }

    public boolean isDownOver() {
        boolean downover = true;
        for(Book b : bookList){
            if(!b.downloaded){
                downover = false;
                break;
            }
        }
        return downover;
    }
}
