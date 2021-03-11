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

import com.amap.api.maps.AMap;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.CameraPosition;
import com.spf.album.IScreenLocation;
import com.spf.album.ImageFileLoader;
import com.spf.album.R;
import com.spf.album.event.ImageFileLoadedEvent;
import com.spf.album.model.GaoDeImageFile;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.AppExecutors;
import com.spf.album.utils.ImageLoadUtils;
import com.spf.album.utils.LogUtils;
import com.spf.album.view.MarkLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;

public class GaoDeLocationFragment extends BaseFragment implements IScreenLocation {
    private MapView mapView;
    private MarkLayout markLayout;
    private AMap mMap;
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
        EventBus.getDefault().register(this);
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
        return mMap.getProjection().toScreenLocation(((GaoDeImageFile) imageFile).getLatLng());
    }

    @Override
    public boolean isInVisibleRegion(ImageFile imageFile) {
        return mMap.getProjection().getVisibleRegion().latLngBounds.contains(((GaoDeImageFile) imageFile).getLatLng());
    }

    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onEventImageFileLoaded(ImageFileLoadedEvent event) {
        isFileListInit = true;
    }

    private void setMarks() {
        if (!isFileListInit) {
            AppExecutors.getInstance().runOnUI(GaoDeLocationFragment.this::setMarks, 1000);
            return;
        }
        mMarkMap.clear();
        isImageBitmapInit = false;
        AppExecutors.getInstance().runOnBackground(() -> {
            for (ImageFile item : ImageFileLoader.getInstance().getCameraList()) {
                GaoDeImageFile imageFile = (GaoDeImageFile) item;
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
        EventBus.getDefault().unregister(this);
    }
}