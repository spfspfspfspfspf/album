package com.spf.album.fragment;

import android.content.Context;
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
import com.spf.album.activity.ImageGridActivity;
import com.spf.album.model.FolderInfo;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlbumFragment extends BaseFragment {
    private TextImageAdapter adapter;
    private List<ImageFile> imageFileList;
    private boolean isFolderMapInit = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_album, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        recyclerView.setAdapter(adapter = new TextImageAdapter(mActivity));
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isFolderMapInit) {
            initFolderMap();
            isFolderMapInit = true;
        }
    }

    private void initFolderMap() {
        Map<String, FolderInfo> folderMap = new LinkedHashMap<>();
        for (ImageFile imageFile : imageFileList) {
            File parentFile = new File(imageFile.getPath()).getParentFile();
            String parentPath;
            String parentName;
            if (parentFile == null) {
                parentPath = "/";
                parentName = "sdcard";
            } else {
                parentPath = parentFile.getAbsolutePath();
                parentName = parentFile.getName();
            }
            FolderInfo folderInfo = folderMap.get(parentPath);
            if (folderInfo == null) {
                folderInfo = new FolderInfo(parentPath, parentName);
                folderMap.put(parentPath, folderInfo);
            }
            folderInfo.addImageFile(imageFile);
        }
        adapter.setFolderList(new ArrayList<>(folderMap.values()));
    }

    public void setImageFileList(List<ImageFile> imageFileList) {
        this.imageFileList = imageFileList;
    }

    static class TextImageAdapter extends RecyclerView.Adapter<TextImageHolder> {
        private Context mContext;
        private List<FolderInfo> mFolderList;
        private int roundCorner;
        private int imageSize;

        TextImageAdapter(Context context) {
            mContext = context;
            roundCorner = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_5);
            imageSize = (ScreenUtils.getScreenWidth(mContext) - mContext.getResources().getDimensionPixelOffset(R.dimen.dp_40)) / 3;
        }

        void setFolderList(List<FolderInfo> folderList) {
            if (folderList == null) {
                mFolderList = new ArrayList<>();
            } else {
                mFolderList = folderList;
            }
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public TextImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new TextImageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_album_thumbnail, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull TextImageHolder holder, int position) {
            FolderInfo folderInfo = mFolderList.get(position);
            ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(mContext, folderInfo.getThumbnail(), holder.ivImage)
                    .setScaleType(ImageView.ScaleType.CENTER_CROP).setRoundCorner(roundCorner)
                    .setSize(imageSize, imageSize));
            holder.tvName.setText(folderInfo.getName());
            holder.tvCount.setText(String.valueOf(folderInfo.getImageFiles().size()));
            holder.itemView.setOnClickListener(v -> ImageGridActivity.start(mContext, folderInfo.getName(), folderInfo.getImageFiles()));
        }

        @Override
        public int getItemCount() {
            return mFolderList == null ? 0 : mFolderList.size();
        }
    }

    static class TextImageHolder extends RecyclerView.ViewHolder {
        private ImageView ivImage;
        private TextView tvName;
        private TextView tvCount;

        TextImageHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.iv_image);
            tvName = itemView.findViewById(R.id.tv_name);
            tvCount = itemView.findViewById(R.id.tv_count);
        }
    }
}