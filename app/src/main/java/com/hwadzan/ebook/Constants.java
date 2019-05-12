package com.hwadzan.ebook;

import java.util.Locale;

public class Constants {

    public static final String Upgrade_Url = "https://amtbapi.amtb.cn/upgradeEBook.json";

    public static final String IBOOK_CONFIG_URL_CN = "https://amtbapi.amtb.cn/ibook_config";
    public static final String IBOOK_CONFIG_URL_TW = "https://amtbapi.amtb.de/ibook_config";

    public static String domain = ""; //https://amtbapi.amtb.de";
    public static String download = ""; //"http://vod.amtb.de/redirect";

    //http://edu.amtb.de/ibook_download_hosts
    public static String Make_BOOKS_URL(int aid){
        return domain + "/ibook_data/" + String.valueOf(aid);
    }

    public static String CATEGORY_URL(){
        return domain + "/ibook_catalog";
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
