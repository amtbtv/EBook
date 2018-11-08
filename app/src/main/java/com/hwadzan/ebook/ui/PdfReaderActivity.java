package com.hwadzan.ebook.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;
import com.github.barteksc.pdfviewer.util.FitPolicy;
import com.google.gson.Gson;
import com.hwadzan.ebook.BookApplication;
import com.hwadzan.ebook.R;
import com.hwadzan.ebook.lib.BookMarkPreferencesHelper;
import com.hwadzan.ebook.lib.OnPositionClickListener;
import com.hwadzan.ebook.lib.PdfLinearLayout;
import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookMark;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

import java.io.File;

public class PdfReaderActivity extends Activity {

    Book book;

    int screenWidth;
    int screenWidth3;

    QMUITopBarLayout mTopBar;
    BookApplication app;
    BookMarkPreferencesHelper bookMarkPreferencesHelper;
    BookMark bookMark;
    PdfLinearLayout pdf_layout;
    PDFView pdfView;
    SeekBar seekBarPage;
    Switch switchDayNight;
    TextView process;
    TextView process_textView;
    private boolean isFullScreen;
    private FrameLayout mTabContainer;

    /*
        需要的功能
        1. 横屏竖屏 //可不做，用处不大
        2. 书签 defaultPage(0)
        3. 进度 .jumpTo(pdfView.getCurrentPage() - 1, true);
        4. 夜间模式 nightMode(false) pdfView.setNightMode();
        5. 单页，滚动浏览 //可不做，用处不大
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_reader);
        QMUIStatusBarHelper.translucent(this);

        screenWidth = QMUIDisplayHelper.getScreenWidth(this);
        screenWidth3 = screenWidth/3;

        Intent intent = getIntent();
        String json = intent.getStringExtra("book");
        book = new Gson().fromJson(json, Book.class);

        bookMarkPreferencesHelper = new BookMarkPreferencesHelper(this);
        bookMark = bookMarkPreferencesHelper.getBookMark(book.fileName);
        if(bookMark==null)
            bookMark = new BookMark();

        app = (BookApplication) getApplication();
        mTopBar = (QMUITopBarLayout) findViewById(R.id.topbar);
        mTopBar.setTitle(book.fabo_title);
        mTabContainer = (FrameLayout) findViewById(R.id.tabs_container);

        pdf_layout = (PdfLinearLayout) findViewById(R.id.pdf_layout);
        process = (TextView) findViewById(R.id.process);
        process_textView = (TextView) findViewById(R.id.process_textView);

        seekBarPage = (SeekBar) findViewById(R.id.seekBarPage);
        seekBarPage.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(progress != pdfView.getCurrentPage())
                    pdfView.jumpTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        switchDayNight = (Switch) findViewById(R.id.switchDayNight);
        switchDayNight.setChecked(bookMark.isNight);
        switchDayNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                bookMark.isNight = isChecked;
                pdfView.setNightMode(isChecked);
                pdfView.jumpTo(pdfView.getCurrentPage());
            }
        });

        pdf_layout.setOnPositionClickListener(new OnPositionClickListener() {
            @Override
            public void onClick(View view, final float x, float y) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(x<screenWidth3 && isFullScreen){
                            if(pdfView.getCurrentPage()>0) {
                                pdfView.jumpTo(pdfView.getCurrentPage() - 1, true);

                            }
                        } else if(x>screenWidth3*2 && isFullScreen){
                            if(pdfView.getCurrentPage()<pdfView.getPageCount()-1) {
                                pdfView.jumpTo(pdfView.getCurrentPage() + 1, true);
                            }
                        } else {
                            if(isFullScreen) {
                                seekBarPage.setProgress(pdfView.getCurrentPage());
                                changeToNotFullScreen();
                            }else {
                                changeToFullScreen();
                            }
                        }
                    }
                });
            }
        });


        pdfView = (PDFView) findViewById(R.id.pdfView);

        /*pdfView.fromUri(Uri)
or
pdfView.fromFile(File)
or
pdfView.fromBytes(byte[])
or
pdfView.fromStream(InputStream) // stream is written to bytearray - native code cannot use Java Streams
or
pdfView.fromSource(DocumentSource)
or
pdfView.fromAsset(String)
    .pages(0, 2, 1, 3, 3, 3) //指定浏览的页列表，默认所有页
    .enableSwipe(true) // 允许手指滑动操作换页进度
    .swipeHorizontal(false) //横向流动
    .enableDoubletap(true) //双击放大
    .defaultPage(0)
    //  允许输出一些内容到当前页，通常在屏幕中间
    .onDraw(onDrawListener)
    // 允许输出一些内容到所有页，在第一页显示时调用
    .onDrawAll(onDrawListener)
    .onLoad(onLoadCompleteListener) // 文档载入后执行
    .onPageChange(onPageChangeListener)
    .onPageScroll(onPageScrollListener)
    .onError(onErrorListener)
    .onPageError(onPageErrorListener)
    .onRender(onRenderListener) // 第一次输出文档时执行事件
    // called on single tap, return true if handled, false to toggle scroll handle visibility
    .onTap(onTapListener)
    .onLongPress(onLongPressListener) //长按事件
    .enableAnnotationRendering(false) // 输出一些提示说明内容
    .password(null)
    .scrollHandle(null)
    .enableAntialiasing(true) // 抗锯齿
    // 页间距，单位 DP，页间颜色可通过设置背景来设置
    .spacing(0)
    .autoSpacing(false) // 自动页间距
    .linkHandler(DefaultLinkHandler)
    .pageFitPolicy(FitPolicy.WIDTH)
    .pageSnap(true) // 吸附页面边缘到屏幕边缘
    .pageFling(false) // 单页模式，就象ViewPager
    .nightMode(false) // 夜间模式
    .load();*/
        File cacheDir = app.getFileDirFun("pdf");
        File file = new File(cacheDir, "book/"+book.fileName);
        pdfView.fromFile(file)
                .enableDoubletap(false) //双击不放大
                .enableSwipe(true) // 允许手指滑动操作换页进度
                .swipeHorizontal(true) //水平翻页
                .spacing(1) // 自动页间距
                .pageFitPolicy(FitPolicy.BOTH) //页面适应屏幕大小
                .pageSnap(true) // snap pages to screen boundaries
                .pageFling(true) // make a fling change only a single page like ViewPager
                .defaultPage(bookMark.page) // 初始化第一页
                .nightMode(bookMark.isNight)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        process.setText(String.valueOf(page+1)+"/"+String.valueOf(pageCount));
                        process_textView.setText(String.valueOf(page+1)+"/"+String.valueOf(pageCount));
                    }
                })
                .onLoad(new OnLoadCompleteListener() {
                    @Override
                    public void loadComplete(int nbPages) {
                        seekBarPage.setMax(nbPages-1);
                        seekBarPage.setProgress(bookMark.page);
                    }
                })
                .load();

        changeToFullScreen();
    }


    Activity getThisActivity(){
        return this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        bookMark.page = pdfView.getCurrentPage();
        bookMarkPreferencesHelper.save(book.fileName, bookMark);
    }

    private void changeToFullScreen() {
        isFullScreen = true;
        Activity activity = this;
        if (activity != null) {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            View decorView = window.getDecorView();
            int systemUi = decorView.getSystemUiVisibility();
            systemUi |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                systemUi |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUi |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(systemUi);
            QMUIDisplayHelper.setFullScreen(this);
            QMUIViewHelper.fadeOut(mTopBar, 300, null, true);
            QMUIViewHelper.fadeOut(mTabContainer, 300, null, true);
        }
    }

    private void changeToNotFullScreen() {
        isFullScreen = false;
        Activity activity = this;
        if (activity != null) {
            Window window = activity.getWindow();
            if (window == null) {
                return;
            }
            final View decorView = window.getDecorView();
            int systemUi = decorView.getSystemUiVisibility();
            systemUi &= ~View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                systemUi &= ~View.SYSTEM_UI_FLAG_FULLSCREEN;
                systemUi |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUi &= ~View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            decorView.setSystemUiVisibility(systemUi);
            QMUIDisplayHelper.cancelFullScreen(this);
            QMUIViewHelper.fadeIn(mTopBar, 300, null, true);
            QMUIViewHelper.fadeIn(mTabContainer, 300, null, true);
            decorView.post(new Runnable() {
                @Override
                public void run() {
                    ViewCompat.requestApplyInsets(decorView);
                }
            });

        }

    }
}
