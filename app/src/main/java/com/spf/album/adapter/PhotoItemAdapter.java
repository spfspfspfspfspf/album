package com.spf.album.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.R;
import com.spf.album.activity.ImagePreviewActivity;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;
import com.spf.album.utils.ScreenUtils;

import java.util.List;

public class PhotoItemAdapter extends RecyclerView.Adapter<PhotoItemAdapter.ImageViewHolder> {
    private static final String TAG = PhotoItemAdapter.class.getSimpleName();
    private final int TYPE_IMAGE = 1;
    private final int TYPE_VIDEO = 2;
    private final Context context;
    private final List<ImageFile> imageFiles;
    private final int imageSize;

    PhotoItemAdapter(Context context, List<ImageFile> imageFiles, int columnCount) {
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
    public PhotoItemAdapter.ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        PhotoItemAdapter.ImageViewHolder imageHolder;
        if (viewType == TYPE_VIDEO) {
            imageHolder = new PhotoItemAdapter.VideoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_data_video, parent, false));
        } else {
            imageHolder = new PhotoItemAdapter.ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_data_image, parent, false));
        }
        ViewGroup.LayoutParams params = imageHolder.ivImage.getLayoutParams();
        params.width = imageSize;
        params.height = imageSize;
        imageHolder.ivImage.setLayoutParams(params);
        return imageHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoItemAdapter.ImageViewHolder holder, int position) {
        LogUtils.d(TAG, "onBindViewHolder: " + position);
        ImageFile imageFile = imageFiles.get(position);
        ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, imageFile.getUri(), holder.ivImage)
                .setPlaceHolder(R.drawable.ic_image_placeholder_rect).setSize(imageSize, imageSize));
        if (imageFile.isVideo() && holder instanceof PhotoItemAdapter.VideoViewHolder) {
            PhotoItemAdapter.VideoViewHolder videoHolder = (PhotoItemAdapter.VideoViewHolder) holder;
            videoHolder.tvDuration.setText(imageFile.getDuration());
        }
        holder.itemView.setOnClickListener(v -> ImagePreviewActivity.start(context, holder.getAdapterPosition(), imageFile.getPath()));
    }

    @Override
    public int getItemCount() {
        return imageFiles == null ? 0 : imageFiles.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        protected ImageView ivImage;

        ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
        }
    }

    static class VideoViewHolder extends ImageViewHolder {
        protected ImageView ivPlay;
        protected TextView tvDuration;

        VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            ivPlay = itemView.findViewById(R.id.iv_play);
            tvDuration = itemView.findViewById(R.id.tv_duration);
        }
    }
}
