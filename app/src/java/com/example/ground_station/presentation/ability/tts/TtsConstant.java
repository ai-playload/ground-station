package java.com.example.ground_station.presentation.ability.tts;

import android.media.AudioFormat;
import android.media.AudioTrack;

public class TtsConstant {

    // 采样率，表示每秒采样的次数，单位是Hz，常用的有44100、22050、16000、8000等
    public static final int SAMPLE_RATE = 16000;

    // 声道，表示音频的声道数，有单声道和立体声两种，单声道用AudioFormat.CHANNEL_OUT_MONO表示，立体声用AudioFormat.CHANNEL_OUT_STEREO表示
    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;

    // 格式，表示音频的数据格式，有8位和16位两种，8位用AudioFormat.ENCODING_PCM_8BIT表示，16位用AudioFormat.ENCODING_PCM_16BIT表示
    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    // 缓冲区大小，表示AudioTrack内部缓冲区的大小，单位是字节，可以通过AudioTrack.getMinBufferSize()方法来获取最小的缓冲区大小
    public static final int BUFFER_SIZE = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
    ) * 2;
}
