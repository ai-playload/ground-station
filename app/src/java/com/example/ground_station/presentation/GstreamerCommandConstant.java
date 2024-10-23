package java.com.example.ground_station.presentation;

public class GstreamerCommandConstant {
    public static final String SHOUTT_COMMAND = "autoaudiosrc ! audioconvert ! audioresample ! opusenc ! rtpopuspay ! udpsink host=%s port=%d"; //喊话命令
    public static final String TEXT_TO_SPEECH_COMMAND = "filesrc location=%s ! decodebin ! audioconvert ! audioresample ! audio/x-raw,rate=8000 ! opusenc ! rtpopuspay ! udpsink host=%s port=%d"; //喊话命令
}
