package com.hwadzan.ebook.lib;

import android.content.Context;

import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookMark;

public class BookMarkPreferencesHelper {
    SharedPreferencesHelper helper;
    public BookMarkPreferencesHelper(Context context) {
        helper = new SharedPreferencesHelper(context, "bookmark");
    }

    public BookMark getBookMark(String fileName) {
        return helper.getSharedPreference(fileName, BookMark.class);
    }

    public void save(String fileName, BookMark bookMark) {
        helper.put(fileName, bookMark);
    }
}
