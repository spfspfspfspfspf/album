package com.spf.album.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.spf.album.R;

public class BottomBarView extends LinearLayout {
    private ImageView ivIcon;
    private TextView tvText;

    private boolean isSelect = false;
    private int imgSelectResId;
    private int imgNormalResId;

    public BottomBarView(Context context) {
        this(context, null);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomBarView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_bottom_bar, this);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        ivIcon = findViewById(R.id.iv_icon);
        tvText = findViewById(R.id.tv_text);

        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BottomBarView);
            isSelect = typedArray.getBoolean(R.styleable.BottomBarView_selected, false);
            imgSelectResId = typedArray.getResourceId(R.styleable.BottomBarView_img_select, -1);
            imgNormalResId = typedArray.getResourceId(R.styleable.BottomBarView_img_normal, -1);
            if (isSelect) {
                ivIcon.setImageResource(imgSelectResId);
                tvText.setTextColor(context.getResources().getColor(R.color.tab_text_color_select));
            } else {
                ivIcon.setImageResource(imgNormalResId);
                tvText.setTextColor(context.getResources().getColor(R.color.tab_text_color_normal));
            }

            int txtContentId = typedArray.getResourceId(R.styleable.BottomBarView_txt_content, -1);
            if (txtContentId > 0) {
                tvText.setText(txtContentId);
            }
            typedArray.recycle();
        }
    }

    public void setSelect(boolean flag) {
        if (isSelect == flag) {
            return;
        }
        isSelect = flag;
        if (isSelect) {
            ivIcon.setImageResource(imgSelectResId);
            tvText.setTextColor(getContext().getResources().getColor(R.color.tab_text_color_select));
        } else {
            ivIcon.setImageResource(imgNormalResId);
            tvText.setTextColor(getContext().getResources().getColor(R.color.tab_text_color_normal));
        }
    }

}