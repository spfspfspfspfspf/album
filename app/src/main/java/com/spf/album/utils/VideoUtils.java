package com.spf.album.utils;

import android.media.MediaMetadataRetriever;

public class VideoUtils {

    public static String getDuration(String videoPath) {
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

}