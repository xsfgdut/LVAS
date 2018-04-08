package zhongjing.dcyy.com.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.vilyever.socketclient.SocketClient;

import cn.qqtheme.framework.picker.OptionPicker;
import cn.qqtheme.framework.widget.WheelView;
import okhttp3.Call;
import zhongjing.dcyy.com.R;
import zhongjing.dcyy.com.utils.SPUtils;
import zhongjing.dcyy.com.utils.ToastUtil;

public class SettingsActivity extends BaseActivity {

    private static final int REQUEST_NAME = 100;
    private static final int REQUEST_PSW = 200;
    private static final int REQUEST_RESET = 300;
    private static final int REQUEST_CHA = 400;
    private static final int REQUEST_GET_CHANNEL = 500;
    private AlertDialog dialogName;
    private AlertDialog dialogPsw;
    private AlertDialog dialogReset;
    private Button btn_save;
    private CheckedTextView checkedTextView;
    private String frameportheader=""; //串口帧头
    private EditText txt_wifiName,txt_wifipassword,txt_wifi_port;
    private CheckedTextView txt_check_mp3,txt_check_wav;
    private  SocketUntils   socketUntils =SocketUntils.getInstance();
    private CheckBox chk_send_hex;
    private EditText txt_sendDate;
    private EditText chk_recice_data;
    private CheckBox chk_recice_hex;
    private Button btn_send;
    private SocketClient socketClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        btn_save = (Button) findViewById(R.id.btn_save);
        txt_wifiName =(EditText) findViewById(R.id.txt_wifiName);
        txt_wifipassword =  (EditText)findViewById(R.id.txt_wifipassword);
        txt_wifi_port = (EditText)findViewById(R.id.txt_wifi_port);

        txt_check_mp3 = (CheckedTextView) findViewById(R.id.txt_check_mp3);
        txt_check_wav = (CheckedTextView) findViewById(R.id.txt_check_wav);

        txt_wifi_port.setText(  (String)SPUtils.get(this,SPUtils.FRAMEPORTHEADER,"5AA5"));
        if( "mp3".equals((String) SPUtils.get(this,SPUtils.SOUND_FILE_TYPE,"mp3"))){
            txt_check_mp3.callOnClick();
        }else{
            txt_check_wav.callOnClick();
        }

