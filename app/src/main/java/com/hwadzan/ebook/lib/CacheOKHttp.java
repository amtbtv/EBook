package com.hwadzan.ebook.lib;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Administrator on 2018/3/1.
 */

public class CacheOKHttp {
    OkHttpClient client;
    private long checkTime;
    File cacheDir;

    public long getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(long checkTime) {
        this.checkTime = checkTime;
    }

    public static Headers headers = new Headers.Builder()
            .add("Connection", "keep-alive")
            .add("Accept", "application/json, text/javascript, */*; q=0.01")
            .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.87 Safari/537.36")
            .add("Accept-Language", "en-US,en;q=0.8")
            .build();


    public CacheOKHttp(Context context, String dir){

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) || !Environment.isExternalStorageRemovable()) {
            cacheDir = new File(context.getExternalCacheDir(), dir);
        } else {
            cacheDir = new File(context.getCacheDir(), dir);
        }

        if(!cacheDir.exists())
            cacheDir.mkdir();

        //默认缓存1天
        checkTime = 1000*30*60*24;

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            TrustAllCerts trustAllCerts = new TrustAllCerts();
            sc.init(null, new TrustManager[]{trustAllCerts}, new SecureRandom());
            builder.sslSocketFactory(sc.getSocketFactory(), trustAllCerts);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }

        builder.hostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        });

        builder.connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS);
        client = builder.build();
    }


    private class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}

        @Override
        public X509Certificate[] getAcceptedIssuers() {return new X509Certificate[0];}
    }

    private SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    /**
     * 这里保存文件，应该加以修改，保存为缓存文件，最后改名文件，保证文件是正确完整的
     * @param cacheFile
     * @param response
     * @return
     */
    private File saveFile(File cacheFile, Response response){
        File tmpFile = new File(cacheFile.getParentFile(), cacheFile.getName()+".tmp");
        if(tmpFile.exists())
            tmpFile.delete();
        boolean isOk = false;
        long length = response.body().contentLength();
        if (length == 0){
            // 文件长度为0，肯定有问题
            return null;
        }
        // 保存文件到本地
        InputStream is = null;
        FileOutputStream fos = null;

        byte[] buff = new byte[2048];
        int len = 0;
        try {
            is = response.body().byteStream();
            fos = new FileOutputStream(tmpFile);
            while ((len = is.read(buff)) != -1) {
                fos.write(buff, 0, len);
            }
            fos.flush();

            // 下载完成
            isOk = true;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null){
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(isOk) {
            if(cacheFile.exists())
                cacheFile.delete();
            tmpFile.renameTo(cacheFile);
            return cacheFile;
        } else {
            return null;
        }
    }

    public void asyncTakeFastStringResult(String[] urls, final StringResult stringResult) {

        FastSiteSynchronized fastSiteSynchronized = new FastSiteSynchronized(urls);
        for (String url : urls) {
            Request request = new Request.Builder()
                    .headers(headers)
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    String url = call.request().url().toString();
                    FastSiteSynchronized.State state = fastSiteSynchronized.putFail(url);
                    if(state == FastSiteSynchronized.State.AllFail) {
                        stringResult.takeStringResult(null);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String url = call.request().url().toString();
                    String str = response.body().string();
                    if(fastSiteSynchronized.putSucess(url, str)){
                        stringResult.takeStringResult(str);
                    }
                }
            });
        }
    }

    /**
     * 将下载并图片存入文件缓存
     */
    public File asyncTakeFile(String url, String fileName, final CacheResult fileCacheResult)
    {
        final File cacheFile = new File(cacheDir, fileName);
        if(!cacheFile.exists() || (cacheFile.exists() && System.currentTimeMillis() - cacheFile.lastModified() > checkTime)) {
            Request request = new Request.Builder()
                    .headers(headers)
                    .url(url)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    fileCacheResult.takeFile(null);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    fileCacheResult.takeFile(saveFile(cacheFile, response));
                }
            });
        }
        if(cacheFile.exists())
            return cacheFile;
        else
            return null;
    }
}
