package com.spf.album.event;

import com.spf.album.ImageFile;

public class LatLntImageClickEvent {
    private ImageFile imageFile;

    public LatLntImageClickEvent(ImageFile imageFile) {
        this.imageFile = imageFile;
    }

    public ImageFile getImageFile() {
        return imageFile;
    }
}