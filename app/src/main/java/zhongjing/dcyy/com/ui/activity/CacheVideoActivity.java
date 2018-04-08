package zhongjing.dcyy.com.ui.activity;

import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import zhongjing.dcyy.com.R;
import zhongjing.dcyy.com.utils.DateUtils;
import zhongjing.dcyy.com.utils.TimeUtils;
import zhongjing.dcyy.com.widget.widget.media.IRenderView;
import zhongjing.dcyy.com.widget.widget.media.TextureRenderView;

public class CacheVideoActivity extends BaseActivity implements IRenderView.IRenderCallback, IMediaPlayer.OnPreparedListener, IMediaPlayer.OnCompletionListener, SeekBar.OnSeekBarChangeListener {
    private static final int HIDE_VOLUME = 100;
    private static final int HIDE_BRIGHT = 200;
    private static final int UPDATE_PROGRESS = 300;

    private String videoName;
    private boolean isHide = false;
    private boolean unlock = true;

    private int maxVolume;
    private int windowH;
    private int windowW;
    private float startY;
    private float startX;
    private float endX;
    private float endY;
    private int startVolume;

    private GestureDetector detector;
    private AudioManager audioManager;
    private Window window;
    private WindowManager.LayoutParams lp;

    private IjkMediaPlayer mediaPlayer;
    private TextureRenderView surfaceView;
    private TextView videoName_tv;
    private ImageView unlock_iv;
    private LinearLayout topLayout;

    private ProgressBar volume_pb;
    private ProgressBar bright_pb;
    private LinearLayout volume_ll;
    private LinearLayout bright_ll;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HIDE_VOLUME:
                    volume_ll.setVisibility(View.GONE);
                    break;
                case HIDE_BRIGHT:
                    bright_ll.setVisibility(View.GONE);
                    break;
                case UPDATE_PROGRESS:
                    startUpdateProgress();
                    break;
            }
        }
    };
    private LinearLayout bottomLayout;
    private SeekBar seekBar;
    private long duration;
    private TextView playTime;
    private TextView totalTime;
    private ImageView playState;
    private File videoPath;
    private boolean playerIsPrepared;
    private String filename;
    private static final String SAVE_PIC_PATH = Environment.getExternalStoragePublicDirectory("").getAbsolutePath();//保存到SD卡
    public static final String SAVE_REAL_PATH = SAVE_PIC_PATH + "/LVAS";//保存的确切位置

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cache_video);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(!new File(SAVE_REAL_PATH).exists()){
            new File(SAVE_REAL_PATH).mkdir();
        }
//        SAVE_REAL_PATH =CacheVideoActivity.this.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();//保存的确切位置
        initData();
        initWindow();
        initView();
        initVolumeAndBright();
        initListener();
        initEvent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
    }

    private void initData() {
        videoName = getIntent().getStringExtra(CacheActivity.CACHE_VIDEO_URL);
        filename = getIntent().getStringExtra(CacheActivity.CACHE_VIDEO_PTAH);
    }

    private void initWindow() {
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);
        windowW = point.x;
        windowH = point.y;
    }

    private void initView() {
        surfaceView = (TextureRenderView) findViewById(R.id.activity_cache_video_trv);
        videoName_tv = (TextView) findViewById(R.id.activity_cache_video_land_tv_video_name);
        unlock_iv = (ImageView) findViewById(R.id.activity_cache_video_land_iv_unlock);

        topLayout = (LinearLayout) findViewById(R.id.activity_cache_video_land_ll_top); //顶部菜单栏
        bottomLayout = (LinearLayout) findViewById(R.id.activity_cache_video_land_ll_bottom);//底部菜单栏
        volume_ll = (LinearLayout) findViewById(R.id.activity_cache_video_volume);//音量容器
        volume_pb = (ProgressBar) findViewById(R.id.volume_pb); //音量pb
        bright_ll = (LinearLayout) findViewById(R.id.activity_cache_video_bright);//亮度容器
        bright_pb = (ProgressBar) findViewById(R.id.bright_pb); //亮度pb

        playState = (ImageView) findViewById(R.id.activity_cache_video_land_iv_play);//播放状态图标
        seekBar = (SeekBar) findViewById(R.id.activity_cache_video_land_sb); //播放进度
        playTime = (TextView) findViewById(R.id.activity_cache_video_land_tv_play_time);//播放时间
        totalTime = (TextView) findViewById(R.id.activity_cache_video_land_tv_total_time);//总时长
    }

    private void initVolumeAndBright() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);//flag 1 显示系统调节控件 0 不显示

        window = this.getWindow();
        lp = window.getAttributes();
    }

    private void initListener() {
        detector = new GestureDetector(this,new MyGestureListener());
    }

