package com.spf.album;

import android.graphics.Point;

import com.spf.album.model.ImageFile;

public interface IScreenLocation {
    Point toScreenLocation(ImageFile imageFile);
    boolean isInVisibleRegion(ImageFile imageFile);
}