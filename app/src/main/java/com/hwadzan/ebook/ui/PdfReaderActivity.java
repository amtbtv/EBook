package com.hwadzan.ebook.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.view.ViewCompat;

import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.app.hubert.guide.NewbieGuide;
import com.app.hubert.guide.listener.AnimationListenerAdapter;
import com.app.hubert.guide.model.GuidePage;
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
import com.hwadzan.ebook.lib.SettingPreferencesHelper;
import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookMark;
import com.hwadzan.ebook.model.Setting;
import com.qmuiteam.qmui.skin.QMUISkinManager;
import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.util.QMUIViewHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.shockwave.pdfium.util.SizeF;

import java.io.File;

public class PdfReaderActivity extends Activity {

    Book book;

    SizeF pageSize;

    int screenWidth;
    int screenWidth3;
    int screenHeight;
    int screenHeight3;

    QMUITopBarLayout mTopBar;
    BookApplication app;
    BookMarkPreferencesHelper bookMarkPreferencesHelper;
    BookMark bookMark;
    PdfLinearLayout pdf_layout;
    PDFView pdfView;

    SeekBar seekBarPage;
    boolean isSeekBarTracking = false;

    Switch switchDayNight;
    Switch switchVerticalPage;
    TextView process;
    TextView process_textView;
    private boolean isFullScreen;
    private FrameLayout mTabContainer;

    SettingPreferencesHelper settingPreferencesHelper;
    Setting setting;

    public static final int BLUE = 1;
    public static final int DARK = 2;
    QMUISkinManager mSkinManager;

    Handler handler;
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

        handler = new Handler();

        mSkinManager = QMUISkinManager.defaultInstance(this);
        mSkinManager.addSkin(BLUE, R.style.AppTheme);
        mSkinManager.addSkin(DARK, R.style.app_skin_dark);

        screenWidth = QMUIDisplayHelper.getScreenWidth(this);
        screenWidth3 = screenWidth/3;
        screenHeight = QMUIDisplayHelper.getScreenHeight(this);
        screenHeight3 = screenHeight/3;

        Intent intent = getIntent();
        String json = intent.getStringExtra("book");
        book = new Gson().fromJson(json, Book.class);

