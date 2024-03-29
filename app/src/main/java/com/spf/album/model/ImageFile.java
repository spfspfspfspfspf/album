package com.spf.album.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.spf.album.utils.DateUtils;

public class ImageFile implements Parcelable {
    public static final long ID_TITLE = -101;

    protected long id;
    protected String path;
    protected String name;
    protected String date;
    protected String mediaType;
    protected long size;
    protected double latitude;
    protected double longitude;
    protected String duration;

    public ImageFile() {
    }

    public ImageFile(long id, String path, String name, long date, String mediaType, long size) {
        this.id = id;
        this.path = path;
        this.name = name;
        this.date = DateUtils.getDateStr(date);
        this.mediaType = mediaType;
        this.size = size;
    }

    protected ImageFile(Parcel in) {
        id = in.readLong();
        path = in.readString();
        name = in.readString();
        date = in.readString();
        mediaType = in.readString();
        size = in.readLong();
        latitude = in.readDouble();
        longitude = in.readDouble();
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
        imageFile.date = title;
        return imageFile;
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

    public String getDate() {
        return date;
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

    public void setLatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isTitle() {
        return id == ID_TITLE;
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

    public void setDuration(String duration) {
        this.duration = duration;
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
        dest.writeString(date);
        dest.writeString(mediaType);
        dest.writeLong(size);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(duration);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ImageFile) {
            return path.equals(((ImageFile) obj).path);
        }
        return false;
    }
}