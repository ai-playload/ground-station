package java.com.example.ground_station.data.socket;

import com.blankj.utilcode.util.ToastUtils;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSocketClient2 {

    private String ip = "127.0.0.1";
    private int portJs = 13551;
    private int portSend = 13552;
    private DatagramSocket datagramSocket;
    private DatagramPacket receivePacket;
    private volatile boolean isConnected = false;
    private Thread readThread;

    public UdpSocketClient2(String ip, int prot) {
        this.ip = ip;
        this.portJs = prot;
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
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("发送失败: " + e.getMessage());
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

    private class ReadThread implements Runnable {
        @Override
        public void run() {
            try {
                byte[] buffer = new byte[256];
                receivePacket = new DatagramPacket(buffer, buffer.length);

                while (isConnected) {
                    datagramSocket.receive(receivePacket);
                    String receivedData = bytesToHex(receivePacket.getData());
                    System.out.println("收到的数据：" + receivedData);
                }
            } catch (IOException e) {
                if (isConnected) {
                    e.printStackTrace();
                    System.err.println("读取数据失败: " + e.getMessage());
                }
            }
        }
    }
}