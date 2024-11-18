package java.com.example.ground_station.presentation.helper;

import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;

import java.com.example.ground_station.data.socket.SocketClient;
import java.util.Vector;

public class SendTaskHelper {

    static SendTaskHelper helper = new SendTaskHelper();
    public static final int BYTE_LENGTH = 6;
    int x = 0;
//    private byte[] ins;
    private Vector<Byte> list = new Vector<>();

    public synchronized static SendTaskHelper getInstance() {
        return helper;
    }

    private final LoopHelper loop;
    int index;

    private SendTaskHelper() {
        loop = new LoopHelper();
        loop.setTime(3000);
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
        Log.d("gs:TaskHelper:Send", TimeUtils.getNowString() + " 执行次数:" + x++);
        try {
            if (socketClient != null && (socketClient.getSocket() != null && socketClient.getSocket().isConnected())) {
                if (list != null && list.size() > 0) {
                    for (Byte in : list) {
                        if (in != null) {
                            socketClient.send(in);
                        }
                    }
                }
            }
            // 接收到消息打印
            System.out.println("接收到客户端的信息是:" + new String().trim());
        } catch (Exception e) {

        }
    }

    public void addInsturd(byte... ins) {
        for (byte in : ins) {
            list.add(in);
        }
    }

    public void remove(byte... ins) {
        for (byte in : ins) {
            int index = list.indexOf(in);
            if (index >= 0) {
                list.remove(index);
            }
        }
    }

    private SocketClient socketClient;

    public LoopHelper getLoop() {
        return loop;
    }
}
