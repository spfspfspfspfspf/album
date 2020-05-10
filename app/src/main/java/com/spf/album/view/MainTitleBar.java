package com.spf.album.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.spf.album.R;

public class MainTitleBar extends ConstraintLayout {
    private TextView tvTitle;

    public MainTitleBar(Context context) {
        this(context, null);
    }

    public MainTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MainTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.title_bar_main, this);
        tvTitle = findViewById(R.id.tv_title);

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.MainTitleBar);
            int resId = ta.getResourceId(R.styleable.MainTitleBar_title, R.string.default_title);
            tvTitle.setText(resId);
            ta.recycle();
        }
    }

    public void setTitle(String title) {
        this.tvTitle.setText(title);
    }
}