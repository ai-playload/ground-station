package java.com.example.ground_station.data.utils;

import android.util.Log;

import java.com.example.ground_station.data.service.ResultCallback;
import java.com.example.ground_station.data.socket.SocketClient;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.io.InputStream;

public class RecvTaskHelper {

    static RecvTaskHelper helper = new RecvTaskHelper();
    public static final int BYTE_LENGTH = 6;
    int x = 0;

    public synchronized static RecvTaskHelper getInstance() {
        return helper;
    }

    private final LoopHelper loop;
    int index;

    private RecvTaskHelper() {
        loop = new LoopHelper();
        loop.setTime(100);
        loop.setRunnable(new Runnable() {
            @Override
            public void run() {
                runTask();
                Log.d("RecvTaskHelper", " index:" + index++);
            }
        });
    }

    public void setSocketManager(SocketClientHelper socketClient) {
        this.socketClient = socketClient;
    }

    public void runTask() {
        Log.d("taxa", "x:" + x++);
        InputStream inputStream = getInput();
        if (inputStream != null) {
            try {
                byte[] bytes = new byte[BYTE_LENGTH];
                // 读取客户端发送的信息
                int count = inputStream.read(bytes, 0, BYTE_LENGTH);
                if (count > 0) {
                    if (this.callback != null) {
                        callback.result(bytes);
                    }
                    // 接收到消息打印
                    System.out.println("接收到客户端的信息是:" + new String(bytes).trim());
                }
                count = 0;
            } catch (Exception e) {
// TODO: 2024/11/17  
            }
        }
    }

    private InputStream getInput() {
        if (socketClient != null && socketClient.getClient() != null
                && socketClient.getClient().isConnected()) {
            InputStream inputStream = socketClient.getInputStream();
            if (inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }

    private ResultCallback<byte[]> callback;
    private SocketClientHelper socketClient;

    public void setCallback(ResultCallback<byte[]> callback) {
        this.callback = callback;
    }

    public LoopHelper getLoop() {
        return loop;
    }
}
