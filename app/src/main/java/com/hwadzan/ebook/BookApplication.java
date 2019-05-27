package com.hwadzan.ebook;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.google.gson.Gson;
import com.hwadzan.ebook.lib.CacheOKHttp;
import com.liulishuo.filedownloader.FileDownloader;

import org.lzh.framework.updatepluginlib.UpdateConfig;
import org.lzh.framework.updatepluginlib.base.UpdateParser;
import org.lzh.framework.updatepluginlib.model.Update;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

//import com.hwadzan.ebook.model.Host;
//import com.tencent.bugly.Bugly;

public class BookApplication extends Application {

    public boolean isHttpConnected = false;
    public CacheOKHttp http;

    /**
     * 0 未下载默认， 1 正在下载, 2 下载出错,  3 下载成功
     */
    public int downloadConfigState = 0;

    @Override
    public void onCreate() {
        super.onCreate();

        http = new CacheOKHttp(this, "json");

        FileDownloader.setup(this);
        //bygly 1979710467
        //ks45i89f
        //app key : 857b9eee-3a37-48c7-aca1-1dc40f3ac47b
        //Bugly.init(getApplicationContext(), "f456fb17ee", true);

        UpdateConfig.getConfig()
                .setUrl(Constants.Upgrade_Url)// 配置检查更新的API接口
                .setUpdateParser(new UpdateParser() {
                    @Override
                    public Update parse(String response) throws Exception {
                        Update update = new Gson().fromJson(response, Update.class);
                        return update;
                    }
                });
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

    public static String readFile(File file) {
        Reader read = null;
        String content = "";
        String result = "";
        BufferedReader br = null;
        try {
            read = new FileReader(file);
            br = new BufferedReader(read);
            while ((content = br.readLine()) != null) {
                result += content + "\r\n";
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                read.close();
                br.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
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
        return ni != null && ni.isAvailable();
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
