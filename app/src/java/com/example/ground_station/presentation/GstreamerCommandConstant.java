package java.com.example.ground_station.presentation;

public class GstreamerCommandConstant {
//    autoaudiosrc ! audioconvert ! volume volume=3.0 !  audioresample ! opusenc ! rtpopuspay ! udpsink host=8080 port=1234
    public static final String SHOUTT_COMMAND_BS = "autoaudiosrc ! audioconvert ! volume volume=%s ! audioresample ! opusenc ! rtpopuspay ! udpsink host=%s port=%d"; //喊话命令
    public static final String SHOUTT_COMMAND = "autoaudiosrc ! audioconvert ! audioresample ! opusenc ! rtpopuspay ! udpsink host=%s port=%d"; //喊话命令
    public static final String TEXT_TO_SPEECH_COMMAND = "filesrc location=%s ! decodebin ! audioconvert ! audioresample ! audio/x-raw,rate=8000 ! opusenc ! rtpopuspay ! udpsink host=%s port=%d"; //喊话命令
}
