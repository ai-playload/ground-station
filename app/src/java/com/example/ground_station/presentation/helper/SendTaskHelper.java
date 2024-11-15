package java.com.example.ground_station.presentation.helper;

import android.os.Looper;
import android.util.Log;

import java.com.example.ground_station.data.socket.SocketClient;

public class SendTaskHelper {

    static SendTaskHelper helper = new SendTaskHelper();
    public static final int BYTE_LENGTH = 6;
    int x = 0;
    private byte[] ins;

    public synchronized static SendTaskHelper getInstance() {
        return helper;
    }

    private final LoopHelper loop;
    int index;

    private SendTaskHelper() {
        loop = new LoopHelper();
        loop.setTime(1000);
        loop.setRunnable(new Runnable() {
            @Override
            public void run() {
                runTast();
                Log.d("SendTaskHelper", " index:" + index++);
            }
        });
    }

    public void setSocketManager(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void runTast() {
        Log.d("taxa", "x:" + x++);
        try {
            if (socketClient != null && (socketClient.getSocket() != null && socketClient.getSocket().isConnected())) {
                if (ins != null) {
                    for (byte in : ins) {
                        socketClient.send(in);
                    }
                }
            }
            // 接收到消息打印
            System.out.println("接收到客户端的信息是:" + new String().trim());
        } catch (Exception e) {

        }
    }

    public void setInsturd(byte... ins) {
        this.ins = ins;
    }

    private SocketClient socketClient;

    public LoopHelper getLoop() {
        return loop;
    }
}
