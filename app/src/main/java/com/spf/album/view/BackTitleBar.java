package com.spf.album.view;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.spf.album.R;

public class BackTitleBar extends ConstraintLayout {
    private ImageView ivBack;
    private TextView tvTitle;

    public BackTitleBar(Context context) {
        this(context, null);
    }

    public BackTitleBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BackTitleBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.title_bar_back, this);
        ivBack = findViewById(R.id.iv_back);
        tvTitle = findViewById(R.id.tv_title);

        ivBack.setOnClickListener(v -> {
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });

        if (attrs != null) {
            TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.BackTitleBar);
            int resId = ta.getResourceId(R.styleable.BackTitleBar_title, R.string.default_title);
            tvTitle.setText(resId);
            ta.recycle();
        }
    }

    public void setTitle(String title) {
        this.tvTitle.setText(title);
    }
}