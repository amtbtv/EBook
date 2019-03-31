package com.hwadzan.ebook.ui;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hwadzan.ebook.BookApplication;
import com.hwadzan.ebook.Constants;
import com.hwadzan.ebook.R;
import com.hwadzan.ebook.lib.TW2CN;
import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.Category;
import com.hwadzan.ebook.model.Host;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;
import com.qmuiteam.qmui.widget.dialog.QMUIBottomSheet;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CategoryActivity extends AppCompatActivity {

    ListView category_lv;
    EditText search;
    ListView content_lv;

    FrameLayout mask_layout;

    List<Category> categoryList;
    CategoryAdatper categoryAdatper;


    List<Book> bookList;
    List<Book> bookListFilter;
    BooksAdatper booksAdatper;

    BookApplication app;

    OkHttpClient http;

    TW2CN tw2CN;

    QMUITopBarLayout mTopBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        QMUIStatusBarHelper.translucent(this);

        tw2CN = TW2CN.getInstance(this);
        bookListFilter = new ArrayList<>();
        bookList = new ArrayList<>();

        app = (BookApplication) getApplication();
        mTopBar = (QMUITopBarLayout) findViewById(R.id.topbar);
        initTopBar();

        mask_layout = (FrameLayout) findViewById(R.id.mask_layout);

        category_lv = (ListView) findViewById(R.id.category_lv);
        search = (EditText) findViewById(R.id.search);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null && s.length()>0){
                    String str = s.toString();
                    if(tw2CN.getIsZH())
                        str = tw2CN.s2t(str);

                    bookListFilter.clear();
                    for(Book b : bookList){
                        if(b.fabo_title.indexOf(str)>=0){
                            bookListFilter.add(b);
                        }
                    }
                    booksAdatper.notifyDataSetChanged();
                } else {
                    bookListFilter.clear();
                    bookListFilter.addAll(bookList);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        content_lv = (ListView) findViewById(R.id.content_lv);

        category_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Category c =  (Category) view.getTag();
                categoryAdatper.selectedAid = c.aid;
                categoryAdatper.notifyDataSetChanged();
                showMaskProcessBar(true);
                downCategoryContentList(c);
            }
        });

        content_lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book b = (Book) view.getTag();
                showSimpleBottomSheetList(b);
            }
        });

        http = new OkHttpClient();

        showMaskProcessBar(true);
        downCategoryList();

    }

    private void showMaskProcessBar(boolean show){
        if(show)
            mask_layout.setVisibility(View.VISIBLE);
        else
            mask_layout.setVisibility(View.GONE);
    }

    /*
    private void showSimpleBottomSheetList(final Book b) {
        // 从华藏下载PDF格式电子
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getThisActivity());
        for (Host host : app.hostList) {
            if ((host.fabo_host_type.equals("PDF") && b.pdf))// || (host.fabo_host_type.equals("TXT") && b.txt) || (host.fabo_host_type.equals("EPUB") && b.epub))
            {
                builder.addItem(String.format(getString(R.string.from_download), host.fabo_host_name, host.fabo_host_type), host.fabo_host_server+";"+host.fabo_host_type);
            }
        }
        builder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                dialog.dismiss();
                String[] ss = tag.toLowerCase().split(";");
                b.url = Constants.Make_DOWNLOAD_BOOK_URL(ss[0], b.path, ss[1], b.fabo_serial);
                b.fileName = b.fabo_serial + "." + ss[1];
                b.lastReadTime = new Date().getTime();

                //数据是使用Intent返回
                Intent intent = new Intent();
                //把返回数据存入Intent
                intent.putExtra("book", new Gson().toJson(b));
                //设置返回数据
                setResult(RESULT_OK, intent);

                finish();
            }
        })
                .build()
                .show();
    }
    */

    private void showSimpleBottomSheetList(final Book b) {
        // 从华藏下载PDF格式电子
        QMUIBottomSheet.BottomListSheetBuilder builder = new QMUIBottomSheet.BottomListSheetBuilder(getThisActivity());
        builder.addItem(getString(R.string.ConfirmDownload));
        builder.setOnSheetItemClickListener(new QMUIBottomSheet.BottomListSheetBuilder.OnSheetItemClickListener() {
            @Override
            public void onClick(QMUIBottomSheet dialog, View itemView, int position, String tag) {
                dialog.dismiss();
                if (b.pdf) {
                    b.url = Constants.Make_DOWNLOAD_BOOK_URL("pdf", b.fabo_serial);
                    b.fileName = b.fabo_serial + ".pdf";
                    b.lastReadTime = new Date().getTime();
                    //数据是使用Intent返回
                    Intent intent = new Intent();
                    //把返回数据存入Intent
                    intent.putExtra("book", new Gson().toJson(b));
                    //设置返回数据
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    new QMUIDialog.MessageDialogBuilder(CategoryActivity.this)
                            .setTitle(R.string.Prompt)
                            .setMessage(R.string.UnsupportedBook)
                            .addAction(R.string.ok, new QMUIDialogAction.ActionListener() {
                                @Override
                                public void onClick(QMUIDialog dialog, int index) {
                                    dialog.dismiss();
                                }
                            })
                            .show();
                }
            }
        })
                .build()
                .show();
    }

    private void downCategoryContentList(Category c){
        mTopBar.setTitle(c.name);
        Request request = new Request.Builder().url(Constants.Make_BOOKS_URL(c.aid)).build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                bookList = new Gson().fromJson(json,new TypeToken<List<Book>>(){}.getType());
                bookListFilter.clear();
                bookListFilter.addAll(bookList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(bookList!=null && bookList.size()>0){
                            if(booksAdatper==null) {
                                booksAdatper = new BooksAdatper();
                                content_lv.setAdapter(booksAdatper);
                            }else {
                                booksAdatper.notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(getThisActivity(), R.string.category_content_fail, Toast.LENGTH_LONG).show();
                        }
                        showMaskProcessBar(false);
                    }
                });

            }
        });
    }


    /*
     * 下载设计，下载为全自动下载，添加书籍后自动下载，没有下载完毕则下次启动后自动开始下载
     * */

    /*
    private void downHostList(){
        OkHttpClient http = new OkHttpClient();
        Request request = new Request.Builder().url(Constants.HOST_URL).build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                app.hostList = new Gson().fromJson(json,new TypeToken<List<Host>>(){}.getType());
            }
        });
    }
    */

    private void downCategoryList() {
        final Request request = new Request.Builder().url(Constants.CATEGORY_URL()).build();
        http.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                //Toast.makeText(getThisActivity(), R.string.category_fail, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();
                categoryList = new Gson().fromJson(json,new TypeToken<List<Category>>(){}.getType());
                if(categoryList!=null && categoryList.size()>0){
                    categoryAdatper = new CategoryAdatper();
                    final Category c = categoryList.get(0);
                    categoryAdatper.selectedAid = c.aid;
                    //downHostList();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            category_lv.setAdapter(categoryAdatper);
                            downCategoryContentList(c);
                        }
                    });
                } else {
                    Toast.makeText(getThisActivity(), R.string.category_fail, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    Activity getThisActivity(){
        return this;
    }

    void initTopBar(){
        mTopBar.setTitle(getString(R.string.app_name));

    }

    class CategoryAdatper extends BaseAdapter{
        private LayoutInflater mInflater;
        int selectedAid = -1;
        public CategoryAdatper(){
            mInflater = getThisActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return categoryList.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return categoryList.get(position).aid;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Category c = categoryList.get(position);
            //观察convertView随ListView滚动情况
            if(convertView==null)
                convertView = mInflater.inflate(R.layout.item_category, null);

            convertView.setTag(c);

            LinearLayout layout = (LinearLayout) convertView.findViewById(R.id.layout);
            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            textView.setText(c.name);
            if(c.aid==selectedAid){
                layout.setBackgroundResource(R.color.app_color_theme_3);
            } else {
                layout.setBackgroundResource(R.color.app_color_theme_5);
            }
            return convertView;
        }
    }

    class BooksAdatper extends BaseAdapter{
        private LayoutInflater mInflater;
        public BooksAdatper(){
            mInflater = getThisActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
            return bookListFilter.size();
        }

        @Override
        public Object getItem(int position) {
            return bookListFilter.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Book b = bookListFilter.get(position);
            //观察convertView随ListView滚动情况
            if(convertView==null)
                convertView = mInflater.inflate(R.layout.item_category_content, null);

            convertView.setTag(b);

            ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
            Glide.with(getThisActivity())
                    .load(b.img_s)
                    .into(imageView);

            TextView textView = (TextView) convertView.findViewById(R.id.textView);
            textView.setText(b.fabo_title);

            TextView content = (TextView) convertView.findViewById(R.id.content);
            content.setText(b.fabo_content);

            return convertView;
        }
    }
}
