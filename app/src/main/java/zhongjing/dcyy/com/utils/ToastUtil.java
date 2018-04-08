package zhongjing.dcyy.com.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Dcyy on 2016/12/26.
 */

public class ToastUtil {
    private static Toast sToast;

    public static void showToast(Context context, String msg) {
        if (sToast == null) {
            sToast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            sToast.cancel();
            sToast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        }
        sToast.setText(msg);
        sToast.show();
    }

    public static void showToast(Context context, int msg) {
        if (sToast == null) {
            sToast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            sToast.cancel();
            sToast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        }
        sToast.setText(msg);
        sToast.show();
    }
}
