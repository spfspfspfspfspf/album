package com.spf.album;

import android.app.Application;

import com.bumptech.glide.Glide;

public class CustomApplication extends Application {

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).onTrimMemory(level);
    }
}