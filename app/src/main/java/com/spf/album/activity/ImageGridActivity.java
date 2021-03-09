package com.spf.album.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.R;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.ScreenUtils;
import com.spf.album.view.BackTitleBar;

import java.util.ArrayList;
import java.util.List;

public class ImageGridActivity extends BaseActivity {
    private static final String KEY_TITLE = "key_title";
    private static final String KEY_IMAGE_FILE = "key_image_file";
    private BackTitleBar titleBar;
    private RecyclerView recyclerView;

    private final int COLUMN_COUNT = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_grid);
        initView();
        initData();
    }

    protected void initView() {
        titleBar = findViewById(R.id.title_bar);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, COLUMN_COUNT));
    }

    protected void initData() {
        Intent intent = getIntent();
        titleBar.setTitle(intent.getStringExtra(KEY_TITLE));
        recyclerView.setAdapter(new ImageAdapter(this, intent.getParcelableArrayListExtra(KEY_IMAGE_FILE), COLUMN_COUNT));
    }

    static class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
        private Context context;
        private List<ImageFile> imageFileList;
        private int imageSize;

        ImageAdapter(Context context, List<ImageFile> imageFileList, int columnCount) {
            this.context = context;
            this.imageFileList = imageFileList;
            imageSize = (ScreenUtils.getScreenWidth(context) - context.getResources().getDimensionPixelOffset(R.dimen.dp_50)) / columnCount;
        }

        @NonNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageHolder imageHolder = new ImageHolder(LayoutInflater.from(context).inflate(R.layout.item_date_image, parent, false));
            ViewGroup.LayoutParams params = imageHolder.ivImage.getLayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            imageHolder.ivImage.setLayoutParams(params);
            return imageHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
            ImageFile imageFile = imageFileList.get(position);
            ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, imageFile.getUri(), holder.ivImage)
                    .setPlaceHolder(R.drawable.ic_image_placeholder_rect).setSize(imageSize, imageSize));
            if (imageFile.isVideo()) {
                holder.ivPlay.setVisibility(View.VISIBLE);
                holder.tvDuration.setVisibility(View.VISIBLE);
                holder.tvDuration.setText(imageFile.getDuration());
            } else {
                holder.ivPlay.setVisibility(View.GONE);
                holder.tvDuration.setVisibility(View.GONE);
            }
            holder.itemView.setOnClickListener(v -> ImagePreviewActivity.start(context, holder.getAdapterPosition(), (ArrayList<ImageFile>) imageFileList));
        }

        @Override
        public int getItemCount() {
            return imageFileList == null ? 0 : imageFileList.size();
        }
    }

    static class ImageHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private ImageView ivPlay;
        private TextView tvDuration;

        ImageHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            ivPlay = itemView.findViewById(R.id.iv_play);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }

    public static void start(Context context, String title, ArrayList<ImageFile> imageFileList) {
        if (imageFileList == null || imageFileList.isEmpty()) {
            return;
        }
        Intent intent = new Intent(context, ImageGridActivity.class);
        intent.putExtra(KEY_TITLE, title);
        intent.putParcelableArrayListExtra(KEY_IMAGE_FILE, imageFileList);
        context.startActivity(intent);
    }
}