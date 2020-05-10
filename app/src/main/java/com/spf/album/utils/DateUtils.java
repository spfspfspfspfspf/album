package com.spf.album.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    public static final String TIME_FORMAT_1 = "yyyy-MM-dd";

    public static String getDateStr(long time) {
        Date date = new Date(time);
        return new SimpleDateFormat(TIME_FORMAT_1, Locale.SIMPLIFIED_CHINESE).format(date);
    }

    public static String getDateStr(long time, String format) {
        Date date = new Date(time);
        return new SimpleDateFormat(format, Locale.SIMPLIFIED_CHINESE).format(date);
    }
}