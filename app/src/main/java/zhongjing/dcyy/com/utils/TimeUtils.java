package zhongjing.dcyy.com.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dcyy on 2017/1/24.
 */

public class TimeUtils {
    private static final int HOUR = 60 * 60 * 1000;
    private static final int MIN = 60 * 1000;
    private static final int SEC = 1000;

    public static String parseDuration(int duration) {
        int hour = duration / HOUR;
        int min = duration % HOUR / MIN;
        int sec = duration % MIN / SEC;
        if (hour == 0) {
            return String.format("%02d:%02d", min, sec);
        } else {
            return String.format("%02d:%02d:%02d", hour, min, sec);
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
        return format.format(new Date(System.currentTimeMillis()));
    }
}
