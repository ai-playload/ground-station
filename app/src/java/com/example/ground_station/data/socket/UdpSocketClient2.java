package java.com.example.ground_station.data.socket;

import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.BuildConfig;

import java.com.example.ground_station.data.service.ResultCallBack;
import java.com.example.ground_station.data.utils.Utils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class UdpSocketClient2 {

    private String ip = "127.0.0.1";
    private int portJs = 13551;
    private int portSend = 13552;
    private DatagramSocket datagramSocket;
    private DatagramPacket receivePacket;
    private volatile boolean isConnected = false;
    private Thread readThread;
    private ResultCallBack<List<byte[]>> callBack;

//    public UdpSocketClient2(String ip, int prot) {
//        this.ip = ip;
//        this.portJs = prot;
//    }

    private UdpSocketClient2() {
    }

    private static UdpSocketClient2 instance = new UdpSocketClient2();

    public static synchronized UdpSocketClient2 getInstance() {
        return instance;
    }

    // 连接到服务器
    public void connect(String ip, int port, ConnectionCallback callback) {
        this.ip = ip;
        this.portJs = port;
        try {
            datagramSocket = new DatagramSocket(portJs);
            InetAddress serverAddress = InetAddress.getByName(ip);
            datagramSocket.connect(serverAddress, portJs + 1);
            isConnected = true;

            byte[] buffer = new byte[256];
            receivePacket = new DatagramPacket(buffer, buffer.length);

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

    public DatagramSocket getDatagramSocket() {
        return datagramSocket;
    }

    public void sendData(byte[] data) {
        if (datagramSocket != null && isConnected) {
            try {
                DatagramPacket packet = new DatagramPacket(data, data.length);
                packet.setData(data);
                datagramSocket.send(packet);
                System.out.println("数据已发送：" + bytesToHex(data));
                if (BuildConfig.DEBUG) {
                    ToastUtils.showShort("udp 成功发送指令：" + data[3] );
                }
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("发送失败: " + e.getMessage());
                if (BuildConfig.DEBUG) {
                    ToastUtils.showShort("udp 发送失败- 指令：" + data[3] );
                }
            }
        } else {
            System.err.println("未连接到服务器，无法发送数据");
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
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
        System.out.println("连接已关闭");
    }

    private List list = new ArrayList<Byte[]>();

    private class ReadThread implements Runnable {
        @Override
        public void run() {
            try {
                while (isConnected && readThread != null && !readThread.isInterrupted()) {
                    datagramSocket.receive(receivePacket);

                    byte[] data = receivePacket.getData();

                    byte[] bytes = Utils.subByte(data, receivePacket.getOffset(), receivePacket.getLength());

                    if (bytes != null && bytes.length > 0) {
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