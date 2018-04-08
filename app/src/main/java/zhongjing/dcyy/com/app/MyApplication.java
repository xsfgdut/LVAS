package zhongjing.dcyy.com.app;

import android.app.Application;
import android.os.Environment;
import android.util.Log;

import com.tencent.bugly.Bugly;
import com.zhy.http.okhttp.OkHttpUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import zhongjing.dcyy.com.utils.SPUtils;

/**
 * Created by Dcyy on 2017/1/10.
 */

public class MyApplication extends Application {
    public String SAVE_REAL_PATH;
    @Override
    public void onCreate() {
        super.onCreate();
        SPUtils.put(this, SPUtils.FRAMEPORTHEADER, "5AA5");
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);
        //Bugly
        Bugly.init(getApplicationContext(), "523a62318c", false);
        String SAVE_PIC_PATH = getSDPath();
        //保存到SD卡
          SAVE_REAL_PATH = SAVE_PIC_PATH + "/LVAS";//保存的确切位置
        Log.i(getClass().getSimpleName(), "onCreate: "+SAVE_REAL_PATH);
        Log.i(getClass().getSimpleName(), "onCreate: "+getExternalFilesDir(Environment.DIRECTORY_MOVIES).getAbsolutePath());

        File file = new File(SAVE_REAL_PATH);

        if (!file.exists()) {
            file.mkdir();
        }


    }

    public String getSDPath(){
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);//判断sd卡是否存在
        if(sdCardExist)
        {
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        }
        return sdDir.toString();
    }
}
