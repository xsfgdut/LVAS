package zhongjing.dcyy.com.ui.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.skyworth.splicing.SerialPortUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

import cafe.adriel.androidaudioconverter.AndroidAudioConverter;
import cafe.adriel.androidaudioconverter.callback.IConvertCallback;
import cafe.adriel.androidaudioconverter.callback.ILoadCallback;
import cafe.adriel.androidaudioconverter.model.AudioFormat;
import cn.qqtheme.framework.util.LogUtils;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import zhongjing.dcyy.com.R;
import zhongjing.dcyy.com.app.MyApplication;
import zhongjing.dcyy.com.ui.view.Popwind;
import zhongjing.dcyy.com.utils.PermissionUtil;
import zhongjing.dcyy.com.utils.PicUtils;
import zhongjing.dcyy.com.utils.SPUtils;
import zhongjing.dcyy.com.utils.WifiManagerUtils;
import zhongjing.dcyy.com.widget.widget.media.IRenderView;
import zhongjing.dcyy.com.widget.widget.media.TextureRenderView;

import static zhongjing.dcyy.com.R.id.activity_play_land_rl_right;

public class PlayActivity extends BaseActivity implements IMediaPlayer.OnPreparedListener, IMediaPlayer
        .OnCompletionListener, IRenderView.IRenderCallback, PermissionUtil.OnRequestPermissionsResultCallbacks, SocketUntils.DataRevice{

    private static final int HIDE_VOLUME = 100;
    private static final int HIDE_BRIGHT = 200;
    private static final int CONNECT_ERROR = 300;

//    public static final String ip="192.168.0.12";
    public static final String ip="192.168.11.123";
//    private static final String URL_RTSP = "rtsp://192.168.11.123/1/h264major";
    private static final String URL_RTSP = "rtsp://"+ip+"/1/h264major";
//    private static final String URL_RTSP = "rtsp://10.10.20.74:8554/t";
//    private static final String URL_RTSP = "rtmp://live.hkstv.hk.lxdns.com/live/hks";

    private RelativeLayout topLayout;
    private RelativeLayout bottomLayout;
    private RelativeLayout rightLayout;
    private ImageView unLock_iv;
    private AudioManager audioManager;
    private Window window;
    private WindowManager.LayoutParams lp;
    private GestureDetector detector;
    private SoundPool soundPool;

    private int windowW;
    private int maxVolume;
    private boolean isHide = false;
    private IjkMediaPlayer mediaPlayer;
    private TextureRenderView surfaceView;
    private boolean isLandOrNot;//true为横屏,false为竖屏
    static {
        System.loadLibrary("ijkffmpeg");
        System.loadLibrary("ijksdl");
        System.loadLibrary("ijkplayer");
    }

    private ProgressBar volume_pb;
    private ProgressBar bright_pb;
    private LinearLayout volume_ll;
    private LinearLayout bright_ll;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HIDE_VOLUME:
                    if (isLandOrNot) {
                        mVolume_ll_land.setVisibility(View.GONE);
                    } else {
                        volume_ll.setVisibility(View.GONE);
                    }
                    break;
                case HIDE_BRIGHT:
                    if (isLandOrNot) {
                        mBright_ll_land.setVisibility(View.GONE);
                    } else {
                        bright_ll.setVisibility(View.GONE);
                    }
                    break;
                case CONNECT_ERROR:
                    Toast.makeText(PlayActivity.this, R.string.line_error, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    private Chronometer timer;
    private int mBottomY;
    private Animation mShowAnim;
    private Animation mHideAnim;
    private View mLoading;
    private Socket mSocket;
    private boolean playerIsPrepared;
    /**
     * 当前声音
     */
    private int mVolume = -1;
    /**
     * 当前亮度
     */
    private float mBrightness = -1f;
    private ImageView mView;
//    private GuidePresenter mPresenter;
    private View mPort_up_container;
    private View mPort_down_container;
    private View mLand_container;
    private Chronometer mTimer_land;
    private View mVolume_ll_land;
    private ProgressBar mVolume_pb_land;
    private View mBright_ll_land;
    private ProgressBar mBright_pb_land;
    private boolean isFirst=true;
    private SerialPortUtil portUtil;

    private static final String SAVE_PIC_PATH = Environment.getExternalStoragePublicDirectory("").getAbsolutePath();//保存到SD卡
    public static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/LVAS";//保存的确切位置

    private ImageView btn_vcr_land;
    private ImageView btn_record_land;
    private ImageView btn_camera_land;
    public   SocketUntils socketUntils =SocketUntils.getInstance();
    private Popwind popwind;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initWindow();
        initView();
        initVolumeAndBright();
        initListener();
        initEvent();
        initAnimation();
//        initSocket();//初始化socket
//        initReConnectedTask();//初始化重连SOCKET任务

        initGuide();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
        Window window = getWindow();

        // Translucent status bar
        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);


        AndroidAudioConverter.load(getApplicationContext(),new ILoadCallback() {
            @Override
            public void onSuccess() {
                Log.i("AndroidAudioConverter", " load ffmpeng Success: ");
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("AndroidAudioConverter", "onFailure:  load error",e );
            }
        });

        PermissionUtil.getExternalStoragePermissions(this,1001);

    }

    /**
     * 接收到数据
     * @param data
     */
    @Override
    public void revivce(String data) {
        if(null!=popwind){
            popwind.setValue(data);
        }
    }

    @Override
    public void error(String data) {
        mHandler.sendEmptyMessage(CONNECT_ERROR);
    }



    private void goToJudgeWifiIsRightOrError() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                SystemClock.sleep(2000);
                WifiManagerUtils wifiManagerUtils = new WifiManagerUtils(PlayActivity.this);
                WifiInfo wifiInfo = wifiManagerUtils.getNetWorkId();
                //if (wifiInfo.getSSID() == null || !wifiInfo.getSSID().toLowerCase().contains("zjbox"))
                if (wifiInfo.getSSID() == null || !wifiInfo.getSSID().toLowerCase().contains("GXTV")) {//当前连接的wifi不是我们想要的目标
                    Log.d("测试", wifiInfo.getSSID());
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("测试", "当前WIFI不对");
/*                            startActivity(new Intent(PlayActivity.this, CheckNetActivity_New.class));
                            overridePendingTransition(R.anim.slide_up_in,
                                    R.anim.slide_down_out);
                            finish();*/
                        }
                    }, 500);
                } else {
                    Log.d("测试", "WIFI正确===========" + wifiInfo.getSSID());
                }
            }
        }).start();
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.i("info", "landscape"); // 横屏
            setViewforLand();
            if (isRecodingOrNot) {
                stopRecordVideo();
            }
            isRecodingOrNot = false;
          findViewById(R.id.activity_play_land_rl_right).setVisibility(View.GONE);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.i("info", "portrait"); // 竖屏
            setViewForPort();
            if (isRecodingOrNot) {
                stopRecordVideo();
            }
        }
    }

    private void initGuide() {
       // mPresenter = new GuidePresenter(this);

        boolean hasGuide = (boolean) SPUtils.get(this, SPUtils.GUIDE, false);//是否进入引导页
        if (!hasGuide) {
            SPUtils.put(this, SPUtils.GUIDE, true);
            mView.post(new Runnable() {
                @Override
                public void run() {
//                    //日语版本去掉引导层
//                   mPresenter.initGuideView11();
//                    mPresenter.initGuideView();
//                    mPresenter.initGuideView2();
//                    mPresenter.initGuideView3();
//                    mPresenter.initGuideView4();
//                    mPresenter.initGuideView5();
//                    mPresenter.initGuideView6();
//                    mPresenter.initGuideView7();
//                    mPresenter.initGuideView8();
//                    mPresenter.initGuideView9();
//                    mPresenter.initGuideView10();
                }
            });
        }
    }


    private void initAnimation() {
        mShowAnim = AnimationUtils.loadAnimation(this, R.anim.show_anim);
        mHideAnim = AnimationUtils.loadAnimation(this, R.anim.hide_anim);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        try {
            mSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            Log.d("测试", "socket为空");
        }
        cachedThreadPool.shutdown();
    }

    private void initView() {
        //播放器视图
        surfaceView = (TextureRenderView) findViewById(R.id.activity_play_sv);

         /*----------------------------- 竖屏 ---------------------------------*/

        mPort_up_container = findViewById(R.id.activity_play_port_up_container);//竖屏状态下,上部分的容器
        mLoading = findViewById(R.id.loading);//加载动画
        mLoading.setVisibility(View.VISIBLE);
        timer = (Chronometer) findViewById(R.id.activity_play_timer);//计时器

        volume_ll = (LinearLayout) findViewById(R.id.activity_play_volume);//音量容器
        volume_pb = (ProgressBar) findViewById(R.id.volume_pb); //音量pb
        bright_ll = (LinearLayout) findViewById(R.id.activity_play_bright);//亮度容器
        bright_pb = (ProgressBar) findViewById(R.id.bright_pb); //亮度pb

        mView = (ImageView) findViewById(R.id.activity_play_btn_camera);//截图,变量的作用仅为了开启引导界面

        mPort_down_container = findViewById(R.id.activity_play_port_down_container); //竖屏状态下,下部分的容器
        /*----------------------------- 竖屏 ---------------------------------*/


        /*----------------------------- 横屏 ---------------------------------*/
        mLand_container = findViewById(R.id.activity_play_land_contain);//横屏状态下的容器
        mTimer_land = (Chronometer) findViewById(R.id.activity_play_land_timer);//计时器
        mVolume_ll_land = findViewById(R.id.activity_play_volume_land);//音量容器
        mVolume_pb_land = (ProgressBar) findViewById(R.id.volume_pb_land);//音量pb
        mBright_ll_land = findViewById(R.id.activity_play_bright_land);//亮度容器
        mBright_pb_land = (ProgressBar) findViewById(R.id.bright_pb_land);//亮度pb

        topLayout = (RelativeLayout) findViewById(R.id.activity_play_land_rl_top);  //横屏顶部菜单栏
        bottomLayout = (RelativeLayout) findViewById(R.id.activity_play_land_rl_bottom);//横屏底部菜单栏
        rightLayout = (RelativeLayout) findViewById(activity_play_land_rl_right);//横屏右侧菜单栏
        rightLayout.setVisibility(View.GONE);
        unLock_iv = (ImageView) findViewById(R.id.activity_play_land_btn_unlock);//横屏锁屏键
        /*----------------------------- 横屏 ---------------------------------*/

      btn_vcr_land= (ImageView)findViewById(R.id.activity_play_btn_vcr_land1);
      btn_record_land  = (ImageView)findViewById(R.id.activity_play_btn_record_land);
      btn_camera_land = (ImageView)findViewById(R.id.activity_play_btn_camera_land);

    }

    //初始化音量和亮度
    private void initVolumeAndBright() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);//flag 1 显示系统调节控件 0 不显示

        volume_pb.setProgress(volume / maxVolume * 100);

        window = this.getWindow();
        lp = window.getAttributes();
        try {
            saveBright = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            float currentBright = (float) saveBright / 255 * 100;
            bright_pb.setProgress((int) currentBright);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    //初始化监听器
    private void initListener() {
        detector = new GestureDetector(this, new MyGestureListener());
        findViewById(R.id.activity_play_btn_record_land).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                click(v);
            }
        });
    }

    private void initEvent() {
        mediaPlayer = new IjkMediaPlayer();
        surfaceView.addRenderCallback(this);

        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "probesize", 1024 * 16);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "analyzeduration", 5000);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 0);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_frame", 0);
        // Param for living
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "max_cached_duration", 1000);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "infbuf", 1);
        mediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "packet-buffering", 0);

        try {
            mediaPlayer.setDataSource(URL_RTSP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //mediaPlayer准备工作-------回调,onPrepared
        mediaPlayer.setOnPreparedListener(this);
        //MediaPlayer完成---------回调,onCompletion
        mediaPlayer.setOnCompletionListener(this);
        //截图时声音
        soundPool = new SoundPool(1, AudioManager.STREAM_SYSTEM, 5);
        soundPool.load(this, R.raw.shutter, 1);
    }

    //获取屏幕宽高
    private void initWindow() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        windowW = point.x;
    }

    @Override
    public void onPrepared(IMediaPlayer iMediaPlayer) {
        iMediaPlayer.start();
        playerIsPrepared = true;
        Log.d("测试", "onPrepared===准备完毕");
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mLoading.setVisibility(View.GONE);
            }
        }, 800);
    }

    @Override
    protected void onResume() {
        super.onResume();
        socketUntils.setDataRevice(this);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        goToJudgeWifiIsRightOrError();
        if (playerIsPrepared) {
            mediaPlayer.start();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if( null!=mediaPlayer &&  mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            if (isRecodingOrNot) {
                stopRecordVideo();
            }
            isRecodingOrNot=false;
        }
    }

    @Override
    public void onCompletion(IMediaPlayer iMediaPlayer) {
        Log.d("测试", "onCompletion");
    }


    public  String result;
   // public void sendSocket2(final char msg) {sendSocket2(msg+"");}
    //发送socket2(长连接)
    /**
    public void sendSocket2(final String msg) {
        cachedThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                DataOutputStream writer = null;
                try {
                    Log.d("测试", "点击发送信息" + msg + "====SOCKET====" + mSocket.hashCode());
                    if (mSocket == null) {
                        mHandler.sendEmptyMessage(CONNECT_ERROR);
                        return;
                    }
                    writer = new DataOutputStream(mSocket.getOutputStream());
                    writer.write( msg.getBytes());
                    writer.flush();
                   // writer.close();
                    //等待500ms
                    try {
                        Thread.currentThread().sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    result="";
                    //3、获取输入流，并读取服务器端的响应信息
                      InputStream is = mSocket.getInputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is));
                    String info = null;
                     while((info+=br.readLine())!=null){
                        LogUtils.warn("我是客户端，服务器说："+info);
                       }
                    br.close();

                    result=info;

                } catch (IOException e) {
                    Log.d("测试", "点击发送信息出错" + e.toString() + "====SOCKET====" + mSocket.hashCode());
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Log.d("测试", "点击发送信息出错" + e.toString());
                }
            }
        });
    }
     **/

    private boolean unlock = true;
    private int ImageRes=R.drawable.ic_play_pause;//图片资源
    private boolean isRecodingOrNot = false;//true是正在录像,false
    private boolean isRecodingSound = false;//true是正在录音,false



    public void click(View view) {
        switch (view.getId()) {
            case R.id.activity_play_land_btn_unlock:            //横屏锁屏键
                unlock = !unlock;
                if (unlock) {
                    unLock_iv.setImageResource(R.drawable.land_btn_unlock);
                } else {
                    unLock_iv.setImageResource(R.drawable.land_btn_lock);
                    hide();
                }
                break;
            case R.id.activity_play_btn_fullscreen:            //竖屏全屏键
//                setViewforLand();
//                if (isRecodingOrNot) {
//                    stopRecordVideo();
//                }
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case  R.id.activity_play_btn_play_land: //播放
                 if( ImageRes == R.drawable.ic_play_video){
                     ImageRes=R.drawable.ic_play_pause;
                     mediaPlayer.start();
                     btn_vcr_land.setEnabled(true);
                     btn_vcr_land.clearColorFilter();
                     btn_record_land.setEnabled(true);
                     btn_record_land.clearColorFilter();
                     btn_camera_land .setEnabled(true);
                     btn_camera_land.clearColorFilter();

                 }else{
                     ImageRes=R.drawable.ic_play_video;
                     mediaPlayer.pause();
                     if (!isRecodingOrNot) {} else {
                         stopRecordVideo_land();
                     }

                     isRecodingOrNot=false;
                     btn_vcr_land.setEnabled(false);
                     btn_record_land.setEnabled(false);
                     btn_camera_land .setEnabled(false);

                     btn_vcr_land.setColorFilter(getResources().getColor(R.color.lightgray2));
                     btn_record_land.setColorFilter(getResources().getColor(R.color.lightgray2));
                     btn_camera_land.setColorFilter(getResources().getColor(R.color.lightgray2));
                 }
                ((ImageView)view).setImageResource(ImageRes);
                break;
            case R.id.activity_play_btn_back:                  //竖屏返回键
                finish();
                if (isRecodingOrNot) {
                    stopRecordVideo();
                }
                break;
            case R.id.activity_play_land_btn_back:              //横屏返回键
            case R.id.activity_play_land_btn_fullscreen:        //横屏全屏键
                setViewForPort();
                if (isRecodingOrNot) {
                    stopRecordVideo();
                }
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case R.id.activity_play_btn_camera:                //竖屏截图键
            case R.id.activity_play_btn_camera_land:           //横屏截图
                Bitmap bitmap = surfaceView.getBitmap();
                screenShot(bitmap);
                soundPool.play(1, 1, 1, 0, 0, 1);
                break;
            case R.id.activity_play_btn_vcr:                   //录像键
                LogUtils.warn("isRecodingOrNot:" +isRecodingOrNot);

                if(!isRecodingSound) {
                    if (!isRecodingOrNot) {
                        startRecordVideo();
                    } else {
                        stopRecordVideo();
                    }
                    isRecodingOrNot = !isRecodingOrNot;
                }else{
                    Toast.makeText(this, "正在录制音频中，请先停止然后在开始录制视频！", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.activity_play_btn_vcr_land1: {              //录像键

                LogUtils.warn("isRecodingOrNot:" + isRecodingOrNot);
                if (!isRecodingOrNot) {
                    startRecordVideo_land();
                } else {
                    stopRecordVideo_land();
                }
                isRecodingOrNot = !isRecodingOrNot;
            } break;
            case R.id.activity_play_btn_ok:
            case R.id.activity_play_btn_ok_land:
                socketUntils.sendSocket2('g');  //103
                break;
            case R.id.activity_play_btn_up:                    //上键
            case R.id.activity_play_btn_up_land:               //上键
                socketUntils.sendSocket2('e'); //101
                break;
            case R.id.activity_play_btn_down:                   //下键
            case R.id.activity_play_btn_down_land:              //下键
                socketUntils.sendSocket2('f');  //102
                break;
            case R.id.activity_play_btn_left:                   //左键
            case R.id.activity_play_btn_left_land:              //左键
                socketUntils.sendSocket2('h'); //104
                break;
            case R.id.activity_play_btn_right:                  //右键
            case R.id.activity_play_btn_right_land:             //右键
                socketUntils.sendSocket2('i'); //105
                break;
//            case R.id.activity_play_btn_menu:                  //菜单键
//            case R.id.activity_play_btn_menu_land:             //菜单键
//                sendSocket2('d'); //100
//                break;
//            case R.id.activity_play_btn_closemenu:             //关闭菜单键
//            case R.id.activity_play_btn_closemenu_land:        //关闭菜单键
//                sendSocket2('j'); //106
//                break;
            case R.id.activity_play_btn_menu_land:        //查看录制的 音频视频
                Intent cacheIntent = new Intent(this, CacheActivity.class);
                startActivity(cacheIntent);
                break;
            case R.id.activity_play_btn_closemenu_land:        //wifi设置界面
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;

            case R.id.activity_play_btn_record_land:{ //录音

                if (!isRecodingOrNot) {
                    if (!isRecodingSound) {
                        startRecordVideo_land();
                    } else {
                        stopRecordVideo_land();
                        //   Toast.makeText(this, videoPath +"", Toast.LENGTH_SHORT).show();
                        if (!TextUtils.isEmpty(videoPath) && new File(videoPath).exists()) {

                                Toast.makeText(PlayActivity.this, "音频文件转码中.." , Toast.LENGTH_LONG).show();
                                mHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        String aacPath = new File(videoPath).getParent() + File.separator + System
                                                .currentTimeMillis() + ".aac" ;
                                        Log.i("click", "aacPath :" +aacPath);
                                        try {
                                            splitMp4(videoPath, aacPath);
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                        convertAudio(aacPath);
                                        new File(videoPath).delete();
                                    }
                                },1500);
                        }
                    }
                    isRecodingSound =!isRecodingSound;
                }else{
                    Toast.makeText(this, "正在录制视频中，请先停止然后在开始录制音频！", Toast.LENGTH_SHORT).show();
                }
            } break;
            case  R.id.activity_play_btn_noise_land:{ //降噪音
                popwind=new Popwind(this,"降噪",3);
                popwind.setMax(100);
                popwind.setUnti("级");
                popwind.show(view);
            }break;
            case R.id.activity_play_btn_sound_land:{ //声音等级
                popwind=new Popwind(this,"声音",1);
                popwind.setMax(100);
                popwind.show(view);
            }break;
            case R.id.activity_play_btn_light_land:{ //亮度等级
                popwind=new Popwind(this,"亮度",2);
                popwind.setMax(100);
                popwind.show(view);
            }break;

        }
    }

    /**
     * 将 Mp4 的音频和视频分离
     *
     * @param mp4Path .mp4
     * @param outPath .mp4
     */
    public   void splitMp4(String mp4Path, String outPath) throws IOException {
        try {
            Log.e("splitMp4", "splitMp4: "+new  File(mp4Path).exists() );
            Movie videoMovie = MovieCreator.build(mp4Path);
            Track videoTracks = null;// 获取视频的单纯视频部分
            Track audioTracks = null;// 获取视频的单纯音频部分
            for (Track videoMovieTrack : videoMovie.getTracks()) {
                if ("soun".equals(videoMovieTrack.getHandler())) {
                    audioTracks = videoMovieTrack;
                }
            }
            Movie resultMovie = new Movie();
            resultMovie.addTrack(audioTracks);// 声音部分
            Log.w("ds", outPath + " ");
            Container out = new DefaultMp4Builder().build(resultMovie);
            FileOutputStream fos = new FileOutputStream(new File(outPath));
            out.writeContainer(fos.getChannel());
            fos.flush();
            fos.close();
//
//            String [] cmds=new String[]{" -i ",mp4Path ," -vn","-y"," -acodec","copy ",outPath };
//            FFmpeg.getInstance( getApplicationContext()).execute(cmds, new FFmpegExecuteResponseHandler() {
//                public void onStart() { }
//
//                public void onProgress(String message) { }
//
//                public void onSuccess(String message) {
//                    Log.e("FFmpeg", "onSuccess: "+message);
//                }
//
//                public void onFailure(String message) {
//                    Log.e("FFmpeg", "onFailure: "+message);
//                }
//                public void onFinish() {}
//            });

        }catch (Exception ex){
            ex.printStackTrace();
        }
    }



    /**
     * AAC 转 mp3 wav
     */
    public void  convertAudio( String  filepath){
        final File wavFile = new File(filepath);

          IConvertCallback callback = new IConvertCallback() {
            @Override
            public void onSuccess(File convertedFile) {
                Log.i("IConvertCallback", "onSuccess: "+convertedFile.getPath());
                Toast.makeText(PlayActivity.this, "文件转码成功!" , Toast.LENGTH_LONG).show();
                wavFile.delete();
            }
            @Override
            public void onFailure(Exception error) {
                Log.e("IConvertCallback", "ERROR: "+error.getMessage());
                Toast.makeText(PlayActivity.this, "文件转码失败!" , Toast.LENGTH_LONG).show();
               // Toast.makeText(PlayActivity.this, "ERROR: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        if("mp3".equalsIgnoreCase( (String)SPUtils.get(this,SPUtils.SOUND_FILE_TYPE,"mp3"))){
            AndroidAudioConverter.with(PlayActivity.this)
                    .setFile(wavFile)
                    .setFormat(AudioFormat.MP3)
                    .setCallback(callback)
                    .convert();
        }else{
            AndroidAudioConverter.with(PlayActivity.this)
                    .setFile(wavFile)
                    .setFormat(AudioFormat.WAV)
                    .setCallback(callback)
                    .convert();
        }
    }


    private void setViewForPort() {
        mPort_up_container.setVisibility(View.VISIBLE);
        mPort_down_container.setVisibility(View.VISIBLE);
        mLand_container.setVisibility(View.GONE);
        isLandOrNot = false;
        volume_pb.setProgress(mVolume_pb_land.getProgress());
        bright_pb.setProgress(mBright_pb_land.getProgress());
        hideView();
    }

    //因为音量亮度调节窗口有延迟,如果切换了横竖屏会再次切换回来会导致视图仍然存在,不会消失,因此每次切换之前先隐藏
    private void hideView() {
        mVolume_ll_land.setVisibility(View.GONE);
        mBright_ll_land.setVisibility(View.GONE);
        bright_ll.setVisibility(View.GONE);
        volume_ll.setVisibility(View.GONE);
    }

    private void setViewforLand() {
        mPort_up_container.setVisibility(View.GONE);
        mPort_down_container.setVisibility(View.GONE);
        mLand_container.setVisibility(View.VISIBLE);
        isLandOrNot = true;
        mVolume_pb_land.setProgress(volume_pb.getProgress());
        mBright_pb_land.setProgress(bright_pb.getProgress());
        hideView();
    }

    String path = Environment.getExternalStoragePublicDirectory("") + "/111myprint/";
//    private static final String SAVE_PIC_PATH = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡


    private  String moves;
    private String  videoPath="";
    private void recordVideo() {
        File  file= new File(SAVE_REAL_PATH);
        if(file.exists()){
            file.mkdir();
        }
           moves=SAVE_REAL_PATH;
        new Thread(new Runnable() {
            @Override
            public void run() {
                File VideoPath = new File(moves, System.currentTimeMillis() + ".mp4");
                videoPath=VideoPath.getAbsolutePath();
                mediaPlayer.lm_rtspRecordVideo(URL_RTSP, VideoPath.getAbsolutePath());
            }
        }).start();    }

    //开始录制视频
    private void startRecordVideo() {
        timer.setVisibility(View.VISIBLE);
        timer.startAnimation(mShowAnim);
        timer.setBase(SystemClock.elapsedRealtime());//计时器清零
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
        timer.setFormat("0" + String.valueOf(hour) + ":%s");
        timer.start();
        mediaPlayer.lm_rtspSetRecodeStop(false);
        recordVideo();
        Toast.makeText(this, "开始录制！", Toast.LENGTH_SHORT).show();

    }

    //开始录制视频
    private void startRecordVideo_land() {
        mTimer_land.setVisibility(View.VISIBLE);
        mTimer_land.startAnimation(mShowAnim);
        mTimer_land.setBase(SystemClock.elapsedRealtime());//计时器清零
        int hour = (int) ((SystemClock.elapsedRealtime() - mTimer_land.getBase()) / 1000 / 60);
        mTimer_land.setFormat("0" + String.valueOf(hour) + ":%s");
        mTimer_land.start();
        mediaPlayer.lm_rtspSetRecodeStop(false);
        recordVideo();
        Toast.makeText(this, "开始录制！", Toast.LENGTH_SHORT).show();
    }


    //停止录制视频
    private void stopRecordVideo() {
        mediaPlayer.lm_rtspSetRecodeStop(true);
        timer.stop();
        timer.setVisibility(View.GONE);
        timer.startAnimation(mHideAnim);

            Toast.makeText(this, "停止录制！", Toast.LENGTH_SHORT).show();

    }

    //停止录制视频
    private void stopRecordVideo_land() {
        mediaPlayer.lm_rtspSetRecodeStop(true);
        mTimer_land.stop();
        mTimer_land.setVisibility(View.GONE);
        mTimer_land.startAnimation(mHideAnim);

        Toast.makeText(this, "停止录制！", Toast.LENGTH_SHORT).show();

    }

    //截图
    private void screenShot(Bitmap bitmap) {
        String picName = System.currentTimeMillis() + ".JPEG";
        File  file1= new File(SAVE_REAL_PATH);
        if(file1.exists()){
            file1.mkdir();
        }
        String  path= SAVE_REAL_PATH;
        //Toast.makeText(this, path+"  " +picName, Toast.LENGTH_SHORT).show();
        PicUtils.saveBitmap(bitmap,path, picName);
        File file = new File(path, picName);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
        sendBroadcast(intent);
        //Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, this.getResources().getString(R.string.save_photo), Toast.LENGTH_SHORT).show();

    }

    //手动更改音量和亮度
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (detector.onTouchEvent(event))
            return true;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                endGesture();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 手势结束
     */
    private void endGesture() {
        mVolume = -1;
        mBrightness = -1f;

        // 隐藏
        mHandler.removeMessages(HIDE_BRIGHT);
        mHandler.removeMessages(HIDE_VOLUME);
        mHandler.sendEmptyMessageDelayed(HIDE_BRIGHT, 1000);
        mHandler.sendEmptyMessageDelayed(HIDE_VOLUME, 1000);
    }

    //改变系统亮度
    private int saveBright;

    @Override
    public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {

        holder.bindToMediaPlayer(mediaPlayer);
        //开启异步准备
        try {
            Log.d("测试", "onSurfaceCreated");
            mediaPlayer.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {

    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms, boolean isAllGranted) {
      File file=  new File(((MyApplication)getApplication()).SAVE_REAL_PATH);
        if(!file.exists()){
            file.mkdir();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms, boolean isAllDenied) {

    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {    //单击
            if (unlock) {
                showOrHide();
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {             //双击
            return super.onDoubleTap(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {                //长按
            super.onLongPress(e);
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            mBottomY = surfaceView.getBottom();//播放器界面Y坐标作为滑动距离
            float mOldX = e1.getX(), mOldY = e1.getY();
            int y = (int) e2.getRawY();

            if (!unlock || mOldY > mBottomY || y > mBottomY) {
                //什么也不干
            } else {
                if (mOldX > windowW / 2)// 右边滑动
                    onVolumeSlide((mOldY - y) / mBottomY);
                else // 左边滑动
                    onBrightnessSlide((mOldY - y) / mBottomY);
            }
            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    /**
     * 滑动改变声音大小
     *
     * @param percent
     */
    private void onVolumeSlide(float percent) {
        if (mVolume == -1) {
            mVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mVolume < 0)
                mVolume = 0;

            // 显示
            mHandler.sendEmptyMessage(HIDE_BRIGHT);

            if (isLandOrNot) {
                mVolume_ll_land.setVisibility(View.VISIBLE);
            } else {
                volume_ll.setVisibility(View.VISIBLE);
            }
//            mOperationBg.setImageResource(R.drawable.video_volumn_bg);
//            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }

        int index = (int) (percent * maxVolume) + mVolume;
        if (index > maxVolume)
            index = maxVolume;
        else if (index < 0)
            index = 0;

        // 变更声音
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        float currentVolume = (float) index / maxVolume * 100;
        if (isLandOrNot) {
            mVolume_pb_land.setProgress((int) currentVolume);
        } else {
            volume_pb.setProgress((int) currentVolume);
        }
        mHandler.removeMessages(HIDE_VOLUME);
        mHandler.sendEmptyMessageDelayed(HIDE_VOLUME, 1000);
//        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
//        lp.width = findViewById(R.id.operation_full).getLayoutParams().width
//                * index / mMaxVolume;
//        mOperationPercent.setLayoutParams(lp);
    }

    /**
     * 滑动改变亮度
     *
     * @param percent
     */
    private void onBrightnessSlide(float percent) {
        if (mBrightness < 0) {
            mBrightness = getWindow().getAttributes().screenBrightness;
            if (mBrightness <= 0.00f)
                mBrightness = 0.50f;
            if (mBrightness < 0.01f)
                mBrightness = 0.01f;

            // 显示
            mHandler.sendEmptyMessage(HIDE_VOLUME);
            if (isLandOrNot) {
                mBright_ll_land.setVisibility(View.VISIBLE);
            } else {
                bright_ll.setVisibility(View.VISIBLE);
            }
//            mOperationBg.setImageResource(R.drawable.video_brightness_bg);
//            mVolumeBrightnessLayout.setVisibility(View.VISIBLE);
        }
        WindowManager.LayoutParams lpa = getWindow().getAttributes();
        lpa.screenBrightness = mBrightness + percent;
        if (lpa.screenBrightness > 1.0f)
            lpa.screenBrightness = 1.0f;
        else if (lpa.screenBrightness < 0.01f)
            lpa.screenBrightness = 0.01f;
        getWindow().setAttributes(lpa);
        if (isLandOrNot) {
            mBright_pb_land.setProgress((int) (lp.screenBrightness * 255));
        } else {
            bright_pb.setProgress((int) (lp.screenBrightness * 255));
        }
        mHandler.removeMessages(HIDE_BRIGHT);
        mHandler.sendEmptyMessageDelayed(HIDE_BRIGHT, 1000);

//        ViewGroup.LayoutParams lp = mOperationPercent.getLayoutParams();
//        lp.width = (int) (findViewById(R.id.operation_full).getLayoutParams().width * lpa.screenBrightness);
//        mOperationPercent.setLayoutParams(lp);
    }

    //显示或隐藏菜单栏
    private void showOrHide() {
        if (isHide) {
            show();                                //显示菜单栏
            unLock_iv.setVisibility(View.VISIBLE); //显示锁屏键
            unLock_iv.startAnimation(mShowAnim);
        } else {
            hide();                                 //隐藏菜单栏
            unLock_iv.setVisibility(View.INVISIBLE);//隐藏锁屏键
            unLock_iv.startAnimation(mHideAnim);
        }
    }

    //显示菜单栏
    private void show() {
        topLayout.setVisibility(View.VISIBLE);
        bottomLayout.setVisibility(View.VISIBLE);
      //  rightLayout.setVisibility(View.VISIBLE);

        topLayout.startAnimation(mShowAnim);
        bottomLayout.startAnimation(mShowAnim);
       // rightLayout.startAnimation(mShowAnim);
        isHide = false;
    }

    //隐藏菜单栏
    private void hide() {
        topLayout.setVisibility(View.INVISIBLE);
        bottomLayout.setVisibility(View.INVISIBLE);
      //  rightLayout.setVisibility(View.INVISIBLE);
        topLayout.startAnimation(mHideAnim);
        bottomLayout.startAnimation(mHideAnim);
       // rightLayout.startAnimation(mHideAnim);
        isHide = true;
    }


}
