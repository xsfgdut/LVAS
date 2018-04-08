package zhongjing.dcyy.com.utils;


import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Dcyy on 2017/1/23.
 */

public class DateUtils {
    public static String FormatDate(String date){
        long time = Long.parseLong(date);
        SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d =new Date(time);
        String dateFormat=format.format(d);
        return dateFormat;
    }
}