//    private static final String SAVE_PIC_PATH= Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory().getAbsolutePath() : "/mnt/sdcard";//保存到SD卡
//    public static final String SAVE_REAL_PATH = SAVE_PIC_PATH+ "/zhongjingVideo";//保存的确切位置
//    public static   String SAVE_REAL_PATH ;//=CacheVideoActivity.this.getExternalFilesDir(Environment.DIRECTORY_MOVIES).getPath();//保存的确切位置



    private void initEvent() {

        videoName_tv.setText(DateUtils.FormatDate(videoName));

        mediaPlayer=new IjkMediaPlayer();

        videoPath = new File(SAVE_REAL_PATH,filename);
        try {
            mediaPlayer.setDataSource(videoPath.getPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
        surfaceView.addRenderCallback(this);
        //mediaPlayer准备工作
        mediaPlayer.setOnPreparedListener(this);
        //MediaPlayer完成
        mediaPlayer.setOnCompletionListener(this);

        seekBar.setOnSeekBarChangeListener(this);
    }

    public void click(View view){
        switch (view.getId()){
            case R.id.activity_cache_video_land_btn_back:  //返回键
                finish();
                break;
            case R.id.activity_cache_video_land_iv_unlock: //锁屏键
                unlock = !unlock;
                if(unlock){
                    unlock_iv.setImageResource(R.drawable.land_btn_unlock);
                }else{
                    unlock_iv.setImageResource(R.drawable.land_btn_lock);
                    hide();
                }
                break;
            case R.id.activity_cache_video_land_iv_play:   //播放或暂停键
                if(playOver){
                    mediaPlayer.start();
                    playState.setImageResource(R.drawable.cache_video_play);
                    //开启更新进度
                    mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,500);
                    playOver = false;
                    break;
                }
                updatePlayState();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (playerIsPrepared){

        }
    }

    //切换播放状态
    private void updatePlayState() {
        //获取当前播放状态
        boolean playing = mediaPlayer.isPlaying();
        //修改播放状态
        if (playing) {
            //暂停
            mediaPlayer.pause();
            playState.setImageResource(R.drawable.cache_video_pause);
            //移除定时更新进度
            mHandler.removeMessages(UPDATE_PROGRESS);
        } else {
            //播放
            mediaPlayer.start();
            playState.setImageResource(R.drawable.cache_video_play);
            //开启更新进度
            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,500);
        }
    }

    /**
     * @param event
     * @return
     */
    //手动更改音量和亮度
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case MotionEvent.ACTION_MOVE:
                endX = event.getX();
                endY = event.getY();

                if(!unlock){
                    break;
                }
                float offsetX = startX - endX;
                float offsetY = startY - endY;
                float offsetPercent = offsetY / windowH;
                if(offsetPercent==0.0){
                    break;
                }
                if(Math.abs(offsetX)<Math.abs((offsetY))){ //上下滑动

                    if (event.getX() > windowW / 2) {
                        int offsetVolume = (int) (offsetPercent * maxVolume);
                        int finalVolume = startVolume + offsetVolume;
                        //更新音量
                        updateVolume(finalVolume);
                    } else{
                        //调节亮度
                        final double FLING_MIN_DISTANCE = 0.5;
                        final double FLING_MIN_VELOCITY = 60;
                        if (offsetY > FLING_MIN_DISTANCE && Math.abs(offsetY) > FLING_MIN_VELOCITY) {
                            setBrightness(10);
                        }
                        if (offsetY < FLING_MIN_DISTANCE && Math.abs(offsetY) > FLING_MIN_VELOCITY) {
                            setBrightness(-10);
                        }
                    }
                }else{ //左右滑动
                    if(endX-startX>0){ //快进
                        long pos = mediaPlayer.getCurrentPosition();
                        pos += 3000;
                        updateProgress(pos);
                        mediaPlayer.seekTo(pos);
                    }else{             //快退
                        long pos = mediaPlayer.getCurrentPosition();
                        pos -= 3000;
                        updateProgress(pos);
                        mediaPlayer.seekTo(pos);
                    }
                    break;
                }
        }
        return super.onTouchEvent(event);
    }

    //更新亮度
    public void setBrightness(float brightness) {
        Log.d("测试", "brightness=========" + brightness);
        mHandler.sendEmptyMessage(HIDE_VOLUME);
        bright_ll.setVisibility(View.VISIBLE);
        WindowManager.LayoutParams lp = getWindow().getAttributes();

        if (lp.screenBrightness == -1) {
            lp.screenBrightness = (float) 0.5;
        }

        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f;

        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0) {
            lp.screenBrightness = 0;
        }
        getWindow().setAttributes(lp);

        bright_pb.setProgress((int) (lp.screenBrightness * 255));
        mHandler.removeMessages(HIDE_BRIGHT);
        mHandler.sendEmptyMessageDelayed(HIDE_BRIGHT, 1000);
    }

    @Override
    public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
        holder.bindToMediaPlayer(mediaPlayer);
        //开启异步准备
        mediaPlayer.prepareAsync();
    }

    @Override
    public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {

    }

    @Override
    public void onPrepared(IMediaPlayer mp) {
        mp.start();
        updatePlayStateBtn();//更新播放状态按钮
        duration = mp.getDuration(); //获取视频总时长
        seekBar.setMax((int)duration);//设置进度条最大值
        totalTime.setText(TimeUtils.parseDuration((int)duration));//设置总时长
        //开始更新进度
        startUpdateProgress();
        playerIsPrepared = true;//表示播放器已经准备好了
    }

    @Override
    public void onCompletion(IMediaPlayer mp) {
        mp.seekTo(0);
        updateProgress(0);
        playState.setImageResource(R.drawable.cache_video_pause);
        playOver = true;
    }

    private boolean playOver = false;

    //根据当前播放状态设置播放状态图标
    private void updatePlayStateBtn() {
        mHandler.sendEmptyMessage(HIDE_BRIGHT);
        boolean playing = mediaPlayer.isPlaying();
        if (playing) {
            playState.setImageResource(R.drawable.cache_video_play);
            //开启更新进度
            mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,500);
        } else {
            playState.setImageResource(R.drawable.cache_video_pause);
            //移除定时更新进度
            mHandler.removeMessages(UPDATE_PROGRESS);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (!fromUser) return;
        //跳转到指定位置播放
        mediaPlayer.seekTo(progress);
        //更新进度
        updateProgress(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    class MyGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {    //单击
            if(unlock){
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

    }

    //开始更新进度
    private void startUpdateProgress() {
        //获取当前进度
        long progress = mediaPlayer.getCurrentPosition();
        //设置进度
        updateProgress(progress);
        //定时更新
        mHandler.sendEmptyMessageDelayed(UPDATE_PROGRESS,500);
    }

    //更新进度数值设置进度
    private void updateProgress(long progress) {
        playTime.setText(TimeUtils.parseDuration((int)progress));
        seekBar.setProgress((int)progress);
    }

    //更新音量
    private void updateVolume(int volume) {
        volume_ll.setVisibility(View.VISIBLE);
        float currentVolume = (float)volume/maxVolume*100;
        volume_pb.setProgress((int)currentVolume);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        mHandler.removeMessages(HIDE_VOLUME);
        mHandler.sendEmptyMessageDelayed(HIDE_VOLUME, 3*1000);
    }

    //改变系统亮度
    private int saveBright;
    public void changeAppBrightness(int brightness) {
        saveBright = brightness;
        bright_ll.setVisibility(View.VISIBLE);
        bright_pb.setProgress(brightness);
        if (brightness == -1) {
            lp.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE;
        } else {
            lp.screenBrightness = (brightness <= 0 ? 1 : brightness)/255f;
        }
        window.setAttributes(lp);
        mHandler.removeMessages(HIDE_BRIGHT);
        mHandler.sendEmptyMessageDelayed(HIDE_BRIGHT, 3*1000);
    }

    //显示或隐藏菜单栏
    private void showOrHide() {
        if(isHide){
            show();                                //显示菜单栏
            unlock_iv.setVisibility(View.VISIBLE); //显示锁屏键
        }else{
            hide();                                 //隐藏菜单栏
            unlock_iv.setVisibility(View.INVISIBLE);//隐藏锁屏键
        }
    }

    //显示菜单栏
    private void show() {
        topLayout.setVisibility(View.VISIBLE);
        bottomLayout.setVisibility(View.VISIBLE);
        isHide = false;
    }

    //隐藏菜单栏
    private void hide() {
        topLayout.setVisibility(View.INVISIBLE);
        bottomLayout.setVisibility(View.INVISIBLE);
        isHide = true;
    }
}
