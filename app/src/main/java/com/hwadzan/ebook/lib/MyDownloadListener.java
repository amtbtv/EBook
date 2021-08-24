package com.hwadzan.ebook.lib;

import java.util.List;

public interface MyDownloadListener {
    void notifyMsg(List<DownloadTask> downloadTaskList);
}
