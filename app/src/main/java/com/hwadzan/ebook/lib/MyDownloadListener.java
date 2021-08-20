package com.hwadzan.ebook.lib;

import android.os.Message;

public interface MyDownloadListener {
    /**
     *
     * @param msg 有下面四个属性
     *    public int what;
     *    public int arg1; size
     *    public int arg2; 间隔时间
     *    public Object obj; List<DownloadTask>
     */
    void notifyMsg(Message msg);
}
