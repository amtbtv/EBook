package com.hwadzan.ebook.lib;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class PdfLinearLayout extends LinearLayout {
    boolean touchFlag;
    long touchTime;

    public void setOnPositionClickListener(OnPositionClickListener onPositionClickListener) {
        this.onPositionClickListener = onPositionClickListener;
    }
    OnPositionClickListener onPositionClickListener;

    public PdfLinearLayout(Context context) {
        super(context);
    }

    public PdfLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // 返回 true 拦截事件，不再传递
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchFlag = false;
                touchTime = System.currentTimeMillis();
                break;
            case MotionEvent.ACTION_MOVE:
                touchFlag = true;
                break;
            case MotionEvent.ACTION_UP:
                if (!touchFlag && System.currentTimeMillis()-touchTime<1000){
                    if(onPositionClickListener!=null){
                        onPositionClickListener.onClick(this, ev.getX(), ev.getY());
                    }
                }
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }
}
