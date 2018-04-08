package zhongjing.dcyy.com.ui.activity;

import android.app.ActivityManager;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.support.v7.app.AppCompatActivity;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.Call;
import zhongjing.dcyy.com.utils.WifiManagerUtils;

/**
 * Created by Dcyy on 2017/1/12.
 */

public class BaseActivity extends AppCompatActivity {

    protected ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    protected ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
    protected void OkHttpUtils_get(String url, final int type){
        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        onError_get(call,e,id,type);
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        onResponse_get(response,id,type);

                    }
                });
    }

    protected void onResponse_get(String response, int id, int type){};

    protected void onError_get(Call call, Exception e, int id,int type){};

    @Override
    protected void onStop() {
        super.onStop();
//        if (!isAppOnForeground()) {
//            //app 进入后台
//            WifiManagerUtils wifiManagerUtils = new WifiManagerUtils(this);
//            WifiInfo wifiInfo = wifiManagerUtils.getNetWorkId();
//            wifiManagerUtils.disconnectWifi(wifiInfo.getNetworkId());
//        }
    }

    public boolean isAppOnForeground() {
        // Returns a list of application processes that are running on the
        // device

        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            // The name of the process that this object is associated with.
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }
}
