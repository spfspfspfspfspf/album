package com.spf.album.utils;

import android.text.TextUtils;
import android.util.Log;

public class LogUtils {
    private static final String TAG = "spf";

    public static void d(String content) {
        d(null, content);
    }

    public static void d(String tag, String content) {
        if (TextUtils.isEmpty(tag) && TextUtils.isEmpty(content)) {
            return;
        }
        String message = "";
        if (TextUtils.isEmpty(tag)) {
            message = content;
        } else if (TextUtils.isEmpty(content)) {
            message = tag;
        } else {
            message = tag + " - " + content;
        }
        Log.d(TAG, message);
    }

}
