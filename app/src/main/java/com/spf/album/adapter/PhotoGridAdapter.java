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
import com.spf.album.utils.ScreenUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhotoGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int COLUMN_COUNT = 4;
    private final int TYPE_TITLE = 1;
    private final int TYPE_IMAGE = 2;
    private final int TYPE_VIDEO = 3;
    private Context context;
    private List<ImageFile> imageList;
    private final int imageSize;

    public PhotoGridAdapter(Context context) {
        this.context = context;
        this.imageList = new ArrayList<>();
        this.imageSize = (ScreenUtils.getScreenWidth()
                - context.getResources().getDimensionPixelOffset(R.dimen.dp_50)) / COLUMN_COUNT;
    }

    public void setImageFiles(Map<String, List<ImageFile>> imageMaps) {
        imageList.clear();
        for (Map.Entry<String, List<ImageFile>> entry : imageMaps.entrySet()) {
            imageList.add(ImageFile.createImageTitle(entry.getKey()));
            imageList.addAll(entry.getValue());
        }
        notifyDataSetChanged();
    }

    public int getPosition(ImageFile imageFile) {
        int i = 0;
        for (; i < imageList.size(); i++) {
            if (imageList.get(i) == imageFile) {
                return i;
            }
        }
        return i;
    }

    public ImageFile getImageFile(int position) {
        if (position < 0 || position >= imageList.size()) {
            return null;
        }
        return imageList.get(position);
    }

    public int getSpanSize(int position) {
        ImageFile imageFile = imageList.get(position);
        if (TYPE_TITLE == getItemViewType(position)) {
            return COLUMN_COUNT;
        } else {
            return 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        ImageFile imageFile = imageList.get(position);
        if (imageFile.isTitle()) {
            return TYPE_TITLE;
        } else if (imageFile.isVideo()) {
            return TYPE_VIDEO;
        } else {
            return TYPE_IMAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TITLE) {
            return new TitleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_title, parent, false));
        } else {
            ImageViewHolder imageHolder;
            if (viewType == TYPE_VIDEO) {
                imageHolder = new VideoViewHolder(LayoutInflater.from(context).inflate(R.layout.item_data_video, parent, false));
            } else {
                imageHolder = new ImageViewHolder(LayoutInflater.from(context).inflate(R.layout.item_data_image, parent, false));
            }
            ViewGroup.LayoutParams params = imageHolder.ivImage.getLayoutParams();
            params.width = imageSize;
            params.height = imageSize;
            imageHolder.ivImage.setLayoutParams(params);
            return imageHolder;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ImageFile imageFile = imageList.get(position);
        if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).tvTitle.setText(imageFile.getDate());
        } else {
            ImageViewHolder imageHolder = (ImageViewHolder) holder;
            ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(context, imageFile.getUri(), imageHolder.ivImage)
                    .setPlaceHolder(R.drawable.ic_image_placeholder_rect).setSize(imageSize, imageSize));
            if (imageFile.isVideo() && holder instanceof VideoViewHolder) {
                ((VideoViewHolder) holder).tvDuration.setText(imageFile.getDuration());
            }
            holder.itemView.setOnClickListener(v -> ImagePreviewActivity.start(context, imageFile.getPath()));
        }
    }

    @Override
    public int getItemCount() {
        return imageList == null ? 0 : imageList.size();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
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