        txt_sendDate = (EditText) findViewById(R.id.txt_sendDate);
        btn_send = (Button) findViewById(R.id.btn_send);
        chk_send_hex = (CheckBox) findViewById(R.id.chk_send_hex);
        chk_send_hex.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(txt_sendDate.getText().toString().trim())) {
                    if (chk_send_hex.isChecked()) {
                        txt_sendDate.setTag( ""+ txt_sendDate.getText());
                        txt_sendDate.setText(SocketUntils.getInstance().bytesToHexString(txt_sendDate.getText()
                                .toString().getBytes()));
                    } else {
                        txt_sendDate.setText( ""+ txt_sendDate.getTag());
                    }
                }
            }
        });

        chk_recice_data = (EditText) findViewById(R.id.chk_recice_data);
        chk_recice_hex = (CheckBox) findViewById(R.id.chk_recice_hex);
        chk_recice_hex.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(chk_recice_data.getText().toString().trim())) {
                    if (chk_recice_hex.isChecked()) {
                        chk_recice_data.setTag( ""+ chk_recice_data.getText());
                        chk_recice_data.setText(SocketUntils.getInstance().bytesToHexString(chk_recice_data.getText()
                                .toString().getBytes()));
                    } else {
                        chk_recice_data.setText( ""+ chk_recice_data.getTag());
                    }
                }
            }
        });




        btn_send.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!TextUtils.isEmpty(txt_sendDate.getText())) {
                    socketUntils.sendSocket2(txt_sendDate.getText().toString());
                }
            }
        });

        socketUntils.setDataRevice(new SocketUntils.DataRevice() {
            @Override
            public void revivce(String data) {
                if(!TextUtils.isEmpty(data)) {
                    if (chk_recice_hex.isChecked()) {
                        chk_recice_data.setTag( data);
                        chk_recice_data.setText( SocketUntils.getInstance().bytesToHexString(data.getBytes()).toString() );
                    } else {
                        chk_recice_data.setText(data);
                    }
                }
            }
            @Override
            public void error(String data) {

            }
        });

    }



    public void click(View view) {
        switch (view.getId()) {
            case R.id.activity_settings_back:            //返回键
                finish();
                break;
            case R.id.activity_settings_changeChannel://切换信道
                changeChannel();
                break;
            case R.id.btn_save:{
               String  wifiName=  txt_wifiName.getText().toString();
               String  wifiPassword=  txt_wifipassword.getText().toString();
               String  wifiPort=  txt_wifi_port.getText().toString();
                if(txt_wifi_port.getText().length()>4){
                    ToastUtil.showToast(this,"串口帧头长度不能超过4位!");
                }
                if(!TextUtils.isEmpty(wifiName)){
                    OkHttpUtils_get("http://"+PlayActivity.ip+"/api/setssid?ssid=" + wifiName, REQUEST_NAME);
                }
                if(!TextUtils.isEmpty(wifiPassword)){
                    OkHttpUtils_get("http://"+PlayActivity.ip+"/api/setpasswd?pswd=" + wifiPassword, REQUEST_PSW);
                }
                if(!TextUtils.isEmpty(wifiPort)){
                    SPUtils.put(this,SPUtils.FRAMEPORTHEADER,txt_wifi_port.getText().toString());
//                    OkHttpUtils_get("http://192.168.11.123/api/setpasswd?pswd=" + wifiPort, REQUEST_PSW);
                }
            }break;
            case R.id.txt_check_mp3:{  //mp3
                  if(checkedTextView  !=null &&   checkedTextView.getId()  != view.getId()){
                    checkedTextView.setChecked(false);
                  }
                ((CheckedTextView)view).setChecked(true);
                 checkedTextView = (CheckedTextView) view;
                 SPUtils.put(this,SPUtils.SOUND_FILE_TYPE,"mp3");
            }break;
            case R.id.txt_check_wav:{ //wav
                if(checkedTextView  !=null &&   checkedTextView.getId()  != view.getId()){
                    checkedTextView.setChecked(false);
                }
                ((CheckedTextView)view).setChecked(true);
                checkedTextView = (CheckedTextView) view;
                SPUtils.put(this,SPUtils.SOUND_FILE_TYPE,"wav");
            }break;
/*            case R.id.activity_settings_getChannel:
                getChannel();
                break;*/
//            case R.id.activity_settings_changeWiFiName:  //更改WiFi名称
//                View nameInflate = View.inflate(this, R.layout.name_dialog, null);
//
//                AlertDialog.Builder builderName = new AlertDialog.Builder(this);
//                dialogName = builderName.setView(nameInflate).create();
//                dialogName.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//                dialogName.show();
//
//                final EditText name_et = (EditText) nameInflate.findViewById(R.id.name_dialog_et);
//                Button name_bt_cancel = (Button) nameInflate.findViewById(R.id.name_dialog_cancel);
//                Button name_bt_sure = (Button) nameInflate.findViewById(R.id.name_dialog_sure);
//                name_bt_cancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialogName.dismiss();
//                    }
//                });
//
//                name_bt_sure.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String wifiName = "ZJBOX-" + name_et.getText().toString();
//                        if (TextUtils.isEmpty(wifiName)) {
//                            Toast.makeText(SettingsActivity.this, R.string.wifi_name, Toast.LENGTH_SHORT).show();
//                            dialogName.dismiss();
//                        } else {
//                            OkHttpUtils_get("http://192.168.11.123/api/setssid?ssid=" + wifiName, REQUEST_NAME);
//                            dialogName.dismiss();
//                        }
//
//                    }
//
//                });

//                break;
//            case R.id.activity_settings_changeWiFiPsw:   //更改WiFi密码
//                View pswInflate = View.inflate(this, R.layout.psw_dialog, null);
//
//                AlertDialog.Builder builderPsw = new AlertDialog.Builder(this);
//                dialogPsw = builderPsw.setView(pswInflate).create();
//                dialogPsw.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
//                dialogPsw.show();
//
//                final EditText psw_et = (EditText) pswInflate.findViewById(R.id.psw_dialog_et);
//                Button psw_bt_cancel = (Button) pswInflate.findViewById(R.id.psw_dialog_cancel);
//                Button psw_bt_sure = (Button) pswInflate.findViewById(R.id.psw_dialog_sure);
//                psw_bt_cancel.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        dialogPsw.dismiss();
//                    }
//                });
//
//                psw_bt_sure.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        String wifiPsw = psw_et.getText().toString();
//                        if (TextUtils.isEmpty(wifiPsw)) {
//                            Toast.makeText(SettingsActivity.this, R.string.wifi_psw, Toast.LENGTH_SHORT).show();
//                            dialogPsw.dismiss();
//                        } else {
//                            OkHttpUtils_get("http://192.168.11.123/api/setpasswd?pswd=" + wifiPsw, REQUEST_PSW);
//                            dialogPsw.dismiss();
//                        }
//                    }
//                });
//                break;
            case R.id.activity_settings_reset:           //恢复出厂设置
                View resetInflate = View.inflate(this, R.layout.reset_dialog, null);

                AlertDialog.Builder builderReset = new AlertDialog.Builder(this);
                dialogReset = builderReset.setView(resetInflate).create();
                dialogReset.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                dialogReset.show();

                Button reset_bt_cancel = (Button) resetInflate.findViewById(R.id.reset_dialog_cancel);
                Button reset_bt_sure = (Button) resetInflate.findViewById(R.id.reset_dialog_sure);
                reset_bt_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialogReset.dismiss();
                    }
                });

                reset_bt_sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        OkHttpUtils_get("http://192.168.11.123/api/reconfig", REQUEST_RESET);
                        dialogReset.dismiss();
                    }
                });
                break;
        }
    }

    private String parseJson(String wifiNameResult) {
        String code = wifiNameResult.substring(127, 128);
        return code;

    }

    private void changeChannel() {
        OptionPicker pickerSex = new OptionPicker(this, channelList);
        pickerSex.setCycleDisable(true);//禁用循环
        pickerSex.setTopHeight(50);//顶部标题栏高度
        pickerSex.setTopLineColor(0xFF33B5E5);//顶部标题栏下划线颜色
        pickerSex.setTopLineHeight(1);//顶部标题栏下划线高度
        pickerSex.setTitleText(R.string.chosechn);
        pickerSex.setTitleTextColor(0xFF999999);//顶部标题颜色
        pickerSex.setTitleTextSize(12);//顶部标题文字大小
        pickerSex.setCancelText(R.string.cancel);
        pickerSex.setSubmitText(R.string.sure);

        pickerSex.setCancelTextColor(0xFF33B5E5);//顶部取消按钮文字颜色
        pickerSex.setCancelTextSize(14);
        pickerSex.setSubmitTextColor(0xFF33B5E5);//顶部确定按钮文字颜色
        pickerSex.setSubmitTextSize(14);
        pickerSex.setTextColor(0xFFEE0000, 0xFF999999);//中间滚动项文字颜色
        WheelView.LineConfig config = new WheelView.LineConfig();
        pickerSex.setSelectedIndex(0);//默认选中项
        config.setColor(0xFFEE0000);//线颜色
        config.setAlpha(140);//线透明度
        pickerSex.setLineConfig(config);
        pickerSex.setBackgroundColor(0xFFE1E1E1);
        pickerSex.setOnOptionPickListener(new OptionPicker.OnOptionPickListener() {
            @Override
            public void onOptionPicked(int index, String item) {
                OkHttpUtils_get("http://"+PlayActivity.ip+"/api/setchn?chn=" + item, REQUEST_CHA);
            }
        });
        pickerSex.show();
    }

    private void getChannel() {
        OkHttpUtils_get("http://"+PlayActivity.ip+"/api/getchn", REQUEST_GET_CHANNEL);
    }

    String[] channelList = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10",
            "11"};

    @Override
    protected void onError_get(Call call, Exception e, int id, int type) {
        switch (type) {
            case REQUEST_NAME:
            case REQUEST_PSW:
            case REQUEST_RESET:
                Toast.makeText(SettingsActivity.this, R.string.set_error, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    protected void onResponse_get(String response, int id, int type) {
        String code = parseJson(response);
        switch (type) {
            case REQUEST_NAME:
                if (code.equals("0")) {
                    Toast.makeText(SettingsActivity.this, R.string.set_name_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.set_name_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_PSW:
                if (code.equals("0")) {
                    Toast.makeText(SettingsActivity.this, R.string.set_psw_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.set_psw_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_RESET:
                if (code.equals("0")) {
                    Toast.makeText(SettingsActivity.this, R.string.reset_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.reset_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CHA:
                if (code.equals("0")) {
                    Toast.makeText(SettingsActivity.this, R.string.setchannel_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.setchannel_fail, Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_GET_CHANNEL:
                if (code.equals("0")) {
                    Toast.makeText(SettingsActivity.this, getString(R.string.getchannel_success) + getCurrentChannel(response), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SettingsActivity.this, R.string.getchannel_fail, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private String getCurrentChannel(String response) {
        String code = response.substring(138, 139);
        String code2 = response.substring(139, 140);
        if (!code2.equals("\""))
            code = code + code2;
        return code;
    }

}
