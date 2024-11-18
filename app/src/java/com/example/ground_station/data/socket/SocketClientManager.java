package java.com.example.ground_station.data.socket;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.presentation.callback.ResultCallback;

import java.com.example.ground_station.presentation.helper.RecvTaskHelper;
import java.com.example.ground_station.presentation.helper.SendTaskHelper;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketClientManager {
    private ExecutorService executorService;
    private final Handler mainHandler;
    private final Handler reconnectHandler; // Handler for reconnection attempts
    private final Handler heartbeatHandler; // Handler for heartbeat
    private final int RECONNECT_DELAY = 5000; // Delay between reconnection attempts (5 seconds)
    private final int HEARTBEAT_INTERVAL = 5000; // Heartbeat interval (10 seconds)
    public volatile boolean isReconnecting = false;
    public boolean isConnected = false;
    private final Context context;
    private ShoutcasterConfig.DeviceInfo controller;
    private SocketClient socketClient;
    private ExecutorService jsMsgThread;

    private static SocketClientManager manager = new SocketClientManager(Utils.getApp());

    public static SocketClientManager getInstance() {
        return manager;
    }

    public SocketClientManager(Context context) {
        this.executorService = Executors.newSingleThreadExecutor(); // 创建一个具有固定线程数的线程池
        this.mainHandler = new Handler(Looper.getMainLooper()); // 用于在主线程上处理结果
        this.reconnectHandler = new Handler(Looper.getMainLooper()); // Reconnection handler
        this.heartbeatHandler = new Handler(Looper.getMainLooper()); // Reconnection handler
        this.context = context;
    }
//    public SocketClientManager(Context context, String serverIp, int serverPort) {
//        this.executorService = Executors.newSingleThreadExecutor(); // 创建一个具有固定线程数的线程池
//        this.socketClient = new SocketClient(serverIp, serverPort);
//        this.mainHandler = new Handler(Looper.getMainLooper()); // 用于在主线程上处理结果
//        this.reconnectHandler = new Handler(Looper.getMainLooper()); // Reconnection handler
//        this.heartbeatHandler = new Handler(Looper.getMainLooper()); // Reconnection handler
//        this.context = context;
//    }

    public void connect(ShoutcasterConfig.DeviceInfo controller, ConnectionCallback callback) {
        this.controller = controller;

        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    // 如果已经连接，先断开连接
                    if (socketClient != null && socketClient.getSocket() != null && socketClient.getSocket().isConnected()) {
                        try {
                            socketClient.disconnect(); // 断开当前连接
                            isConnected = false; // 更新状态为未连接
                            stopHeartbeat(); // 停止心跳机制，避免发送数据到已经断开的连接
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    socketClient = new SocketClient(controller.getIp(), controller.getPort());

                    socketClient.connect(controller.getIp(), controller.getPort());
                    mainHandler.post(() -> {
                        Toast.makeText(context, "连接成功", Toast.LENGTH_SHORT).show();

                        if (callback != null) {
                            callback.onConnectionSuccess();
                        }
                    });
                    isConnected = true;
                    stopReconnection();
                    startHeartbeat();

                    RecvTaskHelper.getInstance().setSocketManager(socketClient);
                    SendTaskHelper.getInstance().setSocketManager(socketClient);

                    Log.d("SocketClientManager", "Connected to server");
                } catch (Exception e) {
                    Log.d("SocketClientManager", "Exception " + e);
                    isConnected = false;

                    mainHandler.post(() -> {
                        Toast.makeText(context, "连接失败，正在重连", Toast.LENGTH_SHORT).show();

                        if (callback != null) {
                            callback.onConnectionFailure(e);
                        }
                    });
                    if (!isReconnecting) {
                        startReconnection(callback); // Start reconnection attempts on failure
                    }
                }
            }
        });
    }



    // 心跳包逻辑
    private void startHeartbeat() {
        heartbeatHandler.post(new Runnable() {
            @Override
            public void run() {
                executorService.execute(() -> {
                    try {
                        socketClient.sendCommand(SocketConstant.HEART_BEAT, 0); // 发送心跳包
                        Log.d("SocketClientManager", "Heartbeat sent");
                    } catch (IOException e) {
                        Log.d("SocketClientManager", "Heartbeat failed, reconnecting...");
                        if (!isReconnecting) {
                            startReconnection(null);
                        }
                    }
                });
                heartbeatHandler.postDelayed(this, HEARTBEAT_INTERVAL);
            }
        });
    }

    public void stopHeartbeat() {
        heartbeatHandler.removeCallbacksAndMessages(null);
    }

    private void startReconnection(ConnectionCallback callback) {
        isReconnecting = true;
        reconnectHandler.post(new Runnable() {
            @Override
            public void run() {
                Log.d("SocketClientManager", "Attempting to reconnect... ip: " + controller.getIp() + " port: " + controller.getPort());
                connect(controller, callback); // Try to reconnect
                if (isReconnecting) {
                    reconnectHandler.postDelayed(this, RECONNECT_DELAY); // Retry after delay if still reconnecting
                }
            }
        });
    }

    public void stopReconnection() {
        isReconnecting = false;
        reconnectHandler.removeCallbacksAndMessages(null); // Stop reconnection attempts
    }

    public void sendSocketCommand(byte msgId2, int payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient.sendCommand(msgId2, payload);
//                    socketClient.sendSetVolumeCommand(100);
                    Log.d("SocketClientManager", "msgId2: " + msgId2 + " Command sent: " + payload);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d("SocketClientManager", "IOException e " + e);
                    if (!isReconnecting) {
//                        startReconnection(null); // Start reconnection attempts on failure
                    }
                }
            }
        });
    }

    public void sendSocketThanReceiveCommand(byte msgId2, int payload, Runnable callback) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient.sendCommand(msgId2, payload);

                    if (callback != null) {
                        callback.run();
                    }
                    Log.d("SocketClientManager", "msgId2: " + msgId2 + " Command sent: " + payload);
                } catch (IOException e) {
                    if (!isReconnecting) {
//                        startReconnection(null); // Start reconnection attempts on failure
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendSetVolumeCommand(int volume) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient.sendSetVolumeCommand(volume);
                    Log.d("SocketClientManager", "volume: " + volume);
                } catch (IOException e) {
                    if (!isReconnecting) {
//                        startReconnection(null); // Start reconnection attempts on failure
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendRemoteAudioCommand(byte msgId2, int payload, int playState) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient.sendRemoteAudioCommand(msgId2, payload, playState);
                    Log.d("SocketClientManager", "volume: " + playState);
                } catch (IOException e) {
                    if (!isReconnecting) {
//                        startReconnection(null); // Start reconnection attempts on failure
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendServoCommand(int direction) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient.sendServoCommand(direction);
                    Log.d("SocketClientManager", "direction: " + direction);
                } catch (IOException e) {
                    if (!isReconnecting) {
//                        startReconnection(null); // Start reconnection attempts on failure
                    }
                    e.printStackTrace();
                }
            }
        });
    }

    public void sendDetectorCommand(int payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient.sendDetectorCommand(payload);
                    Log.d("SocketClientManager", "sendDetectorCommand msg2: " + payload);
                } catch (IOException e) {
                    if (!isReconnecting) {
//                        startReconnection(null); // Start reconnection attempts on failure
                    }
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
                    String response = socketClient.receiveResponse(2048);
                    Log.d("SocketClientManager", "Received response from server: " + response);
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
                try {
                    stopReconnection();
                    stopHeartbeat();
                    socketClient.disconnect();
                    Log.d("SocketClientManager", "Disconnected from server");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public void sendMsgAndCallBack(ResultCallback<byte[]> resultCallback ) {
        if (jsMsgThread == null) {
            jsMsgThread = ThreadUtils.getFixedPool(1);
            jsMsgThread.execute(new Runnable() {
                @Override
                public void run() {
                    socketClient.jsX(resultCallback);
                }
            });
        }


//        executorService.execute(new Runnable() {
//            @Override
//            public void run() {
//
//
//
//                String msg = "指令：";
//                try {
//                    socketClient.sendServoCommand(msgId2);
//                    byte[] bytes = socketClient.receiveResponseByte(1024);
//                    if (bytes != null) {
//                        msg += Utils.bytesToHexFun3(bytes);
//                    }
//                    if (bytes != null && bytes.length >= 4) {
//                        if (bytes[0] == SocketConstant.HEADER && bytes[3] == msgId2) {
//                            byte v = bytes[4];
//                            resultCallback.result(v);
//                            return;
//                        }
//                    }
//                    Log.d("SocketClientManager", "Disconnected from server");
//                } catch (Exception e) {
//                    String message = e.getMessage();
//                    msg += message;
//                    e.printStackTrace();
//                }
//                ToastUtils.showLong(msg);
//            }
//        });
    }

    public void send(byte msgId2, int... payload) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    socketClient.send(msgId2, payload);
                } catch (IOException e) {
                    if (!isReconnecting) {
//                        startReconnection(null); // Start reconnection attempts on failure
                    }
                    e.printStackTrace();
                }
            }
        });
    }
}

