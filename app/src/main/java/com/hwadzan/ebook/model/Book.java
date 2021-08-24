package com.hwadzan.ebook.model;

import java.util.Date;

public class Book {

    //网络JSON数据
    public String img_s;
    public String img_b;
    public boolean epub;
    public boolean pdf;
    public boolean txt;
    public String fabo_serial;
    public String fabo_title;
    public String path;
    public String fabo_content;

    //保存时添加的属性
    public long lastReadTime;
    public boolean downloaded;
    public String downloaProcess;
    public String url;
    public String fileName;
    public Long downloadId = -1L;

    public Book(){
        downloaded = false;
        fileName = "";
        url = "";
        downloaProcess = "%0";
        lastReadTime = new Date().getTime();
    }
}
