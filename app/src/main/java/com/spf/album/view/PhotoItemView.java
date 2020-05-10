package com.spf.album.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.spf.album.ImageFile;
import com.spf.album.R;
import com.spf.album.event.PhotoImageClickEvent;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.ScreenUtils;

import org.greenrobot.eventbus.EventBus;

public class PhotoItemView extends ConstraintLayout {
    private ConstraintLayout clRoot;
    private ImageView ivImage;
    private ImageView ivPlay;
    private TextView tvDuration;

    public PhotoItemView(Context context) {
        super(context);
        initView(context);
    }

    public PhotoItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.item_date_image, this);

        clRoot = findViewById(R.id.cl_root);
        ViewGroup.LayoutParams layoutParams = clRoot.getLayoutParams();
        layoutParams.width = (ScreenUtils.getScreenWidth(context) - context.getResources().getDimensionPixelOffset(R.dimen.dp_10)) / 4;
        layoutParams.height = layoutParams.width;
        clRoot.setLayoutParams(layoutParams);

        ivImage = findViewById(R.id.iv_image);
        ivPlay = findViewById(R.id.iv_play);
        tvDuration = findViewById(R.id.tv_duration);
    }

    public void setImageFile(ImageFile imageFile) {
        ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(getContext(), imageFile.getUri(), ivImage)
                .setPlaceHolder(R.drawable.ic_image_placeholder_rect));
        if (imageFile.isVideo()) {
            ivPlay.setVisibility(VISIBLE);
            tvDuration.setVisibility(VISIBLE);
            tvDuration.setText(imageFile.getDuration());
        } else {
            ivPlay.setVisibility(GONE);
            tvDuration.setVisibility(GONE);
        }
        clRoot.setOnClickListener(v -> EventBus.getDefault().post(new PhotoImageClickEvent(imageFile)));
    }
}