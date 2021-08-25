package com.hwadzan.ebook.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hwadzan.ebook.R;
import com.hwadzan.ebook.lib.GlideApp;
import com.hwadzan.ebook.model.Book;
import com.hwadzan.ebook.model.BookManager;

public class BookAdapter  extends RecyclerView.Adapter<BookViewHolder>{
    private Context mContext;
    private BookManager bookManager;
    private int imageWidth, imageHeight;

    private View.OnClickListener onClickListener;
    private View.OnLongClickListener onLongClickListener;

    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public void setOnLongClickListener(View.OnLongClickListener onLongClickListener) {
        this.onLongClickListener = onLongClickListener;
    }


    public BookAdapter(Context mContext, BookManager bookManager, int imageWidth, int imageHeight){
        this.mContext = mContext;
        this.bookManager = bookManager;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }

    @NonNull
    @Override
    public BookViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_book, viewGroup, false);
        view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(onClickListener!=null)
                                            onClickListener.onClick(v);
                                    }
                                });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(onLongClickListener!=null)
                    return onLongClickListener.onLongClick(v);
                return false;
            }
        });

        return new BookViewHolder(view, imageWidth, imageHeight);
    }

    @Override
    public void onBindViewHolder(@NonNull BookViewHolder bookViewHolder, int i) {
        Book b = bookManager.getBookList().get(i);
        bookViewHolder.title.setText(b.fabo_title);
        if (b.downloaded) {
            bookViewHolder.process.setVisibility(View.INVISIBLE);
        } else {
            bookViewHolder.process.setVisibility(View.VISIBLE);
            bookViewHolder.process.setText(String.valueOf(b.downloaProcess));
        }
        bookViewHolder.cardView.setTag(b);

        GlideApp.with(mContext).load(b.img_s).into(bookViewHolder.img);
    }

    @Override
    public int getItemCount() {
        return bookManager.getBookList().size();
    }
}
