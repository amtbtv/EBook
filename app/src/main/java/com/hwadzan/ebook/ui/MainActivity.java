package com.hwadzan.ebook.ui;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hwadzan.ebook.BookApplication;
import com.hwadzan.ebook.Constants;
import com.hwadzan.ebook.R;
import com.hwadzan.ebook.lib.BookMarkPreferencesHelper;
import com.hwadzan.ebook.lib.BookPreferencesHelper;
import com.hwadzan.ebook.lib.CacheResult;
import com.hwadzan.ebook.lib.GlideApp;
import com.hwadzan.ebook.lib.PingCallBack;
import com.hwadzan.ebook.lib.PingTask;
import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.ibook_config;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.util.FileDownloadSerialQueue;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import org.lzh.framework.updatepluginlib.UpdateBuilder;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    public static final int CategoryActivityREQUESTCODE = 805;
    QMUITopBarLayout mTopBar;
    BookApplication app;

    BookPreferencesHelper bookPreferencesHelper;

    List<Book> bookList;
    RecyclerView recyclerView;
    BookAdapter bookAdapter;

    File parentFile;
    FileDownloadSerialQueue serialQueue;

    int imageWidth;
    int imageHeight;

    /**
     * 0 未下载默认， 1 正在下载, 2 下载出错,  3 下载成功
     */
    int downloadConfigState = 0;
    boolean downloadConfigAfterStartFaoBaoActivity = false;

    FrameLayout mask_layout;

    Button fabaoButton;
    AlphaAnimation fabaoButtonAlphaAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QMUIStatusBarHelper.translucent(this);

        bookPreferencesHelper = new BookPreferencesHelper(this);

        app = (BookApplication) getApplication();
        mTopBar = (QMUITopBarLayout) findViewById(R.id.topbar);

        imageWidth = QMUIDisplayHelper.getScreenWidth(this) / 3 - QMUIDisplayHelper.dp2px(this, 10);
        imageHeight = imageWidth * 228 / 150;

        mask_layout = (FrameLayout) findViewById(R.id.mask_layout);

        bookList = bookPreferencesHelper.getAll();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        bookAdapter = new BookAdapter();
        recyclerView.setAdapter(bookAdapter);

        initTopBar();
        mTopBar.setTitle(getString(R.string.app_name));

        parentFile = new File(app.getFileDirFun("pdf"), "book");
        if (!parentFile.exists())
            parentFile.mkdirs();

        //下载配制信息
        startDownload();

        if (bookList.size() == 0) {
            showSelectBookDialog();
        }

        //每次打开首页检测更新
        UpdateBuilder.create().check();// 启动更新任务
    }

    /**
     * 开启法宝按扭动画
     */
    public void startFabBaoButtonAnimat() {
/*
    闪烁动画
    开始闪烁
    setDuration 设置闪烁一次的时间
    setRepeatCount 设置闪烁次数 可以是具体数值，也可以是Animation.INFINITE（重复多次）
    setRepeatMode 动画结束后从头开始或从末尾开始
        Animation.REVERSE（从末尾开始） Animation.RESTART（从头开始）
    setAnimation将设置的动画添加到view上
 */
        fabaoButtonAlphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        fabaoButtonAlphaAnimation.setDuration(1000);
        fabaoButtonAlphaAnimation.setRepeatCount(Animation.INFINITE);
        fabaoButtonAlphaAnimation.setRepeatMode(Animation.RESTART);
        fabaoButton.setAnimation(fabaoButtonAlphaAnimation);
        fabaoButtonAlphaAnimation.start();
    }

    private void showMaskProcessBar(boolean show){
        if(show)
            mask_layout.setVisibility(View.VISIBLE);
        else
            mask_layout.setVisibility(View.GONE);
    }

    private void showSelectBookDialog(){
        new QMUIDialog.MessageDialogBuilder(this)
                .setTitle(R.string.Prompt)
                .setMessage(R.string.NoBookMessage)
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    //接收返回值
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == CategoryActivityREQUESTCODE){
            if(resultCode == RESULT_OK){
                //获取question，并返回，
                String json = data.getStringExtra("book");
                Book b = new Gson().fromJson(json, Book.class);
                boolean found = false;
                for(Book bb : bookList){
                    if(bb.fabo_serial.equals(b.fabo_serial)){
                        found = true;
                    }
                }
                if(!found){
                    bookList.add(0, b);
                    bookPreferencesHelper.save(b);
                    bookAdapter.notifyDataSetChanged();
                    //开始下载
                    enqueueDownloadTask(b);
                    serialQueue.resume();
                }
            }
        }
    }

    Activity getThisActivity(){
        return this;
    }

    void initTopBar() {
        mTopBar.setTitle(getString(R.string.app_name));
        // 切换其他情况的按钮


        mTopBar.addRightTextButton(R.string.about, R.id.topbar_right_about_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getThisActivity(), AboutUsActivity.class));
                    }
                });

        fabaoButton = mTopBar.addRightTextButton(R.string.fabao, R.id.topbar_right_add_fabao_button);
        fabaoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 0 未下载默认， 1 正在下载, 2 下载出错,  3 下载成功
                if (downloadConfigState == 3) {
                    startActivityForResult(new Intent(getThisActivity(), CategoryActivity.class), CategoryActivityREQUESTCODE);
                } else {
                    if (downloadConfigState == 2) {
                        downloadConfigAfterStartFaoBaoActivity = true;
                        Toast.makeText(getThisActivity(), getString(R.string.downloadingConfig), Toast.LENGTH_LONG).show();
                        startDownload();
                    } else if (downloadConfigState == 1) {
                        downloadConfigAfterStartFaoBaoActivity = true;
                        Toast.makeText(getThisActivity(), getString(R.string.downloadingConfig), Toast.LENGTH_LONG).show();
                    } else if (downloadConfigState == 0) {
                        //不可能等于0
                    }
                }
            }
        });

