package com.skyworth.splicing;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 串口操作
 * 
 * @author guoxiao
 * 
 */
public class SerialPortUtil {
	private String TAG = SerialPortUtil.class.getSimpleName();
	private SerialPort mSerialPort;
	private OutputStream mOutputStream;
	private InputStream mInputStream;
	private ReadThread mReadThread;
//	private String path = "/dev/ttyS3";
	private String path = "2001";
	private int baudrate = 115200;
	private static SerialPortUtil portUtil;
	private OnDataReceiveListener onDataReceiveListener = null;
	private boolean isStop = false;

	public interface OnDataReceiveListener {
		public void onDataReceive(byte[] buffer, int size);
	}

	public void setOnDataReceiveListener(
			OnDataReceiveListener dataReceiveListener) {
		onDataReceiveListener = dataReceiveListener;
	}
	
	public static SerialPortUtil getInstance() {
		if (null == portUtil) {
			portUtil = new SerialPortUtil();
			portUtil.onCreate();
		}
		return portUtil;
	}

	/**
	 * 初始化串口信�?
	 */
	private void onCreate() {
		try {
			mSerialPort = new SerialPort(new File(path), baudrate);
			mOutputStream = mSerialPort.getOutputStream();
			mInputStream = mSerialPort.getInputStream();
			
			mReadThread = new ReadThread();
			isStop = false;
			mReadThread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发�?指令到串�?
	 * 
	 * @param cmd
	 * @return
	 */
	public boolean sendCmds(String cmd) {
		boolean result = true;
		byte[] mBuffer = cmd.getBytes();
		try {
			if (mOutputStream != null) {
				mOutputStream.write(mBuffer);
			} else {
				result = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean sendBuffer(byte[] mBuffer) {
		boolean result = true;
		String tail = "";
		byte[] tailBuffer = tail.getBytes();
		byte[] mBufferTemp = new byte[mBuffer.length+tailBuffer.length];
		System.arraycopy(mBuffer, 0, mBufferTemp, 0, mBuffer.length);
		System.arraycopy(tailBuffer, 0, mBufferTemp, mBuffer.length, tailBuffer.length);
		try {
			if (mOutputStream != null) {
				mOutputStream.write(mBufferTemp);
			} else {
				result = false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	private class ReadThread extends Thread {

		@Override
		public void run() {
			super.run();
			while (!isStop && !isInterrupted()) {
				int size;
				try {
					if (mInputStream == null)
						return;
					byte[] buffer = new byte[512];
					size = mInputStream.read(buffer);
					if (size > 0) {
//							String str = new String(buffer, 0, size);
//							Logger.d("length is:"+size+",data is:"+new String(buffer, 0, size));
						if (null != onDataReceiveListener) {
							onDataReceiveListener.onDataReceive(buffer, size);
						}
					}
					Thread.sleep(10);
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	/**
	 * 关闭串口
	 */
	public void closeSerialPort() {
		isStop = true;
		if (mReadThread != null) {
			mReadThread.interrupt();
		}
		if (mSerialPort != null) {
			mSerialPort.close();
		}
	}
	
}
