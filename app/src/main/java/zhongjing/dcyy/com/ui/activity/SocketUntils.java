package zhongjing.dcyy.com.ui.activity;

import android.support.annotation.NonNull;
import android.util.Log;

import com.vilyever.socketclient.SocketClient;
import com.vilyever.socketclient.SocketResponsePacket;


/**
 * soucket长连接
 */
public class SocketUntils  implements  SocketClient.SocketDelegate{
     private   final static String TAG="SocketUntils";
    private static SocketUntils socketUntils;
    private  DataRevice dataRevice;
    private SocketClient socketClient;


    public void setDataRevice(DataRevice dataRevice) {
        this.dataRevice = dataRevice;
    }

    public static  SocketUntils getInstance(){
        if(socketUntils == null){
            socketUntils=new SocketUntils();
        }
        return socketUntils;
    }

    private  SocketUntils(){
        socketClient = new SocketClient(PlayActivity.ip,10025);
//        socketClient = new SocketClient(PlayActivity.ip,2001);
        socketClient.registerSocketDelegate(this);
        socketClient.setConnectionTimeout(1000 * 15); // 设置连接超时时长
        socketClient.setCharsetName("UTF-8"); // 设置发送和接收String消息的默认编码
        socketClient.connect(); // 连接，异步进行
    }


    public  void sendSocket2(String data){
        socketClient.sendString(data); // 发送String消息，使用默认编码
    }
    public  void sendSocket2(char data){
        socketClient.send(data+""); // 发送String消息，使用默认编码
    }

    @Override
    public void onConnected(SocketClient client) {
        Log.w(TAG, "onConnected:  链接成功");
    }

    @Override
    public void onDisconnected(SocketClient client) {

    }

    @Override
    public void onResponse(SocketClient client, @NonNull SocketResponsePacket responsePacket) {
        byte[] data = responsePacket.getData(); // 获取byte[]数据
        String msg = responsePacket.getMessage(); // 使用默认编码获取String消息
        Log.w(TAG, "onResponse: 收到消息："+msg);
        if(dataRevice!=null) {
            dataRevice.revivce(msg);
        }
    }

    /**
     * 收到数据
     */
    public  static interface  DataRevice{
        public void revivce(String data);
        public void error(String data);
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
