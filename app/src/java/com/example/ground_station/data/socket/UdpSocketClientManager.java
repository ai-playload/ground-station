package java.com.example.ground_station.data.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.iflytek.aikitdemo.tool.SPUtil;

import java.com.example.ground_station.presentation.util.SendUtils;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpSocketClientManager {
    private ExecutorService executorService;
    private UdpSocketClient2 udpSocketClient;  // 替换为 UdpSocketClient

    public UdpSocketClientManager(String serverIp, int serverPort) throws IOException {
        this.executorService = Executors.newSingleThreadExecutor(); // 创建一个具有固定线程数的线程池

        this.udpSocketClient = new UdpSocketClient2(serverIp, serverPort); // 实例化 UdpSocketClient
    }

    public void connect() {
        udpSocketClient.connect(new ConnectionCallback() {
            @Override
            public void onConnectionSuccess() {

            }

            @Override
            public void onConnectionFailure(Exception e) {

            }
        });
    }

    public void sendUdpCommand(byte msgId2, int payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    udpSocketClient.sendData(SendUtils.toData(msgId2, payload)); // 调用 UdpSocketClient 的方法
                    Log.d("UdpSocketClientManager", "msgId2: " + msgId2 + " Command sent: " + payload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void disconnect() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                udpSocketClient.disconnect(); // 调用 UdpSocketClient 的关闭方法
                Log.d("UdpSocketClientManager", "Disconnected from server");
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }
}