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
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.viewpager.widget.PagerAdapter;

import com.spf.album.ImageFile;
import com.spf.album.R;
import com.spf.album.databinding.ActivityImagePreviewBinding;
import com.spf.album.utils.ImageLoadUtils;

import java.util.ArrayList;
import java.util.List;

public class ImagePreviewActivity extends AppCompatActivity {
    private static final String KEY_INDEX = "key_index";
    private static final String KEY_PATH = "key_path";
    private static final String KEY_URI = "key_uri";
    private ActivityImagePreviewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_preview);
        Intent intent = getIntent();
        int index = intent.getIntExtra(KEY_INDEX, 0);
        List<String> pathList = intent.getStringArrayListExtra(KEY_PATH);
        List<Uri> uriList = intent.getParcelableArrayListExtra(KEY_URI);
        binding.viewPager.setAdapter(new ImagePagerAdapter(this, pathList, uriList));
        binding.viewPager.setCurrentItem(index);
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
                view.findViewById(R.id.iv_play).setVisibility(View.VISIBLE);
            }
            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}