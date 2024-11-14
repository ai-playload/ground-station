package java.com.example.ground_station.presentation.helper;

import android.util.Log;

import java.com.example.ground_station.data.socket.SocketClient;
import java.com.example.ground_station.presentation.callback.ResultCallback;
import java.io.IOException;
import java.io.InputStream;

public class RevHelper implements Runnable {

    private ResultCallback<byte[]> callback;
    private SocketClient socketClient;

    private RevHelper() {
    }

    static RevHelper revHelper = new RevHelper();

    public static synchronized RevHelper getInstance() {
        return revHelper;
    }

    private boolean run;

    public void start() {
        run = true;
        new Thread(this).start();
    }

    public void stop() {
        run = false;
    }

    public void setCallback(ResultCallback<byte[]> callback) {
        this.callback = callback;
    }

    public static final int BYTE_LENGTH = 6;
    int x = 0;

    @Override
    public void run() {
        while (run) {
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
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private InputStream getInput() {
        if (socketClient != null && socketClient.getSocket() != null && socketClient.getSocket().isConnected()) {
            InputStream inputStream = socketClient.getInputStream();
            if (inputStream != null) {
                return inputStream;
            }
        }
        return null;
    }

    private boolean checkClient() {
        return false;
    }

    public void setSocketManager(SocketClient socketClient) {
        this.socketClient = socketClient;
    }
}
