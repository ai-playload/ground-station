package java.com.example.ground_station.data.socket;

public class SocketConstant {
    // Header
    public static final byte HEADER = (byte) 0x8d;

    public static final byte MSG_ID1 = (byte) 0x01;

    // 功能消息 ID
    public static final byte LEFT_ROTATION = (byte) 0x01;             // 左转
    public static final byte RIGHT_ROTATION = (byte) 0x02;            // 右转
    public static final byte CENTER = (byte) 0x05;                    // 居中

    public static final byte START_TALK = (byte) 0x08;                // 开始喊话
    public static final byte STOP_TALK = (byte) 0x09;                 // 停止喊话
    public static final byte GET_RECORD_LIST = (byte) 0x0a;           // 获取录音文件列表
    public static final byte DELETE_RECORD = (byte) 0x0b;             // 删除录音
    public static final byte PLAY_RECORD = (byte) 0x0c;               // 播放录音
    public static final byte STOP_PLAY_RECORD = (byte) 0x0d;          // 停止播放录音
    public static final byte SET_VOLUME = (byte) 0x0e;                // 设置音量
    public static final byte TEXT_TO_SPEECH = (byte) 0x0f;            // 文字转语音
    public static final byte TEXT_TO_SPEECH_LOOP = (byte) 0x11;       // 文字转语音循环播放
    public static final byte STOP_TEXT_TO_SPEECH_LOOP = (byte) 0x12;  // 停止文字转语音循环播放
    public static final byte PLAY_ALARM = (byte) 0x13;                // 播放警报
    public static final byte STOP_PLAY_ALARM = (byte) 0x14;           // 停止播放警报
    public static final byte UPLOAD_AUDIO = (byte) 0x15;              // 上传音频文件
    public static final byte DATA_PACKET = (byte) 0x16;               // 数据包
    public static final byte TEXT_TO_SPEECH_V2 = (byte) 0x17;         // 文字转语音V2
    public static final byte SERVO = (byte) 0x30;
    public static final byte DIRECTION = (byte) 0x46;
    public static final byte AMPLIFIER = (byte) 0x9a;
    public static final byte STREAMER = (byte) 0x9b;

    public static final byte PLAY_RECORD_Bp = (byte) 0x9c;               // 网络循环播放

    public static final byte GET_TTS_LIST = (byte) 0x9d;           // 获取文字转语音 语音文件列表
    public static final byte PLAY_TTS_Bp = (byte) 0x9d;           // 获取文字转语音 语音文件循环播放


    // 新增的命令
    public static final byte REBOOT_DEVICE = (byte) 0x35;             // 设备重启
    public static final byte BOMB_STATUS = (byte) 0x36;               // 炸弹状态
    public static final byte DROP_BOMB = (byte) 0x37;                 // 投弹
    public static final byte PARACHUTE = (byte) 0x38;                 // 缓降器
    public static final byte PARACHUTE_CONTROL = (byte) 0x39;         // 缓降器紧急控制
    public static final byte PARACHUTE_SPEED = (byte) 0x3a;           // 缓降器速度控制
    public static final byte SERVO_STATUS = (byte) 0x3f;              // 舵机状态
    public static final byte PARACHUTE_LENGTH = (byte) 0x3d;        // 缓降器位置控制
    public static final byte LIGHT = (byte) 0x29;                     // 开灯
    public static final byte BRIGHTNESS = (byte) 0x2a;                // 亮度
    public static final byte EXPLOSION_FLASH = (byte) 0x2b;           // 爆闪
    public static final byte RED_BLUE_FLASH = (byte) 0x2e;            // 红蓝爆闪
    public static final byte NOTIFY_RE_FILENAME = (byte) 0x15;          // 提示传文件成功
    public static final byte PLAY_REMOTE_AUDIO_BY_INDEX = (byte) 0x99;  // 发送索引播放音频

    public static final byte HEART_BEAT = (byte) 0x50;  // Heartbeat 心跳包

//    public static final byte SERVO_UP = (byte) 0x30;                  // 舵机 up
//    public static final byte SERVO_DOWN = (byte) 0x30;                // 舵机 down
//    public static final byte SERVO_RETURN = (byte) 0x30;              // 舵机 回中

    public static final byte DESCENT = (byte) 0x10; //索降器接收msg1指令

}
