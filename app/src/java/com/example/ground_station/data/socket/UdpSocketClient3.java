package java.com.example.ground_station.data.socket;

import android.serialport.SerialPort;

import java.com.example.ground_station.data.service.ResultCallBack;
import java.com.example.ground_station.data.utils.Utils;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class UdpSocketClient3 {

    private String ip = "127.0.0.1";
    private int portJs = 13551;
    private volatile boolean isConnected = false;
    private Thread readThread;
    private ResultCallBack<List<byte[]>> callBack;


    private SerialPort mSerialPort;
    private InputStream mInputStream;
    private OutputStream mOutputStream;


    private UdpSocketClient3() {
    }

    private static UdpSocketClient3 instance = new UdpSocketClient3();

    public static synchronized UdpSocketClient3 getInstance() {
        return instance;
    }

    // 连接到服务器
    public void connect(String ip, int port, ConnectionCallback callback) {
        this.ip = ip;
        this.portJs = port;
        try {

            if (isConnected) {
                disconnect();
            }

            mSerialPort = SerialPort.newBuilder(new File(ip), portJs)                    .build();
//                    mSerialPort = new SerialPort(new File(DEVICE_NAME), BAUD_RATE, 0);
            mInputStream = mSerialPort.getInputStream();
            mOutputStream = mSerialPort.getOutputStream();

            isConnected = true;

            if (readThread != null) {
                readThread.interrupt();
            }
            readThread = new Thread(new ReadThread());
            readThread.start();

            if (callback != null) {
                callback.onConnectionSuccess();
            }
            System.out.println("连接成功，监听端口：" + portJs);
        } catch (Exception e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onConnectionFailure(e);
            }
            System.err.println("连接失败: " + e.getMessage());
        }
    }


    public void sendData(byte[] data) {
        sendCommand(data);
    }

    // 发送数据到串口
    private void sendCommand(byte[] bytes) {
        try {
            mOutputStream.write(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    public void disconnect() {
        isConnected = false;
        if (readThread != null) {
            readThread.interrupt();
        }
        if (mSerialPort != null) {
            try {
                mSerialPort.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("连接已关闭");
    }

    private List list = new ArrayList<Byte[]>();

    private class ReadThread implements Runnable {
        @Override
        public void run() {
            try {
                byte[] data = new byte[1024];
                while (isConnected && readThread != null) {
                    // 处理接收到的数据
                    int size = mInputStream.read(data);

                    if (size > 0) {
                        byte[] bytes = Utils.subByte(data, 0, size);
                        list.clear();
                        int length = bytes.length;
                        for (int i = 0; i < length; i++) {
                            byte v = bytes[i];
                            if (v == SocketConstant.HEADER) {
                                if (i + 2 < length) {
                                    byte dataSize = bytes[i + 1];
                                    int sumSize = dataSize + 3;

                                    if (sumSize + i <= length) {
                                        byte[] temp = new byte[sumSize];
                                        for (int n = 0; n < sumSize; n++) {
                                            temp[n] = bytes[i + n];
                                        }
                                        list.add(temp);
                                    }
                                }
                            }
                        }
                        if (list.size() > 0 && callBack != null) {
                            callBack.result(list);
                        }
                    }
//                    System.arraycopy(data,0,tempArray,0,it.length)
//                    Log.e("ReadThread","receive：${String2ByteArrayUtils.bytes2Hex(tempArray)}");
//                    String receivedData = bytesToHex(data);
//                    System.out.println("收到的数据：" + receivedData);
                }
            } catch (IOException e) {
                if (isConnected) {
                    e.printStackTrace();
                    System.err.println("读取数据失败: " + e.getMessage());
                }
            }
        }
    }

    public void setCallBack(ResultCallBack<List<byte[]>> callBack) {
        this.callBack = callBack;
    }
}