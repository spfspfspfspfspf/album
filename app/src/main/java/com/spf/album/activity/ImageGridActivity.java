package com.spf.album.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.ImageFileLoader;
import com.spf.album.R;
import com.spf.album.model.FolderInfo;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;
import com.spf.album.utils.ScreenUtils;
import com.spf.album.view.BackTitleBar;

import java.util.List;

public class ImageGridActivity extends BaseActivity {
    private static final String TAG = ImageGridActivity.class.getSimpleName();
    private static final String KEY_PATH = "key_path";
    private BackTitleBar titleBar;
    private RecyclerView recyclerView;

    private final int COLUMN_COUNT = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        initStatusBar();
        initView();
        initData();
    }

    private void initStatusBar() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
    }

    protected void initView() {
        titleBar = findViewById(R.id.title_bar);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, COLUMN_COUNT));
    }

    protected void initData() {
        String path = getIntent().getStringExtra(KEY_PATH);
        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }
        FolderInfo folderInfo = null;
        for (FolderInfo item : ImageFileLoader.getInstance().getFolderList()) {
            if (path.equals(item.getPath())) {
                folderInfo = item;
                break;
            }
        }
        if (folderInfo == null) {
            finish();
            return;
        }
        titleBar.setTitle(folderInfo.getName());
        recyclerView.setAdapter(new ImageAdapter(this, folderInfo.getImageFiles(), COLUMN_COUNT));
    }

    static class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
        private final int TYPE_IMAGE = 1;
        private final int TYPE_VIDEO = 2;
        private final Context context;
        private final List<ImageFile> imageFiles;
        private final int imageSize;

        ImageAdapter(Context context, List<ImageFile> imageFiles, int columnCount) {
            this.context = context;
            this.imageFiles = imageFiles;
            this.imageSize = (ScreenUtils.getScreenWidth()
                    - context.getResources().getDimensionPixelOffset(R.dimen.dp_50)) / columnCount;
        }

        @Override
        public int getItemViewType(int position) {
            ImageFile imageFile = imageFiles.get(position);
            if (imageFile.isImage()) {
                return TYPE_IMAGE;
            } else if (imageFile.isVideo()) {
                return TYPE_VIDEO;
            } else {
                return TYPE_IMAGE;
            }
        }

        @NonNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageHolder imageHolder;
            if (viewType == TYPE_VIDEO) {
                imageHolder = new VideoHolder(LayoutInflater.from(context).inflate(R.layout.item_data_video, parent, false));
            } else {
                imageHolder = new ImageHolder(LayoutInflater.from(context).inflate(R.layout.item_data_image, parent, false));
            }
            ViewGroup.LayoutParams params = imageHolder.ivImage.getLayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            imageHolder.ivImage.setLayoutParams(params);
            return imageHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
            LogUtils.d(TAG, "onBindViewHolder: " + position);
            ImageFile imageFile = imageFiles.get(position);
            ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, imageFile.getUri(), holder.ivImage)
                    .setPlaceHolder(R.drawable.ic_image_placeholder_rect).setSize(imageSize, imageSize));
            if (imageFile.isVideo() && holder instanceof VideoHolder) {
                VideoHolder videoHolder = (VideoHolder) holder;
                videoHolder.tvDuration.setText(imageFile.getDuration());
            }
            holder.itemView.setOnClickListener(v -> ImagePreviewActivity.start(context, imageFile.getPath()));
        }

        @Override
        public int getItemCount() {
            return imageFiles == null ? 0 : imageFiles.size();
        }
    }

    static class ImageHolder extends RecyclerView.ViewHolder {
        protected ImageView ivImage;

        ImageHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
        }
    }

    static class VideoHolder extends ImageHolder {
        protected ImageView ivPlay;
        protected TextView tvDuration;

        VideoHolder(@NonNull View itemView) {
            super(itemView);
            ivPlay = itemView.findViewById(R.id.iv_play);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }

    public static void start(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Intent intent = new Intent(context, ImageGridActivity.class);
        intent.putExtra(KEY_PATH, path);
        context.startActivity(intent);
    }
}