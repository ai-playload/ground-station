package java.com.example.ground_station.presentation.ability.tts;

import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.SystemClock;
import android.speech.tts.SynthesisCallback;
import android.util.Log;

import androidx.annotation.NonNull;

import java.com.example.ground_station.presentation.ability.AbilityCallback;
import java.com.example.ground_station.presentation.ability.AbilityConstant;
import java.com.example.ground_station.presentation.ability.AudioFileGenerationCallback;
import java.com.example.ground_station.presentation.util.PcmUtil;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TtsHelper2 implements SynthesisCallback {

    private TtsSpeechPrams ttsParams = new TtsSpeechPrams();
    private ExecutorService executorService;
    private AudioTrack audioTrack;
    private TtsEngine ttsEngine;
    private volatile boolean loop = true;
    private volatile boolean isSpeaking = false;
    private Queue<String> textQueue;
    private String engineId;
    private volatile String text;
    private volatile boolean isFlush = false;
    private AbilityCallback callBack;
    private Handler fileHandler;
    private File recordFile;
    private AudioFileGenerationCallback audioFileGenerationCallback;
    int fileNameIndex = 0;

    private static final String TAG = TtsHelper.class.getSimpleName();
    private String fileName = "";
    private int flag = 0;
    private long length;

    public TtsHelper2() {
        textQueue = new ConcurrentLinkedQueue<>();
        executorService = Executors.newFixedThreadPool(2);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioTrack = new AudioTrack.Builder()
                    .setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .build()
                    )
                    .setAudioFormat(
                            new AudioFormat.Builder()
                                    .setEncoding(TtsConstant.AUDIO_FORMAT)
                                    .setSampleRate(TtsConstant.SAMPLE_RATE)
                                    .setChannelMask(TtsConstant.CHANNEL_CONFIG)
                                    .build()
                    )
                    .setBufferSizeInBytes(TtsConstant.BUFFER_SIZE)
                    .build();
        } else {
            audioTrack = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    TtsConstant.SAMPLE_RATE,
                    TtsConstant.CHANNEL_CONFIG,
                    TtsConstant.AUDIO_FORMAT,
                    TtsConstant.BUFFER_SIZE,
                    AudioTrack.MODE_STREAM
            );
        }
        audioTrack.play();
        executorService.execute(() -> {
            while (loop) {
                text = textQueue.poll();
                if (text != null) {
                    ttsEngine.textToSpeech(text, ttsParams, this);
                }
                SystemClock.sleep(50L);
            }
        });
        HandlerThread handlerThread = new HandlerThread("writeFile");
        handlerThread.start();
        fileHandler = new Handler(handlerThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                int what = msg.what;
                if (what == 0) {
                    createFile();
                } else if (what == 1) {
                    Object result = msg.obj;
                    if (!(result instanceof byte[])) {
                        result = null;
                    }
                    byte[] audioBytes = (byte[]) result;
                    if (audioBytes == null) {
                        return false;
                    }
                    byte[] data = audioBytes;
                    writeToFile(data);
                }
                return true;
            }
        });
    }

    private IFileWriteListener fileWriteListener;

    public void setFileWriteListener(IFileWriteListener fileWriteListener) {
        this.fileWriteListener = fileWriteListener;
    }

    public interface IFileWriteListener {
        void onSuccess(File file);
    }

    public void setFileName(int flag) {
        this.flag = flag;
        int tag = flag % 10;
        this.fileName = "t" + tag + ".mp3";
    }

    public String getFileName() {
        return fileName;
    }

    public void onCreate(String engineId, AbilityCallback callBack) {
        this.engineId = engineId;
        if (ttsEngine == null) {
            ttsEngine = new TtsEngine();
            ttsEngine.onCreate(engineId);
        }
        this.callBack = callBack;
    }

    public static final String saveFolder = "/sdcard/Music";

    public File getRecordFile() {
//        recordFile = new File(saveFolder,  this.fileName);
        return recordFile;
    }

    public void setVCN(String vcn) {
        Log.i(TAG, "设置发音人==>" + vcn);
        ttsParams.setVcn(vcn);
        if (engineId.equals(AbilityConstant.TTS_ID)) return;
        switch (vcn) {
            case "xiaoyan":
            case "xiaofeng":
                ttsParams.setLanguage(1);
                break;
            case "catherine":
                ttsParams.setLanguage(2);
                break;
            default:
                break;
        }
    }

    public void setSpeed(int speed) {
        Log.i(TAG, "设置发音人语速==>" + speed);
        ttsParams.setSpeed(speed);
    }

    public void setPitch(int pitch) {
        Log.i(TAG, "设置发音人音调==>" + pitch);
        ttsParams.setPitch(pitch);
    }

    public void setVolume(int volume) {
        Log.i(TAG, "设置发音人音量==>" + volume);
        ttsParams.setVolume(volume);
    }

    public void speechText(String text) {
        textQueue.offer(text);
        audioTrack.play();
        resume();
    }

    public void speakTextAndSaveWav(String text, AudioFileGenerationCallback audioFileGenerationCallback) {
        textQueue.offer(text);
        audioTrack.play();
        resume();

        this.audioFileGenerationCallback = audioFileGenerationCallback;
    }

    public void stopSpeechText(String text) {
        if (!isFlush) {
            isFlush = true;
            executorService.execute(() -> {
                stop();
                SystemClock.sleep(200);
                speechText(text);
                isFlush = false;
            });
        }
    }

    public void pause() {
        audioTrack.pause();
        ttsEngine.pause();
    }

    public void resume() {
        audioTrack.play();
        ttsEngine.resume();
    }

    public void stop() {
        textQueue.clear();
        audioTrack.pause();
        audioTrack.flush();
        ttsEngine.stop();
    }

    public void release() {
        loop = false;
        audioTrack.release();
        executorService.shutdownNow();
        ttsEngine.onDestroy();
        audioTrack = null;
        executorService = null;
        ttsEngine = null;
    }

    public boolean isSpeaking() {
        return isSpeaking || !textQueue.isEmpty();
    }

    @Override
    public int getMaxBufferSize() {
        return 0;
    }

    @Override
    public int start(int sampleRateInHz, int audioFormat, int channelCount) {
        isSpeaking = true;
        mainThread(() -> callBack.onAbilityBegin());

        if (audioFileGenerationCallback != null) {
            audioFileGenerationCallback.onAudioFileGenerationStart();
        }

        if (fileHandler != null) {
            fileHandler.obtainMessage(0).sendToTarget();
        }
        return 0;
    }

    @Override
    public int audioAvailable(byte[] buffer, int offset, int length) {
        if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.write(buffer, offset, length);
        }

        if (fileHandler != null) {
            fileHandler.obtainMessage(1, buffer).sendToTarget();
        }
        return 0;
    }

    @Override
    public int done() {
        isSpeaking = false;
        mainThread(() -> callBack.onAbilityEnd());

        if (audioFileGenerationCallback != null) {
            audioFileGenerationCallback.onAudioFileGenerationEnd(recordFile.getPath());
        }
        return 0;
    }

    @Override
    public void error() {
    }

    @Override
    public void error(int errorCode) {
        mainThread(() -> callBack.onAbilityError(errorCode, new Throwable("TTS error")));
    }


    /**
     * 创建合成音频保存的文件
     */
    private void createFile() {
//        String saveFolder = SPUtil.INSTANCE.getString("audio_folder_path", "");
//        if (TextUtils.isEmpty(saveFolder)) {
//            String absolutePath = Objects.requireNonNull(MyApp.Companion.getCONTEXT().getExternalCacheDir()).getAbsolutePath();
//            recordFile = new File(
//                    absolutePath,
//                    System.currentTimeMillis() + ".wav"
//            );
//        } else {
//            recordFile = new File(
//                    saveFolder,
//                    System.currentTimeMillis() + ".wav"
//            );
//        }

        String saveFolder = "/sdcard/Music";
        File musicDir = new File(saveFolder);
        if (!musicDir.exists()) {
            if (!musicDir.mkdirs()) {
//                throw new RuntimeException("无法创建目录: " + saveFolder);
                Log.d(TAG, "无法创建目录: " + saveFolder);
            }
        }
// TODO: 2024/10/26  
//        String name =


//        recordFile = new File(
//                saveFolder,
//                "001.wav"
//        );

        if (recordFile == null) {
        }
        getRecordFile();
        recordFile = new File(saveFolder,  this.fileName);
        try {
            PcmUtil.changeWavHead(recordFile);
            length = recordFile.length();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final void writeToFile(byte[] data) {
        try {
            File file = this.recordFile;
            if (file != null && file.length() <= length) {
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(data);
                fos.flush();
                fos.close();
                if (fileWriteListener != null) {
                    fileWriteListener.onSuccess(file);
                }
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }
    }

    @Override
    public boolean hasStarted() {
        return false;
    }

    @Override
    public boolean hasFinished() {
        return false;
    }

    private void mainThread(Runnable runnable) {
        // Implement your method to run the runnable on the main thread
    }
}
