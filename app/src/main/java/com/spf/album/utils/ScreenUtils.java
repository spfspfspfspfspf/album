package com.spf.album.utils;

import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.spf.album.GalleryApplication;
import com.spf.album.R;

public class ScreenUtils {
    private static volatile int screenWidthPixels = 0;
    private static volatile int screenHeightPixels = 0;

    public static int getScreenWidth() {
        if (screenWidthPixels > 0) {
            return screenWidthPixels;
        }
        Resources resources = GalleryApplication.getApplication().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        screenWidthPixels = displayMetrics.widthPixels;
        screenHeightPixels = displayMetrics.heightPixels;
        return screenWidthPixels;
    }

    public static int getScreenHeight() {
        if (screenHeightPixels > 0) {
            return screenHeightPixels;
        }
        Resources resources = GalleryApplication.getApplication().getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        screenWidthPixels = displayMetrics.widthPixels;
        screenHeightPixels = displayMetrics.heightPixels;
        return screenHeightPixels;
    }

    public static float getDensity() {
        DisplayMetrics displayMetrics = GalleryApplication.getApplication().getResources().getDisplayMetrics();
        return displayMetrics.density;
    }

    public static int dp2px(float dpValue) {
        return (int) (dpValue * getDensity() + 0.5f);
    }

    public static int px2dp(float pxValue) {
        return (int) (pxValue / getDensity() + 0.5f);
    }

    public static int sp2px(float spValue) {
        float fontScale = GalleryApplication.getApplication().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static int px2sp(float pxValue) {
        float fontScale = GalleryApplication.getApplication().getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int getStatusBarHeight() {
        int result = 0;
        Resources resources = GalleryApplication.getApplication().getResources();
        int resId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            result = resources.getDimensionPixelOffset(resId);
        }
        if (result <= 0) {
            result = resources.getDimensionPixelOffset(R.dimen.dp_30);
        }
        return result;
    }
}