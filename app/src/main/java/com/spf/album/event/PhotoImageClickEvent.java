package com.spf.album.event;

import com.spf.album.ImageFile;

public class PhotoImageClickEvent {
    private ImageFile imageFile;

    public PhotoImageClickEvent(ImageFile imageFile) {
        this.imageFile = imageFile;
    }

    public ImageFile getImageFile() {
        return imageFile;
    }
}