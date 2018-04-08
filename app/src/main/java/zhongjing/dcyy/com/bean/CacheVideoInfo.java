package zhongjing.dcyy.com.bean;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * Created by Dcyy on 2017/1/23.
 */

public class CacheVideoInfo implements Serializable{
    public Bitmap videoBitmap;  //视频第一帧
    public long videoSize; //视频大小
    public String videoName;    //视频名称
    public int isImage=0; // 1 视频  2 图片  3 声音
    public String filePath; //文件路径

    @Override
    public String toString() {
        return "videoName ="+videoName+"videoSize = "+videoSize;
    }
}
