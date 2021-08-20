package com.hwadzan.ebook.lib;

public class DownloadTask {
    public Long downloadId = -1L;
    public String downloadUrl = "";
    public int downloadedBytes = -1;
    public int totalBytes = -1;
    public int status = 0;

    public String localFileUri;
}
