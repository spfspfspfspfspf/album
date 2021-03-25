package com.spf.album.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.spf.album.ImageFileLoader;
import com.spf.album.R;
import com.spf.album.adapter.PhotoGridAdapter;
import com.spf.album.databinding.FragmentPhotoBinding;
import com.spf.album.event.ImageFileLoadedEvent;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.AppExecutors;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class PhotoFragment extends BaseFragment {
    private FragmentPhotoBinding binding;
    private PhotoGridAdapter photoGridAdapter;
    private GridLayoutManager layoutManager;
    private String nowTopDate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_photo, null, false);
        photoGridAdapter = new PhotoGridAdapter(mActivity);
        layoutManager = new GridLayoutManager(mActivity, PhotoGridAdapter.COLUMN_COUNT);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return photoGridAdapter.getSpanSize(position);
            }
        });
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(photoGridAdapter);
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                int position = layoutManager.findFirstVisibleItemPosition();
                ImageFile imageFile = photoGridAdapter.getImageFile(position);
                if (imageFile == null) {
                    binding.tvTitle.setVisibility(View.INVISIBLE);
                } else {
                    binding.tvTitle.setVisibility(View.VISIBLE);
                    if (nowTopDate.equals(imageFile.getDate())) {
                        return;
                    }
                    nowTopDate = imageFile.getDate();
                    binding.tvTitle.setText(nowTopDate);
                }
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        EventBus.getDefault().register(this);
    }

    public void scrollToImage(ImageFile imageFile) {
        int position = photoGridAdapter.getPosition(imageFile);
        binding.recyclerView.scrollToPosition(position);
        ((LinearLayoutManager) binding.recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventImageFileLoaded(ImageFileLoadedEvent event) {
        if (ImageFileLoader.getInstance().getCameraList().isEmpty()) {
            return;
        }
        SortedMap<String, List<ImageFile>> imageFileMap = new TreeMap<>((o1, o2) -> o2.compareTo(o1));
        for (ImageFile imageFile : ImageFileLoader.getInstance().getCameraList()) {
            //String date = DateUtils.getDateStr(imageFile.getAddDate() * 1000);
            List<ImageFile> dateImageList = imageFileMap.get(imageFile.getDate());
            if (dateImageList == null) {
                dateImageList = new ArrayList<>();
                imageFileMap.put(imageFile.getDate(), dateImageList);
            }
            dateImageList.add(imageFile);
        }
        AppExecutors.getInstance().runOnUI(() -> {
            nowTopDate = ImageFileLoader.getInstance().getCameraList().get(0).getDate();
            binding.tvTitle.setText(nowTopDate);
            photoGridAdapter.setImageFiles(imageFileMap);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}