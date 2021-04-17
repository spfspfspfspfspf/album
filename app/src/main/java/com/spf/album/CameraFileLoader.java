package com.spf.album;

import android.os.Environment;

import androidx.exifinterface.media.ExifInterface;

import com.spf.album.event.CameraFileLoadedEvent;
import com.spf.album.model.GaoDeImageFile;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.AppExecutors;
import com.spf.album.utils.VideoUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CameraFileLoader {
    private static final String MIME_IMAGE = "image/jpeg";
    private static final String MIME_VIDEO = "video/mp4";
    private static volatile CameraFileLoader instance;
    private List<ImageFile> imageList;
    private Map<String, List<ImageFile>> imageMap;

    private CameraFileLoader() {
    }

    public static CameraFileLoader getInstance() {
        if (instance == null) {
            synchronized (CameraFileLoader.class) {
                if (instance == null) {
                    instance = new CameraFileLoader();
                }
            }
        }
        return instance;
    }

    public List<ImageFile> getImageList() {
        if (imageList == null) {
            imageList = new ArrayList<>();
        }
        return imageList;
    }

    public Map<String, List<ImageFile>> getImageMap() {
        if (imageMap == null) {
            imageMap = new TreeMap<>((o1, o2) -> o2.compareTo(o1));
        }
        return imageMap;
    }

    public void init() {
        AppExecutors.getInstance().runOnBackground(new Runnable() {
            @Override
            public void run() {
                File cameraDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
                toImageFileList(cameraDir.listFiles());
                toImageFileMap();
                EventBus.getDefault().postSticky(new CameraFileLoadedEvent());
                ImageFileLoader.getInstance().initOrUpdate();
            }
        });
    }

    private void toImageFileList(File[] files) {
        int size = files == null ? 0 : files.length;
        if (imageList == null) {
            imageList = new ArrayList<>(size);
        } else {
            imageList.clear();
        }
        for (File file : files) {
            ImageFile imageFile = new GaoDeImageFile(0, file.getAbsolutePath(), file.getName(),
                    file.lastModified(), mediaType(file), file.getTotalSpace());
            if (imageFile.isVideo()) {
                imageFile.setDuration(VideoUtils.getDuration(imageFile.getPath()));
            }
            try {
                double[] latLng = new ExifInterface(file.getAbsolutePath()).getLatLong();
                if (latLng != null) {
                    imageFile.setLatLng(latLng[0], latLng[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            imageList.add(imageFile);
        }
    }

    private void toImageFileMap() {
        if (imageMap == null) {
            imageMap = new TreeMap<>((o1, o2) -> o2.compareTo(o1));
        } else {
            imageMap.clear();
        }
        for (ImageFile imageFile : imageList) {
            List<ImageFile> dateImageList = imageMap.get(imageFile.getDate());
            if (dateImageList == null) {
                dateImageList = new ArrayList<>();
                imageMap.put(imageFile.getDate(), dateImageList);
            }
            dateImageList.add(imageFile);
        }
    }

    private String mediaType(File file) {
        if (file.getName().endsWith("jpg")) {
            return MIME_IMAGE;
        } else if (file.getName().endsWith("mp4")) {
            return MIME_VIDEO;
        }
        return "";
    }

}