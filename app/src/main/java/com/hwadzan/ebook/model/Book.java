package com.hwadzan.ebook.model;

import com.hwadzan.ebook.R;

import java.net.PortUnreachableException;
import java.util.Date;

/**
 * Created by wangwei
 * 法宝
 * Date: 11/24/12
 * {"img_b":"png","img_s":"png","epub":false,"pdf":true,"fabo_serial":"CH15-06-01","fabo_title":"淨土五經讀本【全部注音】","path":"/fabo/","txt":false,"fabo_content":"32開 精裝"}
 */
public class Book {
    public enum Status {
        WAITING(R.string.waiting),
        DOWNLOADING(R.string.downloading),
        PAUSE(R.string.paused),
        COMPLETE(R.string.complete);

        private int description;

        Status(int description) {
            this.description = description;
        }

        public int getDescription() {
            return description;
        }

        public static DownloadBook.Status parseStatus(String value) {
            DownloadBook.Status status = DownloadBook.Status.valueOf(value);
            return status == null ? DownloadBook.Status.WAITING : status;
        }
    }


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
