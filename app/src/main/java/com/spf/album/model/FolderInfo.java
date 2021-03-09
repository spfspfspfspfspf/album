package com.spf.album.model;

import android.net.Uri;

import java.util.ArrayList;

public class FolderInfo {
    private String path;
    private String name;
    private Uri thumbnail;
    private ArrayList<ImageFile> imageFiles;

    public FolderInfo(String path, String name) {
        this.path = path;
        this.name = name;
        this.imageFiles = new ArrayList<>();
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public void addImageFile(ImageFile imageFile) {
        imageFiles.add(imageFile);
        if (thumbnail == null) {
            thumbnail = imageFile.getUri();
        }
    }

    public ArrayList<ImageFile> getImageFiles() {
        return imageFiles;
    }

    public Uri getThumbnail() {
        return thumbnail;
    }
}