package com.hwadzan.ebook;

public class Constants {
    public static final String CATEGORY_URL = "http://edu.amtb.de/ibook_catalog";
    public static final String HOST_URL = "http://edu.amtb.de/ibook_download_hosts";
    //http://edu.amtb.de/ibook_download_hosts
    public static String Make_BOOKS_URL(int aid){
        return "http://edu.amtb.de/ibook_data/" + String.valueOf(aid);
    }

    /*
    public static String Make_DOWNLOAD_BOOK_URL(String server, String path, String type, String fabo_serial){
        //return "https://" + server + path + type + "/" + fabo_serial + "." + type;
        return "https://vod.amtb.de/redirect/fabo/" + type + "/" + fabo_serial + "." + type;
    }
    */

    public static String Make_DOWNLOAD_BOOK_URL(String type, String fabo_serial){
        //return "https://" + server + path + type + "/" + fabo_serial + "." + type;
        return "http://vod.amtb.de/redirect/fabo/" + type + "/" + fabo_serial + "." + type;
    }
}
