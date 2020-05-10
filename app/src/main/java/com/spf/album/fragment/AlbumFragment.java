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
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.FolderInfo;
import com.spf.album.ImageFile;
import com.spf.album.R;
import com.spf.album.activity.ImageGridActivity;
import com.spf.album.databinding.FragmentAlbumBinding;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.ScreenUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlbumFragment extends BaseFragment {
    private FragmentAlbumBinding binding;
    private TextImageAdapter adapter;
    private List<ImageFile> imageFileList;
    private Map<String, FolderInfo> mFolderMap = new LinkedHashMap<>();
    private volatile boolean isFolderMapInit = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_album, null, false);
        binding.recyclerView.setLayoutManager(new GridLayoutManager(mActivity, 3));
        binding.recyclerView.setAdapter(adapter = new TextImageAdapter(mActivity));
        return binding.getRoot();
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
        mFolderMap.clear();
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
            FolderInfo folderInfo = mFolderMap.get(parentPath);
            if (folderInfo == null) {
                folderInfo = new FolderInfo(parentPath, parentName);
                mFolderMap.put(parentPath, folderInfo);
            }
            folderInfo.addImageFile(imageFile);
        }
        adapter.setFolderList(new ArrayList<>(mFolderMap.values()));
    }

    public void setImageFileList(List<ImageFile> imageFileList) {
        this.imageFileList = imageFileList;
    }

    static class TextImageAdapter extends RecyclerView.Adapter<TextImageHolder> {
        private Context mContext;
        private int roundCorner;
        private List<FolderInfo> mFolderList;

        TextImageAdapter(Context context) {
            mContext = context;
            roundCorner = mContext.getResources().getDimensionPixelOffset(R.dimen.dp_5);
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
            TextImageHolder holder = new TextImageHolder(LayoutInflater.from(mContext).inflate(R.layout.item_album_thumbnail, parent, false));
            ViewGroup.LayoutParams params = holder.ivImage.getLayoutParams();
            params.width = (ScreenUtils.getScreenWidth(mContext) - mContext.getResources().getDimensionPixelOffset(R.dimen.dp_10)) / 3;
            params.height = params.width;
            holder.ivImage.setLayoutParams(params);
            return holder;
        }

        @Override
        public void onBindViewHolder(@NonNull TextImageHolder holder, int position) {
            FolderInfo folderInfo = mFolderList.get(position);
            ImageLoadUtils.loadImage(new ImageLoadUtils.ImageBuilder(mContext, folderInfo.getThumbnail(), holder.ivImage)
                    .setScaleType(ImageView.ScaleType.CENTER_CROP).setRoundCorner(roundCorner));
            holder.tvName.setText(folderInfo.getName());
            holder.tvCount.setText(folderInfo.getImageFiles().size() + "");
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