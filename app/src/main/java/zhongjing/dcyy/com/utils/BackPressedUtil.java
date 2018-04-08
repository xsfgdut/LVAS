package zhongjing.dcyy.com.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;

/**
 * Created by Administrator on 2017/3/14 0014.
 */

public class BackPressedUtil {

    // 记录第一次按下的时间
    private long firstPressTime = -1;
    // 记录第二次按下的时间
    private long lastPressTime;
    // 两次按下的时间间隔
    private final long INTERVAL = 2000;

    private static BackPressedUtil instance;

    private BackPressedUtil() {
    }

    //单例
    public static BackPressedUtil getInstance() {
        if (instance == null) {
            synchronized (BackPressedUtil.class) {
                if (instance == null) {
                    instance = new BackPressedUtil();
                }
            }
        }
        return instance;
    }

    /**
     * 显示提示框
     */
    public void showQuitTips(Context context,int msg) {
        // 如果是第一次按下 直接提示
        if (firstPressTime == -1) {
            firstPressTime = System.currentTimeMillis();
            ToastUtil.showToast(context, msg);
        } else {
            lastPressTime = System.currentTimeMillis();
            if (lastPressTime - firstPressTime <= INTERVAL) {
                WifiManagerUtils wifiManagerUtils = new WifiManagerUtils(context);
                WifiInfo wifiInfo = wifiManagerUtils.getNetWorkId();
                wifiManagerUtils.disconnectWifi(wifiInfo.getNetworkId());
                System.exit(0);
            } else {
                firstPressTime = lastPressTime;
                ToastUtil.showToast(context, msg);
            }
        }
    }
}
