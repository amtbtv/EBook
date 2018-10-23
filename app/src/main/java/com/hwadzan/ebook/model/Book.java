package com.hwadzan.ebook.model;

import java.net.PortUnreachableException;
import java.util.Date;

/**
 * Created by wangwei
 * 法宝
 * Date: 11/24/12
 * {"img_b":"png","img_s":"png","epub":false,"pdf":true,"fabo_serial":"CH15-06-01","fabo_title":"淨土五經讀本【全部注音】","path":"/fabo/","txt":false,"fabo_content":"32開 精裝"}
 */
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

    public Book(){
        downloaded = false;
        fileName = "";
        url = "";
        downloaProcess = "%0";
        lastReadTime = new Date().getTime();
    }
}
