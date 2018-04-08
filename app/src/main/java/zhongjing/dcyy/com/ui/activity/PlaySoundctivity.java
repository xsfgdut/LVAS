package zhongjing.dcyy.com.ui.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import zhongjing.dcyy.com.R;


public class PlaySoundctivity extends BaseActivity {

    private ImageView playState;
    private long duration;
    private TextView playTime;
    private TextView totalTime;
    private SeekBar seekbar;
    private TextView tv_play_name;

    private boolean ifplay = false;
    private MediaPlayer player = null;
    private String musicName = "blueflawer.mp3";
    private boolean iffirst = false;
    private boolean isChanging;
    private String filePath;
    private Timer mTimer;
    private TimerTask mTimerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_soundctivity);
        initView();
        player = new MediaPlayer();
        filePath= getIntent().getStringExtra(CacheActivity.CACHE_VIDEO_PTAH);
    }

    private void initView() {
        playState = (ImageView) findViewById(R.id.activity_cache_video_land_iv_play);//播放状态图标
        seekbar = (SeekBar) findViewById(R.id.activity_cache_video_land_sb); //播放进度
        playState.setTag("1");
        playTime = (TextView) findViewById(R.id.activity_cache_video_land_tv_play_time);//播放时间
        totalTime = (TextView) findViewById(R.id.activity_cache_video_land_tv_total_time);//总时长
        tv_play_name = (TextView) findViewById(R.id.activity_cache_video_land_tv_play_name);//总时长
        playState.setOnClickListener(new MyClick());
        playState.callOnClick();
    }


    class MyClick implements View.OnClickListener {
        public void onClick(View v) {
            File file = new File(Environment.getExternalStorageDirectory(),
                    musicName);
            // 判断有没有要播放的文件
            if (file.exists()) {
                switch (v.getId()) {
                    case R.id.activity_cache_video_land_iv_play:
                        if (player != null && "1".equals(  playState.getTag().toString())) {
                            playState.setImageResource(R.drawable.cache_video_pause);
                            if (!iffirst) {
                                player.reset();
                                try {
                                    player.setDataSource(filePath);
                                    player.prepare();// 准备

                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                } catch (IllegalStateException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                seekbar.setMax(player.getDuration());//设置进度条
                                //----------定时器记录播放进度---------//
                                mTimer = new Timer();
                                mTimerTask = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if(isChanging==true) {
                                            return;
                                        }
                                        seekbar.setProgress(player.getCurrentPosition());
                                    }
                                };
                                mTimer.schedule(mTimerTask, 0, 10);
                                iffirst=true;
                            }
                            player.start();// 开始
                            ifplay = true;
                        } else if (ifplay) {
                            playState.setImageResource(R.drawable.cache_video_play);
                            player.pause();
                            ifplay = false;
                        }
                        break;

                }
            }
        }
    }


    //进度条处理
    class MySeekbar implements SeekBar.OnSeekBarChangeListener {
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            isChanging=true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            player.seekTo(seekBar.getProgress());
            isChanging=false;
        }

    }
    //来电处理
    protected void onDestroy() {
        if(player != null){
            if(player.isPlaying()){
                player.stop();
            }
            player.release();
        }
        super.onDestroy();
    }

    protected void onPause() {
        if(player != null){
            if(player.isPlaying()){
                player.pause();
            }
        }
        super.onPause();
    }

    protected void onResume() {
        if(player != null){
            if(!player.isPlaying()){
                player.start();
            }
        }
        super.onResume();
    }

}
