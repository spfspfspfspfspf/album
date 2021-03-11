package com.spf.album.model;

import android.os.Parcel;

import com.amap.api.maps.CoordinateConverter;
import com.amap.api.maps.model.LatLng;
import com.spf.album.GalleryApplication;

public class GaoDeImageFile extends ImageFile {
    private LatLng latLng;

    public GaoDeImageFile(long id, String path, String name, long addDate, String mediaType, long size) {
        super(id, path, name, addDate, mediaType, size);
    }

    public GaoDeImageFile(Parcel in) {
        super(in);
        latLng = in.readParcelable(LatLng.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(latLng, flags);
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(double latitude, double longitude) {
        super.setLatLng(latitude, longitude);
        CoordinateConverter converter = new CoordinateConverter(GalleryApplication.getApplication());
        converter.from(CoordinateConverter.CoordType.GPS);
        converter.coord(new LatLng(latitude, longitude));
        this.latLng = converter.convert();
    }
}