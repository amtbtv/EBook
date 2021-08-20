package com.hwadzan.ebook.ui;

import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.hwadzan.ebook.R;

public class BookViewHolder  extends RecyclerView.ViewHolder {
    CardView cardView;
    ImageView img;
    TextView title;
    TextView process;

    public BookViewHolder(View view, int imageWidth, int imageHeight) {
        super(view);
        cardView = (CardView) view;
        img = (ImageView) view.findViewById(R.id.img);
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) img.getLayoutParams();
        lp.width = imageWidth;
        lp.height = imageHeight;
        img.setLayoutParams(lp);
        title = (TextView) view.findViewById(R.id.title);
        process = (TextView) view.findViewById(R.id.process);
    }
}
