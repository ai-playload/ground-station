package java.com.example.ground_station.data.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.iflytek.aikitdemo.tool.ThreadExtKt;
import com.lzf.easyfloat.EasyFloat;

import org.freedesktop.gstreamer.GStreamer;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.ConnectionCallback;
import java.com.example.ground_station.data.socket.ResponseCallback;
import java.com.example.ground_station.data.socket.SocketClientManager;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.socket.UdpSocketClientManager;
import java.com.example.ground_station.presentation.ability.AbilityCallback;
import java.com.example.ground_station.presentation.ability.AbilityConstant;
import java.com.example.ground_station.presentation.ability.AudioFileGenerationCallback;
import java.com.example.ground_station.presentation.ability.tts.TtsHelper2;
import java.com.example.ground_station.presentation.floating.FloatingWindowHelper;
import java.com.example.ground_station.presentation.util.GsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;

public class GroundStationService extends Service implements AbilityCallback {
    private static final String TAG = "GroundStationService";

    private static final String CHANNEL_ID = "GroundStationServiceChannel";

    private final IBinder binder = new LocalBinder();

    private native void nativeInit();     // Initialize native code, build pipeline, etc

    private native void nativeFinalize(); // Destroy pipeline and shutdown native code

    private native void nativeConnect(String command);

    private native void nativeDisconnect();

    private native boolean nativeIsConnected();

    private native void nativePlay();     // Set pipeline to PLAYING

    private native void nativePause();    // Set pipeline to PAUSED

    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks

    private long native_custom_data;      // Native code will use this to keep private data

    public boolean isShouting;
    private TtsHelper2 aiSoundHelper;
    private SocketClientManager socketClientManager;
    private UdpSocketClientManager udpSocketClientManager;
    private ShoutcasterConfig config;
    private PlaybackCallback playbackCallback;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public TtsHelper2 getTtsHelper() {
        return aiSoundHelper;
    }

    public boolean isConnectedSocket() {
        SocketClientManager manager = getSocketClientManager();
        return manager != null && manager.isConnected;
    }

    public class LocalBinder extends Binder {
        public GroundStationService getService() {
            return GroundStationService.this;
        }
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("ground-station");
        nativeClassInit();
    }

    public SocketClientManager getSocketClientManager() {
        return socketClientManager;
    }

    public UdpSocketClientManager getUdpSocketClientManager() {
        return udpSocketClientManager;
    }

    public void setShoutcasterConfig(ShoutcasterConfig config, ConnectionCallback callback) {
        this.config = config;

        connectSocket(callback);
    }

    private void initGStreamer() {
        try {
            GStreamer.init(this);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        nativeInit();
    }

    private void setMessage(final String message) {
        Log.d(TAG, message);
    }

    private void onGStreamerInitialized() {
        Log.i(TAG, "Gst initialized. Restoring state, playing:");

        nativePlay();
    }

    private void showToast(String message) {

        ThreadExtKt.mainThread(new Function0<Unit>() {
            @Override
            public Unit invoke() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                return Unit.INSTANCE;
            }
        });
    }

    @Override
    public void onCreate() {
        super.onCreate();

        initTts();
        initGStreamer();
//        createNotificationChannel();
//        startForegroundService();
    }

    // 设置回调的方法
    public void setPlaybackCallback(PlaybackCallback callback) {
        this.playbackCallback = callback;
    }

    // Java 方法用于接收播放结束通知
    public void onPlaybackEnded() {
        if (playbackCallback != null) {
            ThreadExtKt.mainThread(() -> {
                playbackCallback.onPlaybackEnded();
                return Unit.INSTANCE;
            });
        }
    }

    public void sendGstreamerCommand(String command) {
        if (nativeIsConnected()) {
            nativeDisconnect();
            initGStreamer();
        }

        if (!isShouting) {
            return;
        }

        ShoutcasterConfig.DeviceInfo shoutcaster = config.getShoutcaster();
        if (shoutcaster != null) {
            String compCommand = String.format(command, shoutcaster.getIp(), shoutcaster.getPort());
            nativeConnect(compCommand);
        }
    }

    public void cancelGstreamerCommand() {
        nativeFinalize();
    }

    public void sendShoutCommand(String command) {
        isShouting = !isShouting;
        sendGstreamerCommand(command);
    }

    public void cancelGstreamerAudioCommand() {
        isShouting = true;
        sendShoutCommand("");
    }

    public void sendMusicCommand(String command) {
        if (nativeIsConnected()) {
            nativeDisconnect();
            initGStreamer();
        }

        nativeConnect(command);
    }

    private void initTts() {
        aiSoundHelper = new TtsHelper2();
        aiSoundHelper.onCreate(AbilityConstant.TTS_ID, this);
    }

    public void speechText(String text) {
        if (aiSoundHelper != null) {
            aiSoundHelper.speechText(text);
        }
    }

    public TtsHelper2 getAiSoundHelper() {
        return aiSoundHelper;
    }

    public ShoutcasterConfig getConfig() {
        return config;
    }

    public void generateAudioFile(String text, AudioFileGenerationCallback audioFileGenerationCallback) {
        if (aiSoundHelper != null) {
//            aiSoundHelper.setAudioFileGenerationCallback(audioFileGenerationCallback);
            aiSoundHelper.speakTextAndSaveWav(text, audioFileGenerationCallback);
        }
    }

