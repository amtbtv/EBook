package com.hwadzan.ebook.ui;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hwadzan.ebook.BookApplication;
import com.hwadzan.ebook.Constants;
import com.hwadzan.ebook.DownloadService;
import com.hwadzan.ebook.DownloadServiceBinder;
import com.hwadzan.ebook.DownloadServiceBinderListener;
import com.hwadzan.ebook.R;
import com.hwadzan.ebook.lib.BookMarkPreferencesHelper;
import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int CategoryActivityREQUESTCODE = 805;
    QMUITopBarLayout mTopBar;
    FrameLayout mask_layout;
    Button fabaoButton;
    RecyclerView recyclerView;
    BookAdapter bookAdapter;

    BookApplication app;
    DownloadServiceBinder binder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QMUIStatusBarHelper.translucent(this);

        app = (BookApplication) getApplication();
        mTopBar = (QMUITopBarLayout) findViewById(R.id.topbar);
        mask_layout = (FrameLayout) findViewById(R.id.mask_layout);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recyclerView.setLayoutManager(layoutManager);
        initTopBar();
        mTopBar.setTitle(getString(R.string.app_name));
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(binder == null) {
            //绑定服务开始
            showMaskProcessBar(true);
            Intent bindIntent = new Intent(this, DownloadService.class);
            bindService(bindIntent, serviceConnection, this.BIND_AUTO_CREATE);
        }
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder = (DownloadServiceBinder) service;
            initBookAdapter(binder.getBookManager());
            binder.setDownloadServiceBinderListener(downloadServiceBinderListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    void initBookAdapter(BookManager bookManager){
        int imageWidth = QMUIDisplayHelper.getScreenWidth(this) / 3 - QMUIDisplayHelper.dp2px(this, 10);
        int imageHeight = imageWidth * 228 / 150;

        bookAdapter = new BookAdapter(getThisActivity(), bookManager, imageWidth, imageHeight);
        bookAdapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Book book = (Book) v.getTag();
                File file = binder.getBookPdfFile(book);
                if (book.downloaded && file.exists()) {
                    Intent intent = new Intent(getThisActivity(), PdfReaderActivity.class);
                    intent.putExtra("book", new Gson().toJson(book));
                    /*
                    book.lastReadTime = System.currentTimeMillis();
                    bookManager.save(book);
                    bookManager.sort();
                    bookAdapter.notifyDataSetChanged();
                    */
                    startActivity(intent);
                }
            }
        });

        bookAdapter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Book book = (Book) v.getTag();
                new QMUIBottomSheet.BottomListSheetBuilder(getThisActivity())
                        .addItem(getString(R.string.delfabao) + ":" + book.fabo_title)
                        .setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
                            @Override
                            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                                binder.deleteBook(book);
                                new BookMarkPreferencesHelper(getThisActivity()).remove(book.fileName);
                                dialog.dismiss();
                                bookAdapter.notifyDataSetChanged();
                            }
                        })
                        .build().show();
                return false;
            }
        });

        recyclerView.setAdapter(bookAdapter);
    }

    void onDownloadConfigStateChange(DownloadService.DownloadConfigState downloadConfigState){
        switch (downloadConfigState){
            case Fail:
                showMaskProcessBar(false);
                Toast.makeText(this, getString(R.string.NoNetworkConnected), Toast.LENGTH_LONG).show();
                break;
            case Downloading:
                break;
            case Sucess:
                showMaskProcessBar(false);
                if(bookAdapter.getItemCount() == 0)
                    showSelectBookDialog();
                break;
            case NoNetWork:
                showMaskProcessBar(false);
                Toast.makeText(this, getString(R.string.NoNetworkConnected), Toast.LENGTH_LONG).show();
                break;
            case NoStart:
                break;
        }
    }

    DownloadServiceBinderListener downloadServiceBinderListener = new DownloadServiceBinderListener() {
        @Override
        public void downloaded(Book book) {

        }

        @Override
        public void downloadConfigStateChange(DownloadService.DownloadConfigState downloadConfigState) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    onDownloadConfigStateChange(downloadConfigState);
                }
            });
        }

        @Override
        public void notifyAdapterDataSetChanged() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bookAdapter.notifyDataSetChanged();
                }
            });
        }
    };

    private void showMaskProcessBar(boolean show) {
        if (show)
            mask_layout.setVisibility(View.VISIBLE);
        else
            mask_layout.setVisibility(View.GONE);
    }

    private void showSelectBookDialog() {
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

    /**
     * 接收返回值
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CategoryActivityREQUESTCODE) {
            if (resultCode == RESULT_OK) {
                //获取question，并返回，
                String json = data.getStringExtra("book");
                Book b = new Gson().fromJson(json, Book.class);
                //后台添加下载任务
                binder.downNewBook(b);
            }
        }
    }


    Activity getThisActivity() {
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
                if (binder.getDownloadedConfigState() == DownloadService.DownloadConfigState.Sucess) {
                    startActivityForResult(new Intent(getThisActivity(), CategoryActivity.class), CategoryActivityREQUESTCODE);
                } else {
                    Toast.makeText(getThisActivity(), getString(R.string.DownConfigFail), Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(serviceConnection !=null) {
            binder.setDownloadServiceBinderListener(null);
            unbindService(serviceConnection);
        }
    }
}
