package java.com.example.ground_station.data.socket;

import android.text.TextUtils;
import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.BuildConfig;

import java.com.example.ground_station.data.service.ResultCallback;
import java.com.example.ground_station.data.utils.DataUtils;
import java.com.example.ground_station.data.utils.LoopHelper;
import java.com.example.ground_station.data.utils.SendUtils;
import java.com.example.ground_station.data.utils.Utils;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SocketClientHelper implements Clien {

    private static SocketClientHelper media = new SocketClientHelper();
    private static SocketClientHelper dessent = new SocketClientHelper();

    public static SocketClientHelper getMedia() {
        return media;
    }

    public static SocketClientHelper getDessent() {
        return dessent;
    }


    public static final int CONNECTION_TIMEOUT_MS = 5000; // Set a 5-second timeout

    private String ip;
    private int port;

    private volatile boolean isConnected = false;
    private Thread readThread;
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // 创建一个具有固定线程数的线程池;
    private boolean adsChange;
    private ConnectionCallback connectCallBack;
    private ResultCallback<byte[]> callBack;
    private ExecutorService sendThread = ThreadUtils.getSinglePool();


    private final LoopHelper heartbeatHandler = new LoopHelper();
    private final LoopHelper reconnectHandler = new LoopHelper();

    public SocketClientHelper() {
        heartbeatHandler.setTime(SocketClientHelper.CONNECTION_TIMEOUT_MS);
        heartbeatHandler.setRunnable(() -> {
            try {
                byte[] data = SendUtils.toData(SocketConstant.HEART_BEAT, 0);
                socketClient.send(data);
//              sendInstruct(SocketConstant.HEART_BEAT, 0); // 发送心跳包
            } catch (IOException e) {
                if (!reconnectHandler.isRuning()) {
                    reconnectHandler.start();
                    stopHeartbeat();
                }
            }
        });
        reconnectHandler.setTime(SocketClientHelper.CONNECTION_TIMEOUT_MS);
        reconnectHandler.setRunnable(() -> {
            connect(); // 重新连接
        });
    }

    public Clien getClient() {
        return this;
    }

    public InputStream getInputStream() {
        return socketClient.getInputStream();
    }

    @Override
    public synchronized boolean isConnected() {
        return this.isConnected && socketClient != null && socketClient.getSocket() != null
                && socketClient.getSocket().isConnected();
    }

    public synchronized void setConnectState(boolean isConnected) {
        this.isConnected = isConnected;
    }

    private ClientImpl socketClient = new ClientImpl();

    @Override
    public void connect() {
        if (ThreadUtils.isMainThread()) {
            executorService.execute(() -> {
                runConnect();
            });
        } else {
            runConnect();
        }
    }

    private void runConnect() {
        try {
            if (TextUtils.isEmpty(ip) || port == 0) {
                return;
            }
            // 如果已经连接，先断开连接
            if (socketClient.getSocket() != null && socketClient.getSocket().isConnected()) {
                try {
                    socketClient.disconnect(); // 断开当前连接
                    isConnected = false; // 更新状态为未连接
                    stopHeartbeat(); // 停止心跳机制，避免发送数据到已经断开的连接
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            socketClient = new ClientImpl();
            socketClient.connect(ip, port);

            setConnectState(true);
            if (connectCallBack != null) {
                connectCallBack.onConnectionSuccess();
            }

            if (readThread != null) {
                readThread.interrupt();
            }
            readThread = new Thread(new SocketClientHelper.ReadThread());
            readThread.start();

            stopReconnection();
            startHeartbeat();
            Log.d("SocketClientManager", "Connected to server");
        } catch (Exception e) {
            Log.d("SocketClientManager", "Exception " + e);
            setConnectState(false);
            if (connectCallBack != null) {//"连接失败，正在重连"
                connectCallBack.onConnectionFailure(e);
            }
            if (!reconnectHandler.isRuning()) {
                startReconnection(); //开始重连
            }
        }
    }

    private void startHeartbeat() {
        heartbeatHandler.start();
    }

    public void stopHeartbeat() {
        heartbeatHandler.stop();
    }

    private void startReconnection() {
        reconnectHandler.start();
    }

    public void stopReconnection() {
        reconnectHandler.stop();
    }


    @Override
    public void send(byte[] data) {
        sendThread.execute(() -> {
            try {
                socketClient.send(data);
            } catch (IOException e) {
                String message = e.getMessage();
            }
        });
    }

    public void send(String msgId2, String... payload) {
        byte[] data = SendUtils.toData(msgId2, payload);
        send(data);
    }

    int t;
    public void send(byte msgId2, int... payload) {
        send(SendUtils.toData(msgId2, payload));
        if (!filtSend(msgId2)) {
            if (BuildConfig.DEBUG) {
                try {
                    String hexString = Integer.toHexString(msgId2);
                    ToastUtils.showShort("" + hexString);

                    String pv = "";
                    if (payload != null && payload.length > 0) {
                        int p = payload[0];
                        pv = Integer.toHexString(Math.abs(p));
                        if (pv.length() == 1) {
                            pv = "0" + pv;
                        }
                        if (p < 0) {
                            pv = "-" + pv;
                        }
                    }
                    fsMap.put(TimeUtils.getNowString() + t++, hexString + pv);

                    resultCallback.result(fsMap);
                } catch (Exception e) {
                }
            }
        }
    }

    byte[] filtSend = new byte[]{};
//    byte[] filtSend = new byte[]{SocketConstant.PARACHUTE_3E, SocketConstant.PARACHUTE_3C};

    private boolean filtSend(byte msgId2) {
        for (byte b : filtSend) {
            if (b == msgId2) {
                return true;
            }
        }
        return false;
    }

    ResultCallback<Map> resultCallback;

    public void setResultCallback(ResultCallback resultCallback) {
        this.resultCallback = resultCallback;
    }

    public Map<String, String> fsMap = new LinkedHashMap<String, String>() {

        @Override
        protected boolean removeEldestEntry(Entry<String, String> eldest) {
            super.removeEldestEntry(eldest);
            return this.size() > 8;
        }
    };


    @Override
    public void update(String ip, String port) {
        update(ip, Integer.parseInt(port));
    }

    @Override
    public void update(String ip, int port) {
        adsChange = !Objects.equals(this.ip, ip) || this.port != port;
        this.ip = ip;
        this.port = port;
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
        try {
            if (readThread != null && !readThread.isInterrupted()) {
                readThread.interrupt();
            }
            if (socketClient != null && !socketClient.getSocket().isClosed()) {
                socketClient.disconnect();
            }
        } catch (IOException e) {
        }
        setConnectState(false);
        System.out.println("连接已关闭");
    }

    @Override
    public void setConnectCallBack(ConnectionCallback callBack) {
        this.connectCallBack = callBack;
    }

    @Override
    public void setCallBack(ResultCallback<byte[]> callBack) {
        this.callBack = callBack;
    }

    /**
     * 发送指令
     *
     * @param msgId2
     * @param payload
     * @throws IOException
     */
    public void sendInstruct(byte msgId2, int... payload) {
        byte[] data = SendUtils.toData(msgId2, payload);
        // 8. 发送完整消息
        send(data);
    }

    public void sendInstruct(String msgId2, String... payload) {
        byte[] data = SendUtils.toData(msgId2, payload);
        // 8. 发送完整消息
        send(data);
    }

    /**
     * 发送指令
     *
     * @param msgId2
     * @param payload
     * @throws IOException
     */
    public void sendSjInstruct(byte msgId2, int... payload) throws IOException {
        byte[] data = SendUtils.toData(msgId2, payload);
        // 8. 发送完整消息
        send(data);
    }

    class ReadThread implements Runnable {
        @Override
        public void run() {
            try {
                while (isConnected() && readThread != null && !readThread.isInterrupted()) {
                    try {
                        Log.e("udp接收数据开始", " 当前线程id:" + Thread.currentThread().getId());

                        InputStream inputStream = socketClient.getInputStream();
                        if (inputStream != null) {
                            byte[] data = new byte[256];
                            int read = inputStream.read(data);
                            if (read > 0) {
                                byte[] bytes = Utils.subByte(data, 0, read);
                                DataUtils.parse(bytes, callBack);
                            }
                        }
                    } catch (IOException e) {
                        if (isConnected()) {
                            e.printStackTrace();
                            String tag = "clent读取数据失败";
                            Log.e(tag, e.getMessage());
                        } else {
                            String tag = "clent读取数据失败未连";
                            Log.e(tag, e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                Log.e("udp接收数据 Exception", e.getMessage());
            }
        }
    }
}

class ClientImpl {
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    public void connect(String serverIp, int serverPort) throws IOException {
        socket = new Socket();
        socket.connect(new InetSocketAddress(serverIp, serverPort), SocketClientHelper.CONNECTION_TIMEOUT_MS);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    // 发送字节数组到输出流
    public void send(byte[] data) throws IOException {
        if (outputStream != null) {
            outputStream.write(data);
            outputStream.flush();
        }
    }

    public String receiveResponse() throws IOException {
        return receiveResponse(1024);
    }

    public String receiveResponse(int lenght) throws IOException {
        if (inputStream == null) {
            return null;
        }
        byte[] buffer = new byte[lenght];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            return null;
        }
        return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
    }

    public void disconnect() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
        if (socket != null) {
            socket.close();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public InputStream getInputStream() {
        return inputStream;
    }
}

