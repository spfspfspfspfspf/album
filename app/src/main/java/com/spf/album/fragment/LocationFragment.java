package com.spf.album.fragment;

import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.exifinterface.media.ExifInterface;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.spf.album.ImageFile;
import com.spf.album.R;
import com.spf.album.databinding.FragmentLocationBinding;
import com.spf.album.utils.AppExecutors;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;
import com.spf.album.view.ImageDrawView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationFragment extends BaseFragment {
    private FragmentLocationBinding mBinding;
    private AMap mMap;
    private List<ImageFile> mImageFileList = new ArrayList<>();
    private Map<ImageFile, ImageDrawView.AreaImageInfo> mImagePointMap = new HashMap<>();

    private volatile boolean isImageFileInit = false;
    private volatile boolean isImageBitmapInit = false;
    private int corner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_location, null, false);
        mBinding.mapView.onCreate(savedInstanceState);
        mMap = mBinding.mapView.getMap();
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(TAG, "onCameraChange");
                updatePointImageMap();
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                LogUtils.d(TAG, "onCameraChangeFinish");
                setPointImageMap();
            }
        });
        mMap.setOnMapLoadedListener(this::setPointImageMap);
        corner = getResources().getDimensionPixelOffset(R.dimen.dp_5);
        return mBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mBinding.mapView.onResume();
    }

    public void setImageFileList(List<ImageFile> imageFileList) {
        isImageFileInit = false;
        if (imageFileList == null || imageFileList.isEmpty()) {
            mImageFileList.clear();
            mImagePointMap.clear();
            isImageFileInit = true;
        } else {
            AppExecutors.getInstance().runOnBackground(() -> {
                for (ImageFile imageFile : imageFileList) {
                    if (imageFile.getLatitude() == 0 && imageFile.getLongitude() == 0) {
                        try {
                            double[] latLng = new ExifInterface(imageFile.getPath()).getLatLong();
                            if (latLng != null) {
                                imageFile.setLatLng(latLng[0], latLng[1]);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (getActivity() == null || !LocationFragment.this.isAdded()) {
                        return;
                    }
                    if (imageFile.getLatitude() != 0 && imageFile.getLongitude() != 0) {
                        CoordinateConverter converter = new CoordinateConverter(getActivity());
                        converter.from(CoordinateConverter.CoordType.GPS);
                        converter.coord(new LatLng(imageFile.getLatitude(), imageFile.getLongitude()));
                        imageFile.setLatLng(converter.convert());
                    }
                    mImageFileList = imageFileList;
                }
                isImageFileInit = true;
            });
        }
    }

    private void updatePointImageMap() {
        if (!isImageFileInit || !isImageBitmapInit) {
            return;
        }
        for (Map.Entry<ImageFile, ImageDrawView.AreaImageInfo> entry : mImagePointMap.entrySet()) {
            entry.getValue().setArea(mMap.getProjection().toScreenLocation(entry.getKey().getLatLng()));
        }
        mBinding.ivDrawView.setPoints(mImagePointMap);
    }

    private void setPointImageMap() {
        if (!isImageFileInit) {
            AppExecutors.getInstance().runOnUI(LocationFragment.this::setPointImageMap, 1000);
            return;
        }
        mImagePointMap.clear();
        isImageBitmapInit = false;
        AppExecutors.getInstance().runOnBackground(() -> {
            for (ImageFile imageFile : mImageFileList) {
                if (imageFile.getLatLng() == null) {
                    continue;
                }
                if (mMap.getProjection().getVisibleRegion().latLngBounds.contains(imageFile.getLatLng())) {
                    Point point = mMap.getProjection().toScreenLocation(imageFile.getLatLng());
                    ImageFile findKey = null;
                    for (Map.Entry<ImageFile, ImageDrawView.AreaImageInfo> fileAreaEntry : mImagePointMap.entrySet()) {
                        if (fileAreaEntry.getValue().intersects(point)) {
                            findKey = fileAreaEntry.getKey();
                            break;
                        }
                    }
                    ImageDrawView.AreaImageInfo areaImageInfo;
                    if (findKey == null) {
                        areaImageInfo = new ImageDrawView.AreaImageInfo(point, imageFile.getUri());
                        mImagePointMap.put(imageFile, areaImageInfo);
                    } else {
                        areaImageInfo = mImagePointMap.get(findKey);
                        areaImageInfo.addCount();
                    }
                    try {
                        if (areaImageInfo.getBitmap() == null || areaImageInfo.getBitmap().isRecycled()) {
                            areaImageInfo.setBitmap(ImageLoadUtils.getBitmap(new ImageLoadUtils.ImageBuilder(
                                    getContext(), imageFile.getUri())
                                    .setScaleType(ImageView.ScaleType.CENTER_CROP)
                                    .setRoundCorner(corner)
                                    .setSize(2 * ImageDrawView.radius, 2 * ImageDrawView.radius)));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (findKey == null) {
                            mImagePointMap.remove(imageFile);
                        } else {
                            mImagePointMap.remove(findKey);
                        }
                    }
                }
            }
            AppExecutors.getInstance().runOnUI(() -> {
                mBinding.ivDrawView.setPoints(mImagePointMap);
                isImageBitmapInit = true;
            });
        });
    }


    @Override
    public void onPause() {
        super.onPause();
        mBinding.mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mBinding.mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBinding.mapView.onDestroy();
    }
}