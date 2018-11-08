package com.hwadzan.ebook.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.hwadzan.ebook.BookApplication;
import com.hwadzan.ebook.R;
import com.hwadzan.ebook.lib.BookMarkPreferencesHelper;
import com.hwadzan.ebook.lib.BookPreferencesHelper;
import com.hwadzan.ebook.model.Book;
import com.liulishuo.okdownload.DownloadListener;
import com.liulishuo.okdownload.DownloadSerialQueue;
import com.liulishuo.okdownload.DownloadTask;
import com.liulishuo.okdownload.core.Util;
import com.liulishuo.okdownload.core.cause.EndCause;
import com.liulishuo.okdownload.core.cause.ResumeFailedCause;
import com.liulishuo.okdownload.core.listener.DownloadListener1;
import com.liulishuo.okdownload.core.listener.assist.Listener1Assist;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    public static final int CategoryActivityREQUESTCODE = 805;
    QMUITopBarLayout mTopBar;
    BookApplication app;
    BookPreferencesHelper bookPreferencesHelper;
    List<Book> bookList;
    RecyclerView recyclerView;
    BookAdapter bookAdapter;

    File parentFile;
    DownloadSerialQueue serialQueue;

    int imageWidth;
    int imageHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        QMUIStatusBarHelper.translucent(this);

        bookPreferencesHelper = new BookPreferencesHelper(this);

        app = (BookApplication) getApplication();
        mTopBar = (QMUITopBarLayout) findViewById(R.id.topbar);

        imageWidth = QMUIDisplayHelper.getScreenWidth(this) / 3 - QMUIDisplayHelper.dp2px(this, 10);
        imageHeight = imageWidth*228/150;

        bookList = bookPreferencesHelper.getAll();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        GridLayoutManager layoutManager=new GridLayoutManager(this,3);
        recyclerView.setLayoutManager(layoutManager);
        bookAdapter=new BookAdapter();
        recyclerView.setAdapter(bookAdapter);

        initTopBar();
        mTopBar.setTitle(getString(R.string.app_name));

        parentFile = new File(app.getFileDirFun("pdf"), "book");
        if (!parentFile.exists())
            parentFile.mkdirs();

        startDownload();

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

    void initTopBar(){
        mTopBar.setTitle(getString(R.string.app_name));
        // 切换其他情况的按钮


        mTopBar.addRightTextButton(R.string.about, R.id.topbar_right_about_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(getThisActivity(), AboutUsActivity.class));
                    }
                });

        mTopBar.addRightTextButton(R.string.fabao, R.id.topbar_right_add_fabao_button)
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivityForResult(new Intent(getThisActivity(), CategoryActivity.class), CategoryActivityREQUESTCODE);
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

            Glide.with(mContext).load(b.img_s).into(bookViewHolder.img);

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
        DownloadTask task = new DownloadTask.Builder(b.url, file).build();
        task.setTag(b);
        serialQueue.enqueue(task);
    }

    private void startDownload() {
        if(serialQueue==null) {

            Util.enableConsoleLog();
            serialQueue = new DownloadSerialQueue(downloadListener);

            File pdfDir = app.getFileDirFun("pdf");
            for(Book b : bookList){
                File file = new File(pdfDir, "book/"+b.fileName);
                if(!file.exists()){
                    b.downloaded=false;
                    bookPreferencesHelper.save(b);
                }
                if(!b.downloaded){
                    enqueueDownloadTask(b);
                }
            }
            serialQueue.resume();
        }
    }

    private void stopDownload() {
        if (serialQueue != null) {
            serialQueue.shutdown();
            serialQueue = null;
        }
    }

    DownloadListener downloadListener = new DownloadListener1() {
        @Override
        public void taskStart(@NonNull DownloadTask task, @NonNull Listener1Assist.Listener1Model model) {
            Log.i("DownloadListener", "taskStart");
        }

        @Override
        public void retry(@NonNull DownloadTask task, @NonNull ResumeFailedCause cause) {
            Log.i("DownloadListener", cause.toString());
        }

        @Override
        public void connected(@NonNull DownloadTask task, int blockCount, long currentOffset, long totalLength) {
            Log.i("DownloadListener", task.getFilename());
        }

        @Override
        public void progress(@NonNull DownloadTask task, long currentOffset, long totalLength) {
            Book b = (Book) task.getTag();
            b.downloaProcess = String.valueOf(currentOffset*100/totalLength) + "%";
            bookAdapter.notifyDataSetChanged();
        }

        @Override
        public void taskEnd(@NonNull DownloadTask task, @NonNull EndCause cause, @Nullable Exception realCause, @NonNull Listener1Assist.Listener1Model model) {
            if(EndCause.COMPLETED == cause) {
                Book b = (Book) task.getTag();
                b.downloaded = true;
                b.downloaProcess = getThisActivity().getString(R.string.downoad_over);
                bookPreferencesHelper.save(b);
                bookAdapter.notifyDataSetChanged();
            } else if(EndCause.ERROR == cause){
                Book b = (Book) task.getTag();
                b.downloaProcess = getThisActivity().getString(R.string.downoad_error);;
                bookPreferencesHelper.save(b);
                bookAdapter.notifyDataSetChanged();
            }
        }
    };
}
