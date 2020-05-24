package com.spf.album.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.spf.album.ImageFile;
import com.spf.album.R;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;
import com.spf.album.view.CustomViewPager;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends BaseActivity {
    private static final String KEY_INDEX = "key_index";
    private static final String KEY_PATH = "key_path";
    private static final String KEY_URI = "key_uri";
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
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                LogUtils.d(TAG, "getPlayPosition: " + GSYVideoManager.instance().getPlayPosition());
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    GSYVideoManager.releaseAllVideos();
                }
            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    protected void initData() {
        Intent intent = getIntent();
        viewPager.setAdapter(new ImagePagerAdapter(this,
                intent.getStringArrayListExtra(KEY_PATH),
                intent.getParcelableArrayListExtra(KEY_URI)));
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

    public static void start(Context context, int index, List<ImageFile> imageFileList) {
        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        Intent intent = new Intent(context, ImagePreviewActivity.class);
        intent.putExtra(KEY_INDEX, index);
        ArrayList<String> pathList = new ArrayList<>(imageFileList.size());
        ArrayList<Uri> uriList = new ArrayList<>(imageFileList.size());
        for (ImageFile imageFile : imageFileList) {
            pathList.add(imageFile.getPath());
            uriList.add(imageFile.getUri());
        }
        intent.putStringArrayListExtra(KEY_PATH, pathList);
        intent.putParcelableArrayListExtra(KEY_URI, uriList);
        context.startActivity(intent);
    }

    static class ImagePagerAdapter extends PagerAdapter {
        private Context context;
        private List<String> pathList;
        private List<Uri> uriList;

        ImagePagerAdapter(Context context, List<String> pathList, List<Uri> uriList) {
            this.context = context;
            this.pathList = pathList;
            this.uriList = uriList;
        }

        @Override
        public int getCount() {
            return uriList == null ? 0 : uriList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View view = LayoutInflater.from(container.getContext()).inflate(R.layout.item_image_preview, container, false);
            container.addView(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, uriList.get(position), view.findViewById(R.id.pv_image))
                    .setScaleType(ImageView.ScaleType.FIT_CENTER));
            if (pathList != null && pathList.size() > position && pathList.get(position).endsWith("mp4")) {
                StandardGSYVideoPlayer videoPlayer = view.findViewById(R.id.video_player);
                videoPlayer.setVisibility(View.VISIBLE);
                initVideoPlayer(videoPlayer, pathList.get(position));
                //videoPlayer.setThumbImageView(view.findViewById(R.id.pv_image));
            }
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }

        private void initVideoPlayer(StandardGSYVideoPlayer videoPlayer, String path) {
            videoPlayer.setUpLazy(path, true, null, null, "");
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