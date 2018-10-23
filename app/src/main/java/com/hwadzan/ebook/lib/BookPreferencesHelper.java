package com.hwadzan.ebook.lib;

import android.content.Context;

import com.hwadzan.ebook.model.Book;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class BookPreferencesHelper {
    SharedPreferencesHelper helper;
    public BookPreferencesHelper(Context context) {
        helper = new SharedPreferencesHelper(context, "book");
    }

    public Book getBook(String serialId) {
        return helper.getSharedPreference(serialId, Book.class);
    }

    public List<Book> getAll() {
        Map<String, Book> bookMap = helper.getAll(Book.class);
        List<Book> bookList = new ArrayList<>();
        bookList.addAll(bookMap.values());
        Collections.sort(bookList, comparator);
        return bookList;
    }

    public static Comparator<Book> comparator = new Comparator<Book>() {
        @Override
        public int compare(Book o1, Book o2) {
            return Long.compare(o2.lastReadTime, o1.lastReadTime);
        }
    };

    public void save(Book b) {
        helper.put(b.fabo_serial, b);
    }

    public void remove(Book book) {
        helper.remove(book.fabo_serial);
    }
}
