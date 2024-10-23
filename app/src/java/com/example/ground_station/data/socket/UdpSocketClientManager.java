package java.com.example.ground_station.data.socket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UdpSocketClientManager {
    private ExecutorService executorService;
    private UdpSocketClient udpSocketClient;  // 替换为 UdpSocketClient
    private Handler mainHandler;

    public UdpSocketClientManager(String serverIp, int serverPort) throws IOException {
        this.executorService = Executors.newFixedThreadPool(4); // 创建一个具有固定线程数的线程池
        this.udpSocketClient = new UdpSocketClient(serverIp, serverPort); // 实例化 UdpSocketClient
        this.mainHandler = new Handler(Looper.getMainLooper()); // 用于在主线程上处理结果
    }

    public void sendUdpCommand(byte msgId2, String payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    udpSocketClient.sendCommand(msgId2, payload); // 调用 UdpSocketClient 的方法
                    Log.d("UdpSocketClientManager", "msgId2: " + msgId2 + " Command sent: " + payload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendUdpCommand(byte msgId2, int payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    udpSocketClient.sendCommand(msgId2, payload); // 调用 UdpSocketClient 的方法
                    Log.d("UdpSocketClientManager", "msgId2: " + msgId2 + " Command sent: " + payload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendUdpThanReceiveCommand(byte msgId2, int payload, Runnable callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    udpSocketClient.sendCommand(msgId2, payload); // 调用 UdpSocketClient 的方法

                    if (callback != null) {
                        callback.run();
                    }
                    Log.d("UdpSocketClientManager", "msgId2: " + msgId2 + " Command sent: " + payload);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void receiveResponse(ResponseCallback callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String response = udpSocketClient.receiveResponse(); // 调用 UdpSocketClient 的方法
                    Log.d("UdpSocketClientManager", "Received response from server: " + response);
                    mainHandler.post(() -> {
                        if (callback != null) {
                            callback.onResponse(response);
                        }
                    });
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
                udpSocketClient.close(); // 调用 UdpSocketClient 的关闭方法
                Log.d("UdpSocketClientManager", "Disconnected from server");
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public interface ResponseCallback {
        void onResponse(String response);
    }

    public interface ConnectionCallback {
        void onConnectionSuccess();

        void onConnectionFailure(Exception e);
    }
}