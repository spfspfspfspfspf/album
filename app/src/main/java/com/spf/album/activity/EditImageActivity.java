package com.spf.album.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.spf.album.R;
import com.spf.album.databinding.ActivityEditImageBinding;
import com.spf.album.event.CloseEditEvent;
import com.spf.album.utils.AppExecutors;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.ScreenUtils;
import com.spf.album.view.DrawImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class EditImageActivity extends BaseActivity implements View.OnClickListener {
    private static final String KEY_URI = "key_uri";
    private ActivityEditImageBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_image);
        initView();
        initData();
        EventBus.getDefault().register(this);
    }

    private void initData() {
        Uri uri = getIntent().getParcelableExtra(KEY_URI);
        AppExecutors.getInstance().runOnBackground(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = ImageLoadUtils.getBitmap(new ImageLoadUtils.ImageBuilder(EditImageActivity.this, uri)
                        .setSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight() - binding.llBottom.getHeight())
                        .setScaleType(ImageView.ScaleType.FIT_CENTER));
                ViewGroup.LayoutParams params = binding.img.getLayoutParams();
                params.width = bitmap.getWidth();
                params.height = bitmap.getHeight();
                binding.img.setImageBitmap(bitmap);
            }
        });
    }

    private void initView() {
        binding.tabLine.setSelect(true);
        binding.img.setMode(DrawImageView.MODE_LINE);
        binding.tvCancelEdit.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                binding.tvCancelEdit.setRect(new Rect(0, 0,
                        ScreenUtils.getScreenWidth(), binding.llBottom.getTop()));
            }
        });

        binding.tabLine.setOnClickListener(this);
        binding.tabWord.setOnClickListener(this);
        binding.tabMosaic.setOnClickListener(this);
        binding.tvCancelEdit.setOnClickListener(this);
        binding.tvSaveEdit.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (R.id.tab_line == id) {
            binding.img.setMode(DrawImageView.MODE_LINE);
            binding.tabLine.setSelect(true);
            binding.tabWord.setSelect(false);
            binding.tabMosaic.setSelect(false);
        } else if (R.id.tab_word == id) {
            binding.img.setMode(DrawImageView.MODE_WORD);
            binding.tabLine.setSelect(false);
            binding.tabWord.setSelect(true);
            binding.tabMosaic.setSelect(false);
        } else if (R.id.tab_mosaic == id) {
            binding.img.setMode(DrawImageView.MODE_MOSAIC);
            binding.tabLine.setSelect(false);
            binding.tabWord.setSelect(false);
            binding.tabMosaic.setSelect(true);
        } else if (R.id.tv_cancel_edit == id) {
            if (!binding.tvCancelEdit.isDrag()) {
                binding.img.cancelEdit();
            }
        } else if (R.id.tv_save_edit == id) {
            if (!binding.tvSaveEdit.isDrag()) {
                binding.img.saveImage();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventCloseEdit(CloseEditEvent event) {
        finish();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    public static void start(Context context, Uri uri) {
        Intent intent = new Intent(context, EditImageActivity.class);
        intent.putExtra(KEY_URI, uri);
        context.startActivity(intent);
    }
}