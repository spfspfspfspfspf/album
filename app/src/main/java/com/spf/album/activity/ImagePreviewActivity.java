package com.spf.album.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.PhotoView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.spf.album.ImageFileLoader;
import com.spf.album.R;
import com.spf.album.databinding.ActivityImagePreviewBinding;
import com.spf.album.model.FolderInfo;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;

import java.util.List;

public class ImagePreviewActivity extends BaseActivity {
    private static final String TAG = ImagePreviewActivity.class.getSimpleName();
    private static final String KEY_PATH = "key_path";
    private ActivityImagePreviewBinding binding;
    private boolean isTopBottomVisible = true;
    private ImagePagerAdapter mImageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview);
        initStatusBar();
        initView();
        initData();
    }

    private void initStatusBar() {
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.getAttributes().layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        updateStatusBarVisibility();
    }

    protected void initView() {
        binding.viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LogUtils.d(TAG, "getPlayPosition: " + GSYVideoManager.instance().getPlayPosition());
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    GSYVideoManager.releaseAllVideos();
                }
            }
        });
        binding.tabEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageFile imageFile = mImageAdapter.getImageFile(binding.viewPager.getCurrentItem());
                if (imageFile == null) {
                    return;
                }
                EditImageActivity.start(ImagePreviewActivity.this, imageFile.getPath());
            }
        });
    }

    protected void initData() {
        String path = getIntent().getStringExtra(KEY_PATH);
        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }
        FolderInfo folderInfo = null;
        for (FolderInfo item : ImageFileLoader.getInstance().getFolderList()) {
            if (path.contains(item.getPath())) {
                folderInfo = item;
                break;
            }
        }
        if (folderInfo == null || folderInfo.getImageFiles().isEmpty()) {
            finish();
            return;
        }
        List<ImageFile> imageFiles = folderInfo.getImageFiles();
        int index = imageFiles.size() - 1;
        for (; index >= 0; index--) {
            if (path.equals(imageFiles.get(index).getPath())) {
                break;
            }
        }
        mImageAdapter = new ImagePagerAdapter(this, imageFiles);
        binding.viewPager.setAdapter(mImageAdapter);
        binding.viewPager.setCurrentItem(index);
    }

    private void updateStatusBarVisibility() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }
        isTopBottomVisible = !isTopBottomVisible;
        if (isTopBottomVisible) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            binding.llBottom.setVisibility(View.VISIBLE);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
            binding.llBottom.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
    }

    public static void start(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(KEY_PATH, path);
        context.startActivity(intent);
    }

    static class ImagePagerAdapter extends PagerAdapter {
        private ImagePreviewActivity activity;
        private List<ImageFile> imageFiles;

        ImagePagerAdapter(ImagePreviewActivity activity, List<ImageFile> imageFiles) {
            this.activity = activity;
            this.imageFiles = imageFiles;
        }

        public ImageFile getImageFile(int position) {
            if (imageFiles == null || imageFiles.isEmpty()) {
                return null;
            }
            return imageFiles.get(position);
        }

        @Override
        public int getCount() {
            return imageFiles == null ? 0 : imageFiles.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            ImageFile imageFile = imageFiles.get(position);
            if (imageFile.isVideo()) {
                ImageView thumbImageView = new ImageView(activity);
                thumbImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(activity, imageFile.getPath(), thumbImageView)
                        .setScaleType(ImageView.ScaleType.FIT_CENTER));

                StandardGSYVideoPlayer videoPlayer = new StandardGSYVideoPlayer(activity);
                initVideoPlayer(videoPlayer, imageFile.getPath(), thumbImageView);

                container.addView(videoPlayer, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return videoPlayer;
            } else {
                PhotoView photoView = new PhotoView(activity);
                container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(activity, imageFile.getPath(), photoView)
                        .setScaleType(ImageView.ScaleType.FIT_CENTER));

                photoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.updateStatusBarVisibility();
                    }
                });

                return photoView;
            }
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        private void initVideoPlayer(StandardGSYVideoPlayer videoPlayer, String path, ImageView thumbImageView) {
            videoPlayer.setUpLazy(path, true, null, null, "");
            videoPlayer.getThumbImageViewLayout().setVisibility(View.VISIBLE);
            videoPlayer.setThumbImageView(thumbImageView);
            //增加title
            videoPlayer.getTitleTextView().setVisibility(View.GONE);
            //设置返回键
            videoPlayer.getBackButton().setVisibility(View.GONE);
            //设置全屏按键功能
            videoPlayer.getFullscreenButton().setVisibility(View.GONE);
            videoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    videoPlayer.startWindowFullscreen(activity, false, true);
                }
            });
            //防止错位设置
            videoPlayer.setPlayTag(path);
            videoPlayer.setPlayPosition(0);
            //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
            videoPlayer.setAutoFullWithSize(true);
            //音频焦点冲突时是否释放
            videoPlayer.setReleaseWhenLossAudio(false);
            //全屏动画
            videoPlayer.setShowFullAnimation(true);
            //小屏时不触摸滑动
            videoPlayer.setIsTouchWiget(false);
        }
    }
}