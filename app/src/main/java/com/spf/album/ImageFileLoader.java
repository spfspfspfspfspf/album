package com.spf.album;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.exifinterface.media.ExifInterface;

import com.spf.album.event.ImageFileLoadedEvent;
import com.spf.album.model.FolderInfo;
import com.spf.album.model.GaoDeImageFile;
import com.spf.album.model.ImageFile;
import com.spf.album.utils.VideoUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ImageFileLoader {
    private static final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE};

    private static volatile ImageFileLoader instance;
    private volatile boolean isCanceled = false;

    private final List<ImageFile> mAllList;
    private final List<FolderInfo> mFolderList;

    public static ImageFileLoader getInstance() {
        if (instance == null) {
            synchronized (ImageFileLoader.class) {
                if (instance == null) {
                    instance = new ImageFileLoader();
                }
            }
        }
        return instance;
    }

    private ImageFileLoader() {
        mAllList = new ArrayList<>();
        mFolderList = new ArrayList<>();
    }

    public void initOrUpdate() {
        isCanceled = false;
        Cursor cursor = query();
        initData(cursor);
        if (isCanceled) {
            return;
        }
        EventBus.getDefault().post(new ImageFileLoadedEvent());
    }

    private Cursor query() {
        final String SELECTION = "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                + " OR "
                + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                + " AND " + MediaStore.MediaColumns.SIZE + ">0";
        final String[] SELECTION_ARGS = {
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO)};
        Uri uri = MediaStore.Files.getContentUri("external");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            uri = MediaStore.setRequireOriginal(uri);
        }
        return GalleryApplication.getApplication().getContentResolver().query(uri, IMAGE_PROJECTION,
                SELECTION, SELECTION_ARGS, IMAGE_PROJECTION[3] + " DESC");
    }

    private void initData(Cursor cursor) {
        mAllList.clear();
        mFolderList.clear();
        if (cursor == null) {
            return;
        }
        try {
            if (cursor.getCount() < 1) {
                return;
            }
            cursor.moveToFirst();
            do {
                ImageFile imageFile = createImageFile(cursor);
                if (imageFile == null) {
                    continue;
                }
                mAllList.add(imageFile);
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }
    }

    private ImageFile createImageFile(Cursor cursor) {
        String path = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
        if (!TextUtils.isEmpty(path)) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
            long lastDate = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
            String mediaType = cursor.getString(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));

            ImageFile imageFile = new GaoDeImageFile(id, path, name, lastDate * 1000, mediaType, size);
            if (imageFile.isVideo()) {
                imageFile.setDuration(VideoUtils.getDuration(path));
            }
//            if (imageFile.isImage()) {
//                imageFile.setUri(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id));
//            } else if (imageFile.isVideo()) {
//                imageFile.setUri(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id));
//                imageFile.setDuration(VideoUtils.getDuration(path));
//            } else {
//                imageFile.setUri(ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), id));
//            }

            try {
                double[] latLng = new ExifInterface(path).getLatLong();
                if (latLng != null) {
                    imageFile.setLatLng(latLng[0], latLng[1]);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return imageFile;
        }
        return null;
    }

    public List<ImageFile> getAllList() {
        return mAllList;
    }

    public List<FolderInfo> getFolderList() {
        if (mFolderList.isEmpty() && !mAllList.isEmpty()) {
            Map<String, FolderInfo> folderMap = new LinkedHashMap<>();
            for (ImageFile imageFile : mAllList) {
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
            mFolderList.addAll(folderMap.values());
        }
        return mFolderList;
    }

    public void clear() {
        isCanceled = true;
    }
}