/*
仅有两个按扭，没必要使用底部菜单了
        mTopBar.addRightImageButton(R.mipmap.icon_topbar_overflow, R.id.topbar_right_menu_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showBottomSheet();
                    }
                });
*/
    }

/*
    private void showBottomSheet() {
        new QMUIBottomSheet.BottomListSheetBuilder(this)
                .addItem(getString(R.string.fabao))
                .addItem(getString(R.string.about))
                .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                    @Override
                    public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                        switch (position) {
                            case 0:
                                startActivityForResult(new Intent(getThisActivity(), CategoryActivity.class), CategoryActivityREQUESTCODE);
                                break;
                            case 1:
                                startActivity(new Intent(getThisActivity(), AboutUsActivity.class));
                                break;
                        }
                        dialog.dismiss();
                    }
                })
                .build().show();
    }
*/

    private class BookAdapter extends RecyclerView.Adapter<BookViewHolder> {
        private Context mContext;
        @NonNull
        @Override
        public BookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
            if (mContext==null){
                mContext=viewGroup.getContext();
            }
            View view= LayoutInflater.from(mContext).inflate(R.layout.item_book, viewGroup, false);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Book book = (Book) v.getTag();
                    File file = new File(parentFile, book.fileName);
                    if(book.downloaded && file.exists()) {
                        Intent intent = new Intent(getThisActivity(), PdfReaderActivity.class);
                        intent.putExtra("book", new Gson().toJson(book));
                        book.lastReadTime = System.currentTimeMillis();
                        bookPreferencesHelper.save(book);
                        Collections.sort(bookList, BookPreferencesHelper.comparator);
                        bookAdapter.notifyDataSetChanged();
                        startActivity(intent);
                    }
                }
            });

            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Book book = (Book) v.getTag();
                    new QMUIBottomSheet.BottomListSheetBuilder(getThisActivity())
                            .addItem(getString(R.string.delfabao)+":"+book.fabo_title)
                            .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                                @Override
                                public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                    File file = new File(parentFile, book.fileName);
                                    if(file.exists())
                                        file.delete();
                                    bookList.remove(book);
                                    bookPreferencesHelper.remove(book);
                                    new BookMarkPreferencesHelper(getThisActivity()).remove(book.fileName);
                                    dialog.dismiss();
                                    bookAdapter.notifyDataSetChanged();
                                }
                            })
                            .build().show();
                    return false;
                }
            });
            return new BookViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookViewHolder bookViewHolder, int i) {
            Book b = bookList.get(i);
            bookViewHolder.title.setText(b.fabo_title);
            if(b.downloaded) {
                bookViewHolder.process.setVisibility(View.GONE);
            } else {
                bookViewHolder.process.setVisibility(View.VISIBLE);
                bookViewHolder.process.setText(String.valueOf(b.downloaProcess));
            }
            bookViewHolder.cardView.setTag(b);

            GlideApp.with(mContext).load(b.img_s).into(bookViewHolder.img);
        }

        @Override
        public int getItemCount() {
            return bookList.size();
        }
    }

    private class BookViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView img;
        TextView title;
        TextView process;
        public BookViewHolder(View view) {
            super(view);
            cardView=(CardView)view;
            img=(ImageView)view.findViewById(R.id.img);
            FrameLayout.LayoutParams lp=(FrameLayout.LayoutParams) img.getLayoutParams();
            lp.width=imageWidth;
            lp.height=imageHeight;
            img.setLayoutParams(lp);
            title=(TextView)view.findViewById(R.id.title);
            process=(TextView)view.findViewById(R.id.process);
        }

    }

    private void enqueueDownloadTask(Book b){
        File file = new File(parentFile, b.fileName);
        BaseDownloadTask task = FileDownloader.getImpl().create(b.url);
        task.setPath(file.getAbsolutePath());
        task.setTag(b);
        task.setAutoRetryTimes(3);
        task.setListener(downloadListener);
        serialQueue.enqueue(task);
    }

    /**
     * 下载配制信息出错，显示对话框，重新下载
     */
    void showReDonConfigDialog(){
        new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                .setTitle(R.string.Prompt)
                .setMessage(R.string.DownConfigFail)
                .addAction(R.string.ReDown, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        startDownload();
                    }
                })
                .addAction(R.string.Cancle, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private ibook_config paraseConfigFile(File file){
        String json = BookApplication.readFile(file);
        ibook_config config = new Gson().fromJson(json, ibook_config.class);
        return config;
    }

    /**
     * 下载配制信息的回调函数
     */
    CacheResult downConfigCallback = new CacheResult() {
        @Override
        public void tackFile(File file) {
            if(file==null){
                downloadConfigState = 2; //下载出错
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fabaoButtonAlphaAnimation.cancel();
                        showReDonConfigDialog();
                    }
                });
            } else {
                app.isHttpConnected = true;
                ibook_config config = paraseConfigFile(file);
                Constants.domain = config.domain;
                Constants.download = config.download;
                downloadConfigState = 3; //下载成功
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        fabaoButtonAlphaAnimation.cancel();
                        if(downloadConfigAfterStartFaoBaoActivity){
                            startActivityForResult(new Intent(getThisActivity(), CategoryActivity.class), CategoryActivityREQUESTCODE);
                        } else {
                            //开始下载未下载完成的电子图书
                            if (serialQueue == null) {
                                serialQueue = new FileDownloadSerialQueue();
                                File pdfDir = app.getFileDirFun("pdf");
                                for (Book b : bookList) {
                                    File file = new File(pdfDir, "book/" + b.fileName);
                                    if (!file.exists()) {
                                        b.downloaded = false;
                                        bookPreferencesHelper.save(b);
                                    }
                                    if (!b.downloaded) {
                                        enqueueDownloadTask(b);
                                    }
                                }
                                serialQueue.resume();
                            } else {
                                serialQueue.resume();
                            }
                        }
                    }
                });
            }
        }
    };

    private void startDownload() {
        if(app.isNetworkConnected()) {
            /*
            new PingTask(new PingCallBack() {
                @Override
                public void state(boolean state) {
                    //真的有网络
                    if(state){
                        app.isHttpConnected = true;
                        downloadConfigState = 1; //下载成功
                        //真正的开始下载配制文件
                        startDownloadConfig();
                    } else {
                        downloadConfigAfterStartFaoBaoActivity = false;
                        app.isHttpConnected = false;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showNoNetworkDialog();
                            }
                        });
                    }
                }
            }).execute("www.baidu.com");
*/
            downloadConfigState = 1; //下载成功
            //真正的开始下载配制文件
            startDownloadConfig();
        } else {
            downloadConfigAfterStartFaoBaoActivity = false;
            showNoNetworkDialog();
        }
    }

    private void startDownloadConfig(){
        //在initTopBar之后，已初始化fabaoButton，让其动画闪烁
        startFabBaoButtonAnimat();

        String urls[] = {
                Constants.IBOOK_CONFIG_URL_TW,
                Constants.IBOOK_CONFIG_URL_CN
        };

        app.http.asyncTakeFastFile(urls, "setting.json", downConfigCallback);
    }

    private void showNoNetworkDialog(){
        new QMUIDialog.MessageDialogBuilder(MainActivity.this)
                .setTitle(R.string.Prompt)
                .setMessage(R.string.NoNetworkConnected)
                .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private void stopDownload() {
        if (serialQueue != null) {
            serialQueue.shutdown();
            serialQueue = null;
        }
    }

    FileDownloadListener downloadListener = new FileDownloadListener() {
        /*
pending 	等待，已经进入下载队列 	数据库中的soFarBytes与totalBytes
started 	结束了pending，并且开始当前任务的Runnable 	-
connected 	已经连接上 	ETag, 是否断点续传, soFarBytes, totalBytes
progress 	下载进度回调 	soFarBytes
blockComplete 	在完成前同步调用该方法，此时已经下载完成 	-
retry 	重试之前把将要重试是第几次回调回来 	之所以重试遇到Throwable, 将要重试是第几次, soFarBytes
completed 	完成整个下载过程 	-
paused 	暂停下载 	soFarBytes
error 	下载出现错误 	抛出的Throwable
warn 	在下载队列中(正在等待/正在下载)已经存在相同下载连接与相同存储路径的任务 	-
*/
        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Book b = (Book) task.getTag();
            b.downloaProcess = getString(R.string.waiting);
            bookAdapter.notifyDataSetChanged();
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Book b = (Book) task.getTag();
            long d = soFarBytes;
            long t = totalBytes;
            b.downloaProcess = String.valueOf(d*100/t) + "%";
            bookAdapter.notifyDataSetChanged();
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            Book b = (Book) task.getTag();
            b.downloaded = true;
            b.downloaProcess = getThisActivity().getString(R.string.downoad_over);
            bookPreferencesHelper.save(b);
            bookAdapter.notifyDataSetChanged();
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            Book b = (Book) task.getTag();
            b.downloaProcess = getString(R.string.paused);
            bookAdapter.notifyDataSetChanged();
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            Book b = (Book) task.getTag();
            b.downloaProcess = getThisActivity().getString(R.string.downoad_error);;
            bookPreferencesHelper.save(b);
            bookAdapter.notifyDataSetChanged();
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            Book b = (Book) task.getTag();
            b.downloaProcess = "Warm";
            bookAdapter.notifyDataSetChanged();
        }
    };
}
