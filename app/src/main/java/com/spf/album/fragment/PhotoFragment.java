package com.spf.album.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.spf.album.ImageFile;
import com.spf.album.activity.ImagePreviewActivity;
import com.spf.album.adapter.PhotoListAdapter;
import com.spf.album.R;
import com.spf.album.databinding.FragmentPhotoBinding;
import com.spf.album.event.PhotoImageClickEvent;
import com.spf.album.utils.DateUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PhotoFragment extends BaseFragment {
    private FragmentPhotoBinding binding;
    private PhotoListAdapter photoListAdapter;
    private List<ImageFile> imageFileList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo, null, false);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        binding.recyclerView.setAdapter(photoListAdapter = new PhotoListAdapter(mActivity));
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    public void setImageFileList(List<ImageFile> imageFileList) {
        this.imageFileList = imageFileList;
        SortedMap<String, List<ImageFile>> imageFileMap = new TreeMap<>((o1, o2) -> o2.compareTo(o1));
        for (ImageFile imageFile : imageFileList) {
            String date = DateUtils.getDateStr(imageFile.getAddDate() * 1000);
            List<ImageFile> dateImageList = imageFileMap.get(date);
            if (dateImageList == null) {
                dateImageList = new ArrayList<>();
                imageFileMap.put(date, dateImageList);
            }
            dateImageList.add(imageFile);
        }
        photoListAdapter.setImageFiles(imageFileMap);
    }

    public void scrollToImage(ImageFile imageFile) {
        int position = photoListAdapter.getPosition(imageFile);
        binding.recyclerView.scrollToPosition(position);
        ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventImageClick(PhotoImageClickEvent event) {
        int index = 0;
        for (int i = 0; i < imageFileList.size(); i++) {
            if (imageFileList.get(i) == event.getImageFile()) {
                index = i;
                break;
            }
        }
        ImagePreviewActivity.start(getContext(), index, imageFileList);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}