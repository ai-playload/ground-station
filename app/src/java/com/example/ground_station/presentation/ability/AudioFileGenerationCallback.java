package java.com.example.ground_station.presentation.ability;

public interface AudioFileGenerationCallback {
    void onAudioFileGenerationStart();
    void onAudioFileGenerationEnd(String fileName);
}

