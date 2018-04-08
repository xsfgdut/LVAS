package zhongjing.dcyy.com.ui.view;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import zhongjing.dcyy.com.R;
import zhongjing.dcyy.com.ui.activity.PlayActivity;
import zhongjing.dcyy.com.utils.DensityUtils;
import zhongjing.dcyy.com.utils.SPUtils;

import static zhongjing.dcyy.com.utils.SPUtils.get;

/**
 * Created by admin on 2017-12-30.
 */

public class Popwind extends PopupWindow {
    private  Context ctx;
    private SeekBar progressBar;
    private String unti="";
    private TextView textView;
    private TextView tv_item_name;
    private int itemType;

    /**
     *
     * @param context
     * @param name
     * @param itemType  1 音量  2 亮度 3 降噪
     */
    public Popwind(Context context,String  name,int itemType) {
      super(context, null);
        this.ctx=context;
        this.itemType=itemType;
        initview(name);

    }
    public void setUnti(String unti){
       this. unti=unti;
    }

    private void initview(String name) {

        LayoutInflater inflater = (LayoutInflater)this.ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View contentview = inflater.inflate(R.layout.popup_process, null);
//        contentview.setFocusable(true); // 这个很重要
       // contentview.setFocusableInTouchMode(true);

        progressBar= (SeekBar) contentview.findViewById(R.id.bright_pb_land);
        tv_item_name= (TextView) contentview.findViewById(R.id.tv_item_name);
        textView= (TextView) contentview.findViewById(R.id.tv_crtctrl_num);
        tv_item_name.setText(name);
        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                textView.setText(""+progress);

                String head = (String) SPUtils.get(ctx, SPUtils.FRAMEPORTHEADER,"");
                String cmds="";
               String prog=  progress >9? progress+"":"0"+progress;
            //    Log.e(Popwind.class.getSimpleName(),itemType + "  onProgressChanged: "+prog );
                if(itemType ==1) { //音量
                    cmds= head +"8200"+prog;
                    SPUtils.put(ctx,SPUtils.SET_TYPE_VOL,progress);
                }else     if(itemType ==2) { //亮度
                    cmds= head +"8240"+prog;
                    SPUtils.put(ctx,SPUtils.SET_TYPE_LIGHT,progress);
                }else if(itemType ==3) { //降噪
                    cmds= head +"8220"+prog;
                    SPUtils.put(ctx,SPUtils.SET_TYPE_NOISE,progress);
                }

//                SerialPortUtil.getInstance().sendCmds(cmds);
                ((PlayActivity)ctx).socketUntils.sendSocket2( bytesToHexString(cmds.getBytes()));


            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) { }
        });

        setContentView(contentview);
         setFocusable(true);
        // setWidth(DensityUtils.dp2px(ctx,400));
        setOutsideTouchable(true);
//        contentview.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event) {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                    dismiss();
//                    return true;
//                }
//                return false;
//            }
//        });

        String val="50";
        if(itemType ==1) { //音量
            val="" +( SPUtils.get(ctx,SPUtils.SET_TYPE_VOL,50));
        }else     if(itemType ==2) { //亮度

            val="" +(SPUtils.get(ctx,SPUtils.SET_TYPE_LIGHT,50));
        }else if(itemType ==3) { //降噪
            val="" +( SPUtils.get(ctx,SPUtils.SET_TYPE_NOISE,50));
        }
        try{
            Log.e("d", "initview: "+val );

            progressBar.setProgress(Integer.parseInt(val));
        }catch (Exception ex){
        }
        readValue();
    }

    public void setMax(int  max){
        progressBar.setMax(max);
    }

    public  void show(View view){

        showAtLocation(view,  Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 0, DensityUtils.dp2px(ctx,40));


    }
    private  void readValue(){
        String head = (String) get(ctx, SPUtils.FRAMEPORTHEADER,"");
        String cmds="";
        if(itemType ==1) { //音量
            cmds= head +"830000";
        }else if(itemType ==2) { //亮度
            cmds= head +"834000";
        }else if(itemType ==3) { //降噪
            cmds= head +"832000";
        }

        ((PlayActivity)ctx).socketUntils.sendSocket2( bytesToHexString(cmds.getBytes()));
//        SerialPortUtil.getInstance().sendCmds(cmds);

    }

    /**
     * 读取到的值
     * @param re
     */
    public void setValue(String re) {
        if (re.indexOf("81") > -1) { //发送成功
            String type = re.substring(7, 9);
            String value = re.substring(9);

            if ("00".equals(type)) {  //音量
                SPUtils.put(ctx, SPUtils.SET_TYPE_VOL, Integer.parseInt(value));
                progressBar.setProgress(Integer.parseInt(value));
            } else if ("20".equals(type)) { //降噪
                SPUtils.put(ctx, SPUtils.SET_TYPE_NOISE, Integer.parseInt(value));
                progressBar.setProgress(Integer.parseInt(value));
            } else if ("40".equals(type)) { //亮度
                SPUtils.put(ctx, SPUtils.SET_TYPE_LIGHT, Integer.parseInt(value));
                progressBar.setProgress(Integer.parseInt(value));
            }
        }
    }

    public   String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }


}
