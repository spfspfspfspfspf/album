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
import androidx.exifinterface.media.ExifInterface;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.spf.album.IScreenLocation;
import com.spf.album.R;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.AppExecutors;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;
import com.spf.album.view.MarkLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocationFragment extends BaseFragment implements IScreenLocation {
    private MapView mapView;
    private MarkLayout markLayout;
    private AMap mMap;
    private List<ImageFile> mImageFileList = new ArrayList<>();
    private Map<ImageFile, MarkLayout.MarkInfo> mMarkMap = new HashMap<>();

    private volatile boolean isFileListInit = false;
    private volatile boolean isImageBitmapInit = false;
    private int corner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = view.findViewById(R.id.map_view);
        markLayout = view.findViewById(R.id.mark_layout);
        initMap(savedInstanceState);
        markLayout.setScreenLocation(this);
        corner = getResources().getDimensionPixelOffset(R.dimen.dp_5);
    }

    private void initMap(@Nullable Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mMap = mapView.getMap();
        mMap.getUiSettings().setZoomControlsEnabled(false);
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        mMap.setOnCameraChangeListener(new AMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                Log.d(TAG, "onCameraChange");
                if (isFileListInit && isImageBitmapInit) {
                    markLayout.updateMarks();
                }
            }

            @Override
            public void onCameraChangeFinish(CameraPosition cameraPosition) {
                LogUtils.d(TAG, "onCameraChangeFinish");
                setMarks();
            }
        });
        mMap.setOnMapLoadedListener(this::setMarks);
    }

    @Override
    public Point toScreenLocation(ImageFile imageFile) {
        return mMap.getProjection().toScreenLocation(imageFile.getLatLng());
    }

    @Override
    public boolean isInVisibleRegion(ImageFile imageFile) {
        return mMap.getProjection().getVisibleRegion().latLngBounds.contains(imageFile.getLatLng());
    }

    //called in the background
    public void setImageFileList(List<ImageFile> imageFileList) {
        if (mActivity == null || mActivity.isFinishing()) {
            return;
        }
        isFileListInit = false;
        if (imageFileList == null || imageFileList.isEmpty()) {
            mImageFileList.clear();
        } else {
            CoordinateConverter converter = new CoordinateConverter(mActivity);
            converter.from(CoordinateConverter.CoordType.GPS);
            for (ImageFile imageFile : imageFileList) {
                if (mActivity == null || isDetached()) {
                    return;
                }

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

                if (imageFile.getLatitude() != 0 && imageFile.getLongitude() != 0) {
                    converter.coord(new LatLng(imageFile.getLatitude(), imageFile.getLongitude()));
                    imageFile.setLatLng(converter.convert());
                }
            }
            mImageFileList = imageFileList;
        }
        isFileListInit = true;
    }

    private void setMarks() {
        if (!isFileListInit) {
            AppExecutors.getInstance().runOnUI(LocationFragment.this::setMarks, 1000);
            return;
        }
        mMarkMap.clear();
        isImageBitmapInit = false;
        AppExecutors.getInstance().runOnBackground(() -> {
            for (ImageFile imageFile : mImageFileList) {
                if (imageFile.getLatLng() == null) {
                    continue;
                }
                if (isInVisibleRegion(imageFile)) {
                    Point point = toScreenLocation(imageFile);
                    MarkLayout.MarkInfo markInfo = null;
                    for (Map.Entry<ImageFile, MarkLayout.MarkInfo> fileAreaEntry : mMarkMap.entrySet()) {
                        if (fileAreaEntry.getValue().intersects(point)) {
                            markInfo = fileAreaEntry.getValue();
                            break;
                        }
                    }

                    if (markInfo == null) {
                        markInfo = new MarkLayout.MarkInfo(point, imageFile.getUri());
                        mMarkMap.put(imageFile, markInfo);
                    } else {
                        markInfo.addCount();
                    }
                    if (markInfo.getBitmap() == null || markInfo.getBitmap().isRecycled()) {
                        markInfo.setBitmap(ImageLoadUtils.getBitmap(new ImageLoadUtils.ImageBuilder(
                                getContext(), imageFile.getUri())
                                .setScaleType(ImageView.ScaleType.CENTER_CROP)
                                .setRoundCorner(corner)
                                .setSize(2 * MarkLayout.radius, 2 * MarkLayout.radius)));
                    }
                }
            }
            AppExecutors.getInstance().runOnUI(() -> {
                markLayout.setMarks(mMarkMap);
                isImageBitmapInit = true;
            });
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapView.onDestroy();
    }
}