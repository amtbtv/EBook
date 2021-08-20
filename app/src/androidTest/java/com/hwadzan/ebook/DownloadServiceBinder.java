package com.hwadzan.ebook;

import android.os.Binder;

public class DownloadServiceBinder extends Binder {
    DownloadService downloadService;
    public DownloadServiceBinder(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

}
