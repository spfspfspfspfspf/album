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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.ImageFile;
import com.spf.album.R;
import com.spf.album.databinding.ActivityImageGridBinding;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;

public class ImageGridActivity extends AppCompatActivity {
    private static final String KEY_TITLE = "key_title";
    private static final String KEY_IMAGE_FILE = "key_image_file";
    private ActivityImageGridBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_grid);
        Intent intent = getIntent();
        String title = intent.getStringExtra(KEY_TITLE);
        binding.titleBar.setTitle(title);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        binding.recyclerView.setAdapter(new ImageAdapter(this, intent.getParcelableArrayListExtra(KEY_IMAGE_FILE)));
    }

    static class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {
        private Context context;
        private List<ImageFile> imageFileList;

        ImageAdapter(Context context, List<ImageFile> imageFileList) {
            this.context = context;
            this.imageFileList = imageFileList;
        }

        @NonNull
        @Override
        public ImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ImageHolder imageHolder = new ImageHolder(LayoutInflater.from(context).inflate(R.layout.item_date_image, parent, false));
            ViewGroup.LayoutParams params = imageHolder.clRoot.getLayoutParams();
            params.width = ScreenUtils.getScreenWidth(context) / 4;
            params.height = params.width;
            imageHolder.clRoot.setLayoutParams(params);
            return imageHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull ImageHolder holder, int position) {
            ImageFile imageFile = imageFileList.get(position);
            ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, imageFile.getUri(), holder.ivImage)
                    .setPlaceHolder(R.drawable.ic_image_placeholder_rect));
            if (imageFile.isVideo()) {
                holder.ivPlay.setVisibility(View.VISIBLE);
                holder.tvDuration.setVisibility(View.VISIBLE);
                holder.tvDuration.setText(imageFile.getDuration());
            } else {
                holder.ivPlay.setVisibility(View.GONE);
                holder.tvDuration.setVisibility(View.GONE);
            }
            holder.clRoot.setOnClickListener(v -> ImagePreviewActivity.start(context, holder.getAdapterPosition(), imageFileList));
        }

        @Override
        public int getItemCount() {
            return imageFileList == null ? 0 : imageFileList.size();
        }
    }

    static class ImageHolder extends RecyclerView.ViewHolder {
        private ConstraintLayout clRoot;
        private ImageView ivImage;
        private ImageView ivPlay;
        private TextView tvDuration;

        ImageHolder(@NonNull View itemView) {
            super(itemView);
            clRoot = itemView.findViewById(R.id.cl_root);
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