package com.hwadzan.ebook.ui;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.hwadzan.ebook.R;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBarLayout;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);
        QMUIStatusBarHelper.translucent(this);

        QMUITopBarLayout mTopBar = (QMUITopBarLayout) findViewById(R.id.topbar);
        mTopBar.setTitle(getString(R.string.app_name));
    }
}
