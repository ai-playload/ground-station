package java.com.example.ground_station.data.socket;

import android.util.Log;

import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.BuildConfig;

import java.com.example.ground_station.data.service.ResultCallback;
import java.com.example.ground_station.data.utils.DataUtils;
import java.com.example.ground_station.data.utils.SendUtils;
import java.com.example.ground_station.data.utils.Utils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpClientHelper {

    private final UdpClient client;

    private UdpClientHelper() {
        client = new UdpClient();
    }

    private static UdpClientHelper instance = new UdpClientHelper();

    public static UdpClientHelper getInstance() {
        return instance;
    }

    public Clien getClient() {
        return client;
    }

    public void sendUp() {

    }

    public void send(byte msgId2, int... payload) {
        client.send(SendUtils.toData(msgId2, payload));
    }
}

class UdpClient implements Clien {

    private String ip = "127.0.0.1";
    private int portJs = 13551;

    private DatagramSocket datagramSocket;
    private DatagramPacket receivePacket;

    private volatile boolean isConnected = false;
    private Thread readThread;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // 创建一个具有固定线程数的线程池;
    private boolean adsChange;
    private ConnectionCallback connectCallBack;
    private ResultCallback<byte[]> callBack;

    public UdpClient() {
    }

    @Override
    public synchronized boolean isConnected() {
        return datagramSocket != null && datagramSocket.isConnected() && this.isConnected;
    }

    public synchronized void setConnectState(boolean isConnected) {
        this.isConnected = isConnected;
    }

    @Override
    public void connect() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    if (adsChange) {
                        if (isConnected()) {
                            disConnect();
                        }
                    } else if (isConnected()) {

                        return;
                    }
                    datagramSocket = new DatagramSocket(portJs);
//                    datagramSocket.setSoTimeout(3000);
                    InetAddress serverAddress = InetAddress.getByName(ip);
                    datagramSocket.connect(serverAddress, portJs + 1);

                    byte[] buffer = new byte[256];
                    receivePacket = new DatagramPacket(buffer, buffer.length);

                    setConnectState(true);

                    if (readThread != null) {
                        readThread.interrupt();
                    }
                    readThread = new Thread(new ReadThread());
                    readThread.start();

                    if (connectCallBack != null) {
                        connectCallBack.onConnectionSuccess();
                    }
                    System.out.println("udp连接成功，监听端口：" + portJs);
                } catch (Exception e) {
                    e.printStackTrace();
                    setConnectState(false);
                    if (connectCallBack != null) {
                        connectCallBack.onConnectionFailure(e);
                    }
                    System.err.println("udp连接失败: " + e.getMessage());
                }
            }
        });
    }

    @Override
    public void send(byte[] data) {
        executorService.execute(new Runnable() {

            @Override
            public void run() {
                if (datagramSocket != null && isConnected()) {
                    try {
                        DatagramPacket packet = new DatagramPacket(data, data.length);
                        packet.setData(data);
                        datagramSocket.send(packet);
                        System.out.println("udp数据已发送：" + bytesToHex(data));
                        if (BuildConfig.DEBUG) {
                            ToastUtils.showShort("udp 成功发送指令：" + data[3]);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.println("udp发送失败: " + e.getMessage());
                        if (BuildConfig.DEBUG) {
                            ToastUtils.showShort("udp 发送失败- 指令：" + data[3]);
                        }
                    }
                }
            }
        });
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    @Override
    public void update(String ip, String port) {
        update(ip, Integer.parseInt(port));
    }

    @Override
    public void update(String ip, int port) {
        adsChange = !Objects.equals(this.ip, ip) || this.portJs != port;
        this.ip = ip;
        this.portJs = port;
        adsChange = true;
        if (adsChange || !isConnected()) {
            connect();
        }
    }

    @Override
    public synchronized void disConnect() {
        if (Utils.isMainTread()) {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    runDiSconnect();
                }
            });
        } else {
            runDiSconnect();
        }
    }

    private void runDiSconnect() {
        if (readThread != null && !readThread.isInterrupted()) {
            readThread.interrupt();
        }
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
        }
        setConnectState(false);
        System.out.println("连接已关闭");
    }

    @Override
    public void setConnectCallBack(ConnectionCallback connectCallBack) {
        this.connectCallBack = connectCallBack;
    }

    @Override
    public void setCallBack(ResultCallback<byte[]> callBack) {
        this.callBack = callBack;
    }

    class ReadThread implements Runnable {
        @Override
        public void run() {
            try {
                while (isConnected() && readThread != null && !readThread.isInterrupted()) {
                    try {
                        Log.e("udp接收数据开始", " 当前线程id:" + Thread.currentThread().getId());
                        datagramSocket.receive(receivePacket);

                        byte[] data = receivePacket.getData();

                        byte[] bytes = Utils.subByte(data, receivePacket.getOffset(), receivePacket.getLength());

                        DataUtils.parse(bytes, callBack);
                        String receivedData = bytesToHex(data);
                        Log.e("udp接收数据：", receivedData);
                    } catch (IOException e) {
                        if (isConnected()) {
                            e.printStackTrace();
                            Log.e("udp读取数据失败IOException: " , e.getMessage());
                        }else {
                            Log.e("udp读取数据失败IOException 非连接上: " , e.getMessage());
                        }
                    }
                }
            }catch (Exception e) {
                Log.e("udp接收数据 Exception", e.getMessage());
            }
        }
    }
}
