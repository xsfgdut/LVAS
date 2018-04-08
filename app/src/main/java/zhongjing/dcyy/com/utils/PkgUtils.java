package zhongjing.dcyy.com.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by Dcyy on 2017/2/4.
 */

public class PkgUtils {

    // 获取版本名称
    public static String getVersionName(Context context) {
        String versionName = "";
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);

            versionName = info.versionName;
        } catch (Exception e) {
            versionName = "未知版本";
        }

        return versionName;
    }

    // 获取版本号
    public static int getVersionCode(Context context) {
        int versionCode = 1;
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(context.getPackageName(), 0);

            versionCode = info.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return versionCode;

    }
}
