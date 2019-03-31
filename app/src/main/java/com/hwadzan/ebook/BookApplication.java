package com.hwadzan.ebook;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.hwadzan.ebook.model.Host;
import com.liulishuo.filedownloader.FileDownloader;
import com.tencent.bugly.Bugly;

import java.io.File;
import java.util.List;

public class BookApplication extends Application {

    //public List<Host> hostList;

    @Override
    public void onCreate() {
        super.onCreate();

        FileDownloader.setup(this);
        //bygly 1979710467
        //ks45i89f
        //app key : 857b9eee-3a37-48c7-aca1-1dc40f3ac47b
        Bugly.init(getApplicationContext(), "f456fb17ee", true);

    }

    public File getFileDirFun(String type) {
        File fileDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            fileDir = getExternalFilesDir(type);
            if(fileDir==null)
                fileDir = getFilesDir();
        } else {
            fileDir = getFilesDir();
        }
        return fileDir;
    }

    public File getCacheDirFun() {
        File cacheDir;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cacheDir = getExternalCacheDir();
            if(cacheDir==null)
                cacheDir = getCacheDir();
        } else {
            cacheDir = getCacheDir();
        }
        return cacheDir;
    }



    /**
     * 检测网络是否可用
     * @return boolean
     */
    public boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = null;
        if (cm != null) {
            ni = cm.getActiveNetworkInfo();
        }
        return ni != null && ni.isConnected();
    }

    /**
     * 获取屏幕的宽
     * @return
     */
    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度
     * @return
     */
    public int getScreenHeight() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }
}
