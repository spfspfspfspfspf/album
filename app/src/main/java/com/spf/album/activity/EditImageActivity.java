package com.spf.album.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.spf.album.GalleryApplication;
import com.spf.album.R;
import com.spf.album.databinding.ActivityEditImageBinding;
import com.spf.album.event.CloseEditEvent;
import com.spf.album.event.WordMarkEvent;
import com.spf.album.utils.DialogUtils;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.ScreenUtils;
import com.spf.album.view.DrawImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class EditImageActivity extends BaseActivity implements View.OnClickListener {
    private static final String KEY_PATH = "key_path";
    private ActivityEditImageBinding binding;
    private CompositeDisposable disposable;
    private int bottomHeight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_edit_image);
        initStatusBar();
        initView();
        initData();
        EventBus.getDefault().register(this);
    }

    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    private void initData() {
        String path = getIntent().getStringExtra(KEY_PATH);
        bottomHeight = GalleryApplication.getApplication().getResources().getDimensionPixelOffset(R.dimen.dp_50);
        disposable = new CompositeDisposable();
        disposable.add(Observable.create(new ObservableOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Bitmap> emitter) throws Throwable {
                Bitmap bitmap = ImageLoadUtils.getBitmap(new ImageLoadUtils.ImageBuilder(EditImageActivity.this, path)
                        .setSize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight() - 2 * bottomHeight)
                        .setScaleType(ImageView.ScaleType.FIT_CENTER));
                emitter.onNext(bitmap);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Bitmap>() {
                    @Override
                    public void accept(Bitmap bitmap) throws Throwable {
                        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) binding.img.getLayoutParams();
                        params.width = bitmap.getWidth();
                        params.height = bitmap.getHeight();
                        binding.img.setLayoutParams(params);
                        binding.img.setImageBitmap(bitmap);
                    }
                }));
    }

    private void initView() {
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
            DialogUtils.showWordMarkDialog(this);
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
                saveImage();
            }
        }
    }

    public void saveImage() {
        Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(@androidx.annotation.NonNull ObservableEmitter<File> emitter) throws Throwable {
                emitter.onNext(ImageLoadUtils.saveToGallery(binding.img));
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Throwable {
                        Context context = GalleryApplication.getApplication();
                        if (file == null) {
                            Toast.makeText(context, R.string.image_save_fail, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, context.getResources().getString(
                                    R.string.image_save_success, file.getAbsolutePath()), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventCloseEdit(CloseEditEvent event) {
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventWordMark(WordMarkEvent event) {
        binding.img.setMode(DrawImageView.MODE_NONE);
        binding.tabWord.setSelect(false);
        if (TextUtils.isEmpty(event.getContent())) {
            return;
        }
        binding.img.addWord(event.getContent(), event.getColor());
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        super.onDestroy();
    }

    public static void start(Context context, String path) {
        Intent intent = new Intent(context, EditImageActivity.class);
        intent.putExtra(KEY_PATH, path);
        context.startActivity(intent);
    }
}