        settingPreferencesHelper = new SettingPreferencesHelper(this);
        setting = settingPreferencesHelper.getSetting();

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
                isSeekBarTracking = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isSeekBarTracking = false;
                performPageSnap();
            }
        });

        pdfView = (PDFView) findViewById(R.id.pdfView);

        switchVerticalPage = (Switch) findViewById(R.id.switchVerticalPage);
        switchVerticalPage.setChecked(setting.isVerticalPage);
        switchVerticalPage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setting.isVerticalPage = isChecked;
                settingPreferencesHelper.save(setting);
                Toast.makeText(PdfReaderActivity.this, R.string.switchVerticalPage, Toast.LENGTH_LONG).show();
            }
        });


        switchDayNight = (Switch) findViewById(R.id.switchDayNight);
        switchDayNight.setChecked(setting.isNight);
        switchDayNight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setting.isNight = isChecked;
                pdfView.setNightMode(isChecked);
                setPdfViewBackColor(setting.isNight);
                settingPreferencesHelper.save(setting);
                pdfView.jumpTo(pdfView.getCurrentPage());
            }
        });

        pdf_layout.setOnPositionClickListener(new OnPositionClickListener() {
            @Override
            public void onClick(View view, final float x, final float y) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //根据
                        float p;
                        int sc;
                        if(setting.isVerticalPage){
                            p = y;
                            sc = screenHeight3;
                        } else {
                            p=x;
                            sc = screenWidth3;
                        }

                        if(p<sc && isFullScreen){
                            if(pdfView.getCurrentPage()>0) {
                                pdfView.jumpTo(pdfView.getCurrentPage() - 1, true);
                                //pdfView.pageFillsScreen();
                                //pdfView.zoomCenteredRelativeTo();
                                //pdfView.performPageSnap();
                                //pdfView.zoomCenteredRelativeTo(1.0f, new PointF(0, 100f));
                                //performPageSnap();
                            }
                        } else if(p>sc*2 && isFullScreen){
                            if(pdfView.getCurrentPage()<pdfView.getPageCount()-1) {
                                pdfView.jumpTo(pdfView.getCurrentPage() + 1, true);
                                //performPageSnap();
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
        File parentFile = new File(app.getFileDirFun("pdf"), "book");
        File file = new File(parentFile, book.fileName);
        pdfView.fromFile(file)
                .enableDoubletap(true) //双击不放大
                .enableSwipe(true) // 允许手指滑动操作换页进度
                .swipeHorizontal(!setting.isVerticalPage) //水平翻页
                //.spacing(1) // 自动页间距
                .autoSpacing(true)
                .pageFitPolicy(FitPolicy.WIDTH) //页面适应屏幕大小
                .pageSnap(true) // snap pages to screen boundaries
                .pageFling(true) // make a fling change only a single page like ViewPager
                .defaultPage(bookMark.page) // 初始化第一页
                .nightMode(setting.isNight)
                .onPageChange(new OnPageChangeListener() {
                    @Override
                    public void onPageChanged(int page, int pageCount) {
                        process.setText(String.valueOf(page+1)+"/"+String.valueOf(pageCount));
                        process_textView.setText(String.valueOf(page+1)+"/"+String.valueOf(pageCount));
                        performPageSnap();
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

        if(setting.isVerticalPage){
            NewbieGuide.with(PdfReaderActivity.this)
                    .setLabel("PdfReaderActivityGuide1_v")
                    .setShowCounts(1)//控制次数
                    .addGuidePage(GuidePage.newInstance()
                            .setLayoutRes(R.layout.view_guide_activity_padreader_v))
                    .show();
        } else {
            NewbieGuide.with(PdfReaderActivity.this)
                    .setLabel("PdfReaderActivityGuide1_h")
                    .setShowCounts(1)//控制次数
                    .addGuidePage(GuidePage.newInstance()
                            .setLayoutRes(R.layout.view_guide_activity_padreader_h))
                    .show();
        }

        setPdfViewBackColor(setting.isNight);
    }

    void performPageSnap(){
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!isSeekBarTracking) {
                    int curPage = pdfView.getCurrentPage();
                    SizeF curPageSize = pdfView.getPageSize(curPage);
                    if(sizeChage(curPageSize, pageSize)){
                        pageSize = curPageSize;
                        pdfView.fitToWidth(curPage);
                    }
                    pdfView.performPageSnap();
                }
            }
        }, 300);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(mSkinManager != null){
            mSkinManager.register(this);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mSkinManager != null){
            mSkinManager.unRegister(this);
        }
    }

    boolean sizeChage(SizeF s1, SizeF s2){
        if(s1==null || s2==null)
            return true;

        if(Math.abs((s1.getWidth() - s2.getWidth()) / s1.getWidth())>0.05)
            return true;

        if(Math.abs((s1.getHeight() - s2.getHeight()) / s1.getHeight())>0.05)
            return true;

        return false;
    }

    private void setPdfViewBackColor(boolean isNight){
        //mTabContainer app_color_blue_2  qmui_config_color_black
        if(isNight){
            mSkinManager.changeSkin(DARK);
            mTabContainer.setBackgroundColor(getColor(R.color.qmui_config_color_black));
            pdfView.setBackgroundColor(getColor(R.color.qmui_config_color_pure_black));
        } else {
            mSkinManager.changeSkin(BLUE);
            mTabContainer.setBackgroundColor(getColor(R.color.app_color_blue));
            pdfView.setBackgroundColor(getColor(R.color.qmui_config_color_gray_9));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        bookMark.page = pdfView.getCurrentPage();
        bookMarkPreferencesHelper.save(book.fileName, bookMark);
    }

    private void changeToFullScreen() {
        isFullScreen = true;
        QMUIDisplayHelper.setFullScreen(this);
        QMUIViewHelper.fadeOut(mTopBar, 300, null, true);
        QMUIViewHelper.fadeOut(mTabContainer, 300, null, true);
    }

    private void changeToNotFullScreen() {
        isFullScreen = false;
        QMUIDisplayHelper.cancelFullScreen(this);
        QMUIViewHelper.fadeIn(mTopBar, 300, null, true);
        QMUIViewHelper.fadeIn(mTabContainer, 300, null, true);
    }
}
