package com.example.mkarte1.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateUtil {
    private DateUtil() {
    }

    public static String todayYmd() {
        return new SimpleDateFormat("yyyyMMdd", Locale.JAPAN).format(new Date());
    }

    public static String tempStamp() {
        return new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.JAPAN).format(new Date());
    }

    public static String displayYmd(String ymd) {
        if (ymd == null || ymd.length() != 8) {
            return "";
        }
        return ymd.substring(0, 4) + "/" + ymd.substring(4, 6) + "/" + ymd.substring(6, 8);
    }
}
