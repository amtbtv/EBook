package com.hwadzan.ebook.model;

import com.hwadzan.ebook.R;

/**
 * Created by wangwei
 * Date: 11/25/12
 */
public class DownloadBook {


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

        public static Status parseStatus(String value) {
            Status status = Status.valueOf(value);
            return status == null ? Status.WAITING : status;
        }
    }

    public Book book;
    public long size;
    public String fileUrl;
    public String server;
    public long position;
    public boolean isMarked;//是否有书签
    public boolean localFile;//是否本地assert文件
    public Status status = Status.WAITING;

    public int describeContents() {
        return 0;
    }

}
