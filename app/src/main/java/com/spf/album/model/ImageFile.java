package com.spf.album.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.amap.api.maps.model.LatLng;

import java.io.File;

public class ImageFile implements Parcelable {
    public static final long ID_TITLE = -101;
    public static final String[] IMAGE_PROJECTION = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.MIME_TYPE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.LATITUDE,
            MediaStore.Images.Media.LONGITUDE};

    private long id;
    private String path;
    private String name;
    private long addDate;
    private long modifyDate;
    private String mediaType;
    private long size;
    private double latitude;
    private double longitude;
    private Uri uri;
    private LatLng latLng;
    private String duration;

    private ImageFile() {
    }

    protected ImageFile(Parcel in) {
        id = in.readLong();
        path = in.readString();
        name = in.readString();
        addDate = in.readLong();
        modifyDate = in.readLong();
        mediaType = in.readString();
        size = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
        uri = in.readParcelable(Uri.class.getClassLoader());
        latLng = in.readParcelable(LatLng.class.getClassLoader());
        duration = in.readString();
    }

    public static final Creator<ImageFile> CREATOR = new Creator<ImageFile>() {
        @Override
        public ImageFile createFromParcel(Parcel in) {
            return new ImageFile(in);
        }

        @Override
        public ImageFile[] newArray(int size) {
            return new ImageFile[size];
        }
    };

    public static ImageFile createImageTitle(String title) {
        ImageFile imageFile = new ImageFile();
        imageFile.id = ID_TITLE;
        imageFile.name = title;
        return imageFile;
    }

    public static ImageFile createImageFile(Context context, Cursor data) {
        String path = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[1]));
        if (!TextUtils.isEmpty(path) && new File(path).exists()) {
            ImageFile imageFile = new ImageFile();
            imageFile.id = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[0]));
            imageFile.path = path;
            imageFile.name = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[2]));
            imageFile.addDate = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[3]));
            imageFile.modifyDate = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[4]));
            imageFile.mediaType = data.getString(data.getColumnIndexOrThrow(IMAGE_PROJECTION[5]));
            imageFile.size = data.getLong(data.getColumnIndexOrThrow(IMAGE_PROJECTION[6]));
            imageFile.latitude = data.getDouble(data.getColumnIndexOrThrow(IMAGE_PROJECTION[7]));
            imageFile.longitude = data.getDouble(data.getColumnIndexOrThrow(IMAGE_PROJECTION[8]));

            if (imageFile.isImage()) {
                imageFile.uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageFile.id);
            } else if (imageFile.isVideo()) {
                imageFile.uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, imageFile.id);
                imageFile.duration = getLocalVideoDuration(imageFile.path);
            } else {
                imageFile.uri = ContentUris.withAppendedId(MediaStore.Files.getContentUri("external"), imageFile.id);
            }

            return imageFile;
        }
        return null;
    }

    private static String getLocalVideoDuration(String videoPath) {
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(videoPath);
            long seconds = Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)) / 1000;
            int m = (int) (seconds / 60);
            int s = (int) (seconds % 60);
            StringBuilder strBuilder = new StringBuilder();
            if (m < 10) {
                strBuilder.append("0");
            }
            strBuilder.append(m).append(":");
            if (s < 10) {
                strBuilder.append("0");
            }
            strBuilder.append(s);
            return strBuilder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public long getId() {
        return id;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public long getAddDate() {
        return addDate;
    }

    public long getModifyDate() {
        return modifyDate;
    }

    public long getSize() {
        return size;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public Uri getUri() {
        return uri;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public boolean isImage() {
        return mediaType != null && mediaType.startsWith("image");
    }

    public boolean isVideo() {
        return mediaType != null && mediaType.startsWith("video");
    }

    public String getDuration() {
        return duration;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(path);
        dest.writeString(name);
        dest.writeLong(addDate);
        dest.writeLong(modifyDate);
        dest.writeString(mediaType);
        dest.writeLong(size);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeParcelable(uri, flags);
        dest.writeParcelable(latLng, flags);
        dest.writeString(duration);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ImageFile) {
            return uri.equals(((ImageFile) obj).uri);
        }
        return false;
    }
}