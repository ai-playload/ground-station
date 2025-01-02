package java.com.example.ground_station.data.model;

import androidx.room.util.StringUtil;

import com.blankj.utilcode.util.StringUtils;

public class AudioModel {
    private String showName;
    private String audioFileName;
    private String audioFilePath;
    private boolean isPlaying;
    public boolean selected;
    private boolean deleteLoading;

    public AudioModel(String audioFileName, String audioFilePath, boolean isPlaying) {
        this.audioFileName = audioFileName;
        this.audioFilePath = audioFilePath;
        this.isPlaying = isPlaying;
    }

    public AudioModel(String audioFileName, String audioFilePath, boolean isPlaying, String showName) {
        this.audioFileName = audioFileName;
        this.audioFilePath = audioFilePath;
        this.isPlaying = isPlaying;
        this.showName = showName;
    }

    public String getAudioFileName() {
        return audioFileName;
    }

    public String getShowName() {
        if (!StringUtils.isEmpty(showName)) {
            return showName;
        } else {
            return audioFileName;
        }
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

    public void setDeleteLoading(boolean deleteLoading) {
        this.deleteLoading = deleteLoading;
    }

    public boolean isDeleteLoading() {
        return deleteLoading;
    }
}

