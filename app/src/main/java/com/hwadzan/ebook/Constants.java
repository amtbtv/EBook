package com.hwadzan.ebook;

public class Constants {
    public static final String CATEGORY_URL = "http://edu.hwadzan.com/ibook_catalog";
    public static final String HOST_URL = "http://edu.hwadzan.com/ibook_download_hosts";
    public static final String BOOK_URL = "http://edu.hwadzan.com/ibook_data/%d";

    public static String Make_BOOKS_URL(int aid){
        return "http://edu.hwadzan.com/ibook_data/" + String.valueOf(aid);
    }

    public static String Make_DOWNLOAD_BOOK_URL(String server, String path, String type, String fabo_serial){
        return "http://" + server + path + type + "/" + fabo_serial + "." + type;
    }
}
