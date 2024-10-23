package java.com.example.ground_station.data.model;

public class AudioModel {
    private String audioFileName;
    private String audioFilePath;
    private boolean isPlaying;

    public AudioModel(String audioFileName, String audioFilePath, boolean isPlaying) {
        this.audioFileName = audioFileName;
        this.audioFilePath = audioFilePath;
        this.isPlaying = isPlaying;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public void setAudioFileName(String audioFileName) {
        this.audioFileName = audioFileName;
    }

    public String getAudioFilePath() {
        return audioFilePath;
    }

    public void setAudioFilePath(String audioFilePath) {
        this.audioFilePath = audioFilePath;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}

