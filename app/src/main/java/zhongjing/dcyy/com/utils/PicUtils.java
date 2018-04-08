package zhongjing.dcyy.com.utils;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Dcyy on 2017/1/16.
 */

public class PicUtils {
    /**
     * 首先默认文件保存路径
     */
    public static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
    //public static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/zhongjing";//保存的确切位置
    public static  final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/ZJBOX";//保存的确切位置
//    public static final String SAVE_REAL_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/zhongjing/";
    public static void saveBitmap(Bitmap bm, String fileName) {
        saveBitmap(bm,SAVE_REAL_PATH,fileName);
//        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/zhongjing/");
    /**    File filePath = new File(SAVE_REAL_PATH);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        File picPath = new File(filePath, fileName);
        try {
            if(!picPath.exists()){
                picPath.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("测试", "展示错误" + e.toString());
        }

        try {

//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(picPath));
            FileOutputStream foStream = new FileOutputStream(picPath);
            bm.compress(Bitmap.CompressFormat.PNG, 100, foStream);
            foStream.flush();
            foStream.close();
        } catch (IOException e) {
            Log.d("测试", "展示错误" + e.toString());
        }
     **/
    }

    public static void saveBitmap(Bitmap bm, String path, String fileName) {
//        File filePath = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/zhongjing/");
        File filePath = new File(path);
        if (!filePath.exists()) {
            filePath.mkdirs();
        }
        File picPath = new File(filePath, fileName);
        try {
            if(!picPath.exists()){
                picPath.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d("测试", "展示错误" + e.toString());
        }

        try {

//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(picPath));
            FileOutputStream foStream = new FileOutputStream(picPath);
            bm.compress(Bitmap.CompressFormat.PNG, 100, foStream);
            foStream.flush();
            foStream.close();
        } catch (IOException e) {
            Log.d("测试", "展示错误" + e.toString());
        }
    }
}