    private void connectSocket(ConnectionCallback callback) {
        ShoutcasterConfig.DeviceInfo controller = config.getController();
        ShoutcasterConfig.DeviceInfo cloudLightInfo = config.getCloudLightInfo();

        if (socketClientManager == null) {
            socketClientManager = new SocketClientManager(getApplicationContext(), controller.getIp(), controller.getPort());
        }
        socketClientManager.connect(controller, callback);

        try {
            udpSocketClientManager = new UdpSocketClientManager(cloudLightInfo.getIp(), cloudLightInfo.getPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendUdpSocketCommand(byte msgId2, int payload) {
        udpSocketClientManager.sendUdpCommand(msgId2, payload);
    }

    public void sendSocketCommand(byte msgId2, int payload) {
        socketClientManager.sendSocketCommand(msgId2, payload);
    }

    public void sendRemoteAudioCommand(byte msgId2, int payload, int playState) {
        socketClientManager.sendRemoteAudioCommand(msgId2, payload, playState);
    }

    public void sendSocketThanReceiveCommand(byte msgId2, int payload, Runnable callback) {
        socketClientManager.sendSocketThanReceiveCommand(msgId2, payload, callback);
    }

    public void getAudioListInfo(ResultCallBack<List<AudioModel>> callBack) {
        requestAudioListInfo(callBack, 0);
    }

    private void requestAudioListInfo(ResultCallBack<List<AudioModel>> callBack, final int num) {
        sendSocketThanReceiveCommand(SocketConstant.GET_RECORD_LIST, 0, () -> {
            receiveResponse(response -> {
                if (response != null && response.length() > 1 && !response.startsWith("1")) {//gson
                    Log.d(TAG, "ttkx Received response: " + response);

                    // 查找最后一个 ']' 的位置，并截取到该位置为止
                    int lastIndex = response.lastIndexOf("]");
                    if (lastIndex != -1) {
                        response = response.substring(0, lastIndex + 1);  // 保留到最后的 ']'
                    }
                    Log.d(TAG, "Received response after modification: " + response);

                    try {
                        GsonParser gsonParser = new GsonParser();
                        List<AudioModel> audioModelList = getAllRemoteAudioToAudioModel(gsonParser.parseAudioFileList(response));
                        Log.d(TAG, "Received response: " + response);
                        callBack.result(audioModelList);
                    } catch (Exception e) {
                        Log.e(TAG, " error: " + e);
                        callBack.result(null);
                    }
                } else {
                    int newNum = num + 1;
                    if (newNum >= 3) {
                        callBack.result(null);
                    } else {
                        requestAudioListInfo(callBack, newNum);
                    }
                }
            });
        });
    }

    private List<AudioModel> getAllRemoteAudioToAudioModel(List<String> filePaths) {
        List<AudioModel> audioModelList = new ArrayList<>();
        for (String filePath : filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            audioModelList.add(new AudioModel(fileName, filePath, false));
        }
        return audioModelList;
    }

    public void receiveResponse(ResponseCallback callback) {
        socketClientManager.receiveResponse(callback);
    }

    public void sendSetVolumeCommand(int volume) {
        socketClientManager.sendSetVolumeCommand(volume);
    }

    public void sendServoCommand(int direction) {
        socketClientManager.sendServoCommand(direction);
    }

    public void sendDetectorCommand(int payload) {
        socketClientManager.sendDetectorCommand(payload);
    }

    public void checkConnectionStatus() {
        boolean isConnected = nativeIsConnected();
        if (isConnected) {
            Toast.makeText(getApplicationContext(), "Connected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();
        }
    }

    public void pause() {
        nativePause();
    }

    public void play() {
        nativePlay();
    }

    public void showFloatingWindow(AppCompatActivity activity) {
        if (!EasyFloat.isShow(FloatingWindowHelper.tag)) {
            FloatingWindowHelper floatingWindowHelper = new FloatingWindowHelper();
            floatingWindowHelper.showFloatingWindow(activity);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        nativeFinalize();

        if (socketClientManager != null) {
            socketClientManager.disconnect();
            socketClientManager.shutdown();
        }
    }

    @Override
    public void onAbilityBegin() {

    }

    @Override
    public void onAbilityResult(String result) {

    }

    @Override
    public void onAbilityError(int code, Throwable error) {

    }

    @Override
    public void onAbilityEnd() {

    }

    /**
     * 网络点播控制-开始播放
     */
    public void netBpStart(int fileIndex) {
        this.sendInstruct(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, fileIndex, SocketConstant.PM.PLAY_BUNCH_START);
    }

    /**
     * 网络点播控制-停止
     */
    public void netBpStop(int fileIndex) {
        this.sendInstruct(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, fileIndex, SocketConstant.PM.PLAY_BUNCH_STOP);
    }

    /**
     * 网络点播控制-暂停
     */
    public void netBpPause(int fileIndex) {
        this.sendInstruct(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, fileIndex, SocketConstant.PM.PLAY_BUNCH_PAUSE);
    }

    /**
     * 网络点播控制-暂停后恢复
     */
    public void netBpRecoverPlay(int fileIndex) {
        this.sendInstruct(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, fileIndex, SocketConstant.PM.PLAY_BUNCH_RECOVER_PLAY);
    }

    public void sendInstruct(byte msgId2, int... payload) {
        socketClientManager.sendInstruct(msgId2, payload);
    }

    public void sendInstruct(String msgId2, String... payload) {
        socketClientManager.sendInstruct(msgId2, payload);
    }

}
