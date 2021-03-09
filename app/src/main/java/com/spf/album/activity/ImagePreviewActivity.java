package com.spf.album.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.chrisbanes.photoview.PhotoView;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.spf.album.R;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;
import com.spf.album.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends BaseActivity {
    private static final String KEY_INDEX = "key_index";
    private static final String KEY_FILE = "key_file";
    private CustomViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);
        initView();
        initData();
    }

    protected void initView() {
        viewPager = findViewById(R.id.view_pager);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LogUtils.d(TAG, "getPlayPosition: " + GSYVideoManager.instance().getPlayPosition());
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    GSYVideoManager.releaseAllVideos();
                }
            }
        });
    }

    protected void initData() {
        Intent intent = getIntent();
        viewPager.setAdapter(new ImagePagerAdapter(this, intent.getParcelableArrayListExtra(KEY_FILE)));
        viewPager.setCurrentItem(intent.getIntExtra(KEY_INDEX, 0));
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

    public static void start(Context context, int index, ArrayList<ImageFile> imageFileList) {
        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(KEY_INDEX, index);
        intent.putParcelableArrayListExtra(KEY_FILE, imageFileList);
        context.startActivity(intent);
    }

    static class ImagePagerAdapter extends PagerAdapter {
        private Context context;
        private List<ImageFile> imageFiles;

        ImagePagerAdapter(Context context, List<ImageFile> imageFiles) {
            this.context = context;
            this.imageFiles = imageFiles;
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
                ImageView thumbImageView = new ImageView(context);
                thumbImageView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, imageFile.getUri(), thumbImageView)
                        .setScaleType(ImageView.ScaleType.FIT_CENTER));

                StandardGSYVideoPlayer videoPlayer = new StandardGSYVideoPlayer(context);
                initVideoPlayer(videoPlayer, imageFile.getPath(), thumbImageView);

                container.addView(videoPlayer, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                return videoPlayer;
            } else {
                PhotoView photoView = new PhotoView(context);
                container.addView(photoView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, imageFile.getUri(), photoView)
                        .setScaleType(ImageView.ScaleType.FIT_CENTER));
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
                    videoPlayer.startWindowFullscreen(context, false, true);
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