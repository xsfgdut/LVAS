package zhongjing.dcyy.com.utils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemClock;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/12/15.
 */

public class WifiManagerUtils {
    public WifiManager mWifiManager;
    public WifiManager.WifiLock mWifiLock;
    public WifiManagerUtils(Context context) {
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }
    //检测wifi状态  WIFI_STATE_DISABLED  0 正在关闭 WIFI_STATE_DISABLED 1 已经关闭
    //WIFI_STATE_ENABLING 2 正在打开 WIFI_STATE_ENABLED 3 已经打开  WIFI_STATE_UNKNOWN 4 未知
    public int checkWifiState(){
        return mWifiManager.getWifiState();
    }
    //打开wifi
    public   void openWifiEnabled(){
       if (mWifiManager != null){
           mWifiManager.setWifiEnabled(true);
       }
    }
    //关闭wifi
    public void colseWifiEnable(){
        if (mWifiManager != null){
            mWifiManager.setWifiEnabled(false);
        }
    }
    //获取一个WIFILock
    public void crateWifiLock(){
        if (mWifiManager != null){
          mWifiLock = mWifiManager.createWifiLock("Test");
        }
    }
    //锁定wifi
    public void lockWofi(){
        mWifiLock.acquire();
    }
    //释放wifiLock
    public void releaseWifiLock(){
        if (mWifiLock.isHeld()){
            mWifiLock.release();
        }
    }
    //获取扫描结果
    public List<ScanResult> getScanResult(){
        List<ScanResult> results = new ArrayList<>();
        if (mWifiManager != null){
            mWifiManager.startScan();
            SystemClock.sleep(1000);
            results = mWifiManager.getScanResults();
        }
            return results;
    }
    //获取已经配置好的网络连接
    public List<WifiConfiguration> getConfigWifiInfo(){
        List<WifiConfiguration> configInfo = new ArrayList<>();
        if (mWifiManager != null){
            mWifiManager.startScan();
            SystemClock.sleep(1000);
            configInfo = mWifiManager.getConfiguredNetworks();
        }
        return configInfo;
    }
    //获取当前连接的wifi
    public WifiInfo getNetWorkId(){
        return (mWifiManager == null) ? null: mWifiManager.getConnectionInfo();
    }
    //断开指定的wifi
    public void disconnectWifi( int netid){
        if (mWifiManager != null){
            mWifiManager.disableNetwork(netid);
            mWifiManager.disconnect();
        }
    }
    //添加一个网络连接
    public boolean addNetWork(WifiConfiguration wcg){
        int wcgId = mWifiManager.addNetwork(wcg);
        return mWifiManager.enableNetwork(wcgId,true);
    }
    //连接一个wifi
    public WifiConfiguration createWifiInfo(String ssid,String password,int type){
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.allowedAuthAlgorithms.clear();
        wifiConfiguration.allowedPairwiseCiphers.clear();
        wifiConfiguration.allowedKeyManagement.clear();
        wifiConfiguration.allowedProtocols.clear();
        wifiConfiguration.allowedGroupCiphers.clear();
        wifiConfiguration.SSID = "\""+ssid+"\"";
        //避免田间重复的SSID
        WifiConfiguration tempConfig =  isExsitsWifi(ssid);
        if (tempConfig != null){
            mWifiManager.removeNetwork(tempConfig.networkId);
        }
        //wifi没有密码的
        if (type == 1){
            wifiConfiguration.wepKeys[0] = "";
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.wepTxKeyIndex = 0;
        }
        //采用wep加密
        if (type == 2){
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.wepKeys[0] = "\""+password+"\"";
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            wifiConfiguration.wepTxKeyIndex = 0;
        }
        if (type == 3){
            wifiConfiguration.preSharedKey = "\""+password+"\"";
            wifiConfiguration.hiddenSSID = true;
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
            wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
        }
        return wifiConfiguration;
    }

    public WifiConfiguration isExsitsWifi(String ssid){
        if (mWifiManager == null){
            return null;
        }
        List<WifiConfiguration> lists = mWifiManager.getConfiguredNetworks();
        for (WifiConfiguration wcf : lists){
            if (wcf.SSID.equals(ssid)){
                return wcf;
            }
        }
        return  null;
    }
}
