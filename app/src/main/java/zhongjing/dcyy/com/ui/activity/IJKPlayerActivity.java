package zhongjing.dcyy.com.ui.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import zhongjing.dcyy.com.R;

/**
 * Created by Administrator on 2017/12/5.
 */

public class IJKPlayerActivity extends BaseActivity {

    static {
        System.loadLibrary("libijkffmpeg");
        System.loadLibrary("libijksdl");
        System.loadLibrary("libijkplayer");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.act_play);
    }
}
