package com.spf.album.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.model.ImageFile;
import com.spf.album.R;
import com.spf.album.view.PhotoItemView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PhotoListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int TYPE_TITLE = 1;
    private final int TYPE_IMAGE_TWO = 2;
    private final int TYPE_IMAGE_FOUR = 3;
    private Context context;
    private List<List<ImageFile>> imageDataList;

    public PhotoListAdapter(Context context) {
        this.context = context;
        imageDataList = new ArrayList<>();
    }

    public void setImageFiles(Map<String, List<ImageFile>> imageMaps) {
        imageDataList.clear();
        for (Map.Entry<String, List<ImageFile>> entry : imageMaps.entrySet()) {
            List<ImageFile> imageTitle = new ArrayList<>(1);
            imageTitle.add(ImageFile.createImageTitle(entry.getKey()));
            imageDataList.add(imageTitle);

            List<ImageFile> itemValue = entry.getValue();
            int divide4 = itemValue.size() / 4;
            if (divide4 > 0) {
                do {
                    List<ImageFile> itemList = new ArrayList<>();
                    itemList.add(itemValue.remove(0));
                    itemList.add(itemValue.remove(0));
                    itemList.add(itemValue.remove(0));
                    itemList.add(itemValue.remove(0));
                    imageDataList.add(itemList);
                    divide4--;
                } while (divide4 > 0);
            }
            if (itemValue.size() > 0) {
                imageDataList.add(itemValue);
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

    @Override
    public int getItemViewType(int position) {
        List<ImageFile> imageFiles = imageDataList.get(position);
        int size = imageFiles.size();
        if (size > 0) {
            if (size == 1 && imageFiles.get(0).getId() == ImageFile.ID_TITLE) {
                return TYPE_TITLE;
            } else if (size > 2) {
                return TYPE_IMAGE_FOUR;
            } else {
                return TYPE_IMAGE_TWO;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_TITLE) {
            return new TitleViewHolder(LayoutInflater.from(context).inflate(R.layout.item_image_title, parent, false));
        } else if (viewType == TYPE_IMAGE_TWO) {
            return new ItemViewHolderTwo(LayoutInflater.from(context).inflate(R.layout.item_image_list_two, parent, false));
        } else if (viewType == TYPE_IMAGE_FOUR) {
            return new ItemViewHolderFour(LayoutInflater.from(context).inflate(R.layout.item_image_list_four, parent, false));
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        List<ImageFile> imageFiles = imageDataList.get(position);
        if (holder instanceof TitleViewHolder) {
            ((TitleViewHolder) holder).tvTitle.setText(imageFiles.get(0).getName());
        } else if (holder instanceof ItemViewHolderFour) {
            ItemViewHolderFour itemFour = (ItemViewHolderFour) holder;
            itemFour.itemOne.setImageFile(imageFiles.get(0));
            itemFour.itemTwo.setImageFile(imageFiles.get(1));
            itemFour.itemThree.setImageFile(imageFiles.get(2));
            if (imageFiles.size() > 3) {
                itemFour.itemFour.setVisibility(View.VISIBLE);
                itemFour.itemFour.setImageFile(imageFiles.get(3));
            } else {
                itemFour.itemFour.setVisibility(View.GONE);
            }
        } else if (holder instanceof ItemViewHolderTwo) {
            ItemViewHolderTwo itemTwo = (ItemViewHolderTwo) holder;
            itemTwo.itemOne.setImageFile(imageFiles.get(0));
            if (imageFiles.size() > 1) {
                itemTwo.itemTwo.setVisibility(View.VISIBLE);
                itemTwo.itemTwo.setImageFile(imageFiles.get(1));
            } else {
                itemTwo.itemTwo.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return imageDataList == null ? 0 : imageDataList.size();
    }

    static class TitleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;

        TitleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_title);
        }
    }

    static class ItemViewHolderTwo extends RecyclerView.ViewHolder {
        PhotoItemView itemOne;
        PhotoItemView itemTwo;

        ItemViewHolderTwo(@NonNull View itemView) {
            super(itemView);
            itemOne = itemView.findViewById(R.id.photo_item_one);
            itemTwo = itemView.findViewById(R.id.photo_item_two);
        }
    }

    static class ItemViewHolderFour extends ItemViewHolderTwo {
        PhotoItemView itemThree;
        PhotoItemView itemFour;

        ItemViewHolderFour(@NonNull View itemView) {
            super(itemView);
            itemThree = itemView.findViewById(R.id.photo_item_three);
            itemFour = itemView.findViewById(R.id.photo_item_four);
        }
    }
}
