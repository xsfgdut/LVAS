package zhongjing.dcyy.com.ui.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import zhongjing.dcyy.com.R;

import static zhongjing.dcyy.com.R.drawable.cache_video_pause;
import static zhongjing.dcyy.com.R.drawable.cache_video_play;

/**
 * Created by admin on 2018-01-25.
 */

public class CustomDialog extends Dialog {


    public CustomDialog(Context context) {
        super(context);

    }
    public CustomDialog(Context context, int theme) {
        super(context, theme);

    }



    public static class Builder implements  OnDismissListener,OnCancelListener,View.OnClickListener,MediaPlayer.OnCompletionListener{
        private  Context ctx;
        private   TextView tv_item_name;
        private   SeekBar seekBar;
        private   TextView textView;
        private   ImageView imageView;

        private String message;
        private View contentView;
        private String positiveButtonText;
        private String negativeButtonText;
        private String singleButtonText;
        private View.OnClickListener positiveButtonClickListener;
        private View.OnClickListener negativeButtonClickListener;
        private View.OnClickListener singleButtonClickListener;

        private View layout;
        private CustomDialog dialog;
        public Builder(Context context) {
            //这里传入自定义的style，直接影响此Dialog的显示效果。style具体实现见style.xml
            ctx=context;
            dialog = new CustomDialog(context, R.style.Dialog);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            layout = inflater.inflate(R.layout.sound_dialog, null);
            seekBar=(SeekBar)layout.findViewById(R.id.seekBar);
            imageView=(ImageView)layout.findViewById(R.id.imageView);
            imageView.setColorFilter(Color.BLACK);
            tv_item_name=(TextView)layout.findViewById(R.id.tv_item_name);
            textView=(TextView)layout.findViewById(R.id.textView);

            dialog.setContentView(layout);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;   //设置宽度充满屏幕
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(lp);
            dialog.setOnDismissListener(this);
            dialog.setOnCancelListener(this);
            imageView.setOnClickListener(this);
           // dialog.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setContentView(View v) {
            this.contentView = v;
            return this;
        }

        public Builder setPositiveButton(String positiveButtonText, View.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setNegativeButton(String negativeButtonText, View.OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setSingleButton(String singleButtonText, View.OnClickListener listener) {
            this.singleButtonText = singleButtonText;
            this.singleButtonClickListener = listener;
            return this;
        }

        MediaPlayer mp=null;
        int maxLength=0;
        public Dialog showProgressDialog(String path) {
        /* @setProgress 设置初始进度
         * @setProgressStyle 设置样式（水平进度条）
         * @setMax 设置进度最大值
         */
            File file=  new File(path);
            mp =  new MediaPlayer();
            tv_item_name.setText(file.getName());
            try {
                mp.setDataSource(ctx, Uri.fromFile(file));
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        maxLength=mp.getDuration();
                        mp.start();

                        imageView.setImageResource(cache_video_play);
                        imageView.setTag("1");
                        sendProgress();
                    }
                });
                mp.setOnCompletionListener(this);
                // prepare 通过异步的方式装载媒体资源
                mp.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
            create();
            return dialog;
        }

        /**
         * 更新进度条 进度
         */
        private  void  sendProgress(){
            int currentTime = mp.getCurrentPosition() / 1000;
            String currentStr =  currentTime / 60 +":"+ currentTime % 60;
            textView  .setText(currentStr);
            int pro= (int) ((mp.getCurrentPosition()/((float) maxLength)) * 100);
           seekBar.setProgress(pro);

            Message message = mHandle.obtainMessage();
            message.what=10001;
            mHandle.sendMessageDelayed(message,200);
        }


        private   Handler mHandle=new Handler(){
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if(null !=mp && mp.isPlaying()) {
                    sendProgress();
                }

            }
        };


        /**
         * 创建单按钮对话框
         * @return
         */
//        public CustomDialog createSingleButtonDialog() {
//            showSingleButton();
//            layout.findViewById(R.id.singleButton).setOnClickListener(singleButtonClickListener);
//            //如果传入的按钮文字为空，则使用默认的“返回”
//            if (singleButtonText != null) {
//                ((Button) layout.findViewById(R.id.singleButton)).setText(singleButtonText);
//            } else {
//                ((Button) layout.findViewById(R.id.singleButton)).setText("返回");
//            }
//            create();
//            return dialog;
//        }

        /**
         * 创建双按钮对话框
         * @return
         */
//        public CustomDialog createTwoButtonDialog() {
//            showTwoButton();
//            layout.findViewById(R.id.positiveButton).setOnClickListener(positiveButtonClickListener);
//            layout.findViewById(R.id.negativeButton).setOnClickListener(negativeButtonClickListener);
//            //如果传入的按钮文字为空，则使用默认的“是”和“否”
//            if (positiveButtonText != null) {
//                ((Button) layout.findViewById(R.id.positiveButton)).setText(positiveButtonText);
//            } else {
//                ((Button) layout.findViewById(R.id.positiveButton)).setText("是");
//            }
//            if (negativeButtonText != null) {
//                ((Button) layout.findViewById(R.id.negativeButton)).setText(negativeButtonText);
//            } else {
//                ((Button) layout.findViewById(R.id.negativeButton)).setText("否");
//            }
//            create();
//            return dialog;
//        }

        /**
         * 单按钮对话框和双按钮对话框的公共部分在这里设置
         */
        private void create() {
//            if (message != null) {      //设置提示内容
//                ((TextView) layout.findViewById(R.id.message)).setText(message);
//            } else if (contentView != null) {       //如果使用Builder的setContentview()方法传入了布局，则使用传入的布局
//                ((LinearLayout) layout.findViewById(R.id.content)).removeAllViews();
//                ((LinearLayout) layout.findViewById(R.id.content))
//                        .addView(contentView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
//            }
            dialog.setContentView(layout);
            dialog.setCancelable(true);     //用户可以点击手机Back键取消对话框显示
            dialog.setCanceledOnTouchOutside(true);        //用户不能通过点击对话框之外的地方取消对话框显示
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if(null!= mp   ) {
                if(mp.isPlaying()) {
                    mp.stop();
                }
                mp.release();
                mp=null;
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            if(null!= mp   ) {
                if(mp.isPlaying()) {
                    mp.stop();
                }
                mp.release();
                mp=null;
            }
        }

        @Override
        public void onClick(View v) {
            if("0".equals(v.getTag().toString())){ //暂停
                v.setTag("1");
                mp.start();
                imageView.setImageResource(cache_video_play );
                mHandle.sendEmptyMessage(10001);

            }else{ //播放
                v.setTag("0");
                mp.pause();
                imageView.setImageResource(R.drawable.cache_video_pause);
                mHandle.removeMessages(10001);

            }
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            mHandle.removeMessages(10001);
            imageView.setTag("0");
            imageView.setImageResource(cache_video_pause);

        }

        /**
         * 显示双按钮布局，隐藏单按钮
         */
//        private void showTwoButton() {
//            layout.findViewById(R.id.singleButtonLayout).setVisibility(View.GONE);
//            layout.findViewById(R.id.twoButtonLayout).setVisibility(View.VISIBLE);
//        }

        /**
         * 显示单按钮布局，隐藏双按钮
         */
//        private void showSingleButton() {
//            layout.findViewById(R.id.singleButtonLayout).setVisibility(View.VISIBLE);
//            layout.findViewById(R.id.twoButtonLayout).setVisibility(View.GONE);
//        }

    }
}
