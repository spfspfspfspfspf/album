package com.spf.album.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.R;
import com.spf.album.model.ImageFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhotoRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_TITLE = 1;
    private final int TYPE_RECYCLER = 2;
    private Context context;
    private RecyclerView.RecycledViewPool recyclerPool;
    private List<List<ImageFile>> imageDataList;

    public PhotoRecyclerAdapter(Context context) {
        this.context = context;
        imageDataList = new ArrayList<>();
        recyclerPool = new RecyclerView.RecycledViewPool();
    }

    public void setImageFiles(Map<String, List<ImageFile>> imageMaps) {
        imageDataList.clear();
        for (Map.Entry<String, List<ImageFile>> entry : imageMaps.entrySet()) {
            List<ImageFile> imageTitle = new ArrayList<>(1);
            imageTitle.add(ImageFile.createImageTitle(entry.getKey()));
            imageDataList.add(imageTitle);

            List<ImageFile> itemData = null;
            for (int i = 0; i < entry.getValue().size(); i++) {
                if (i % 4 == 0) {
                    itemData = new ArrayList<>(4);
                    imageDataList.add(itemData);
                }
                itemData.add(entry.getValue().get(i));
            }
        }
        notifyDataSetChanged();
    }

    public int getPosition(ImageFile imageFile) {
        int i = 0;
        for (; i < imageDataList.size(); i++) {
            for (ImageFile target : imageDataList.get(i)) {
                if (target == imageFile) {
                    return i;
                }
            }
        }
        return i;
    }

    public ImageFile getImageFile(int position) {
        if (imageDataList.size() > position) {
            return imageDataList.get(position).get(0);
        }
        return null;
    }

    @Override
    public int getItemViewType(int position) {
        List<ImageFile> imageFiles = imageDataList.get(position);
        int size = imageFiles.size();
        if (size > 0) {
            if (size == 1 && imageFiles.get(0).getId() == ImageFile.ID_TITLE) {
                return TYPE_TITLE;
            } else {
                return TYPE_RECYCLER;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TITLE) {
            return new PhotoRecyclerAdapter.TitleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_title, parent, false));
        } else if (viewType == TYPE_RECYCLER) {
            return new PhotoRecyclerAdapter.RecyclerViewHolder(LayoutInflater.from(context).inflate(R.layout.item_data_recycler, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        List<ImageFile> imageFiles = imageDataList.get(position);
        if (holder instanceof PhotoRecyclerAdapter.TitleViewHolder) {
            ((PhotoRecyclerAdapter.TitleViewHolder) holder).tvTitle.setText(imageFiles.get(0).getDate());
        } else if (holder instanceof PhotoRecyclerAdapter.RecyclerViewHolder) {
            PhotoRecyclerAdapter.RecyclerViewHolder recyclerViewHolder = (PhotoRecyclerAdapter.RecyclerViewHolder) holder;

            recyclerViewHolder.recyclerView.setAdapter(new PhotoItemAdapter(context, imageFiles, 4));
        }
    }

    @Override
    public int getItemCount() {
        return imageDataList == null ? 0 : imageDataList.size();
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }

    class RecyclerViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        RecyclerViewHolder(@NonNull View itemView) {
            super(itemView);
            recyclerView = itemView.findViewById(R.id.recycler_view);
            GridLayoutManager manager = new GridLayoutManager(itemView.getContext(), 4);
            manager.setRecycleChildrenOnDetach(true);
            recyclerView.setLayoutManager(manager);
            recyclerView.setRecycledViewPool(recyclerPool);
        }
    }
}