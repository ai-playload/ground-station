package java.com.example.ground_station.presentation.util;

import android.os.Environment;
import android.util.Log;

import java.com.example.ground_station.data.model.AudioModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MusicFileUtil {
    private static final String TAG = "MusicFileUtil";

    /**
     * 获取 sdcard/Music 目录下所有 MP3 文件的列表
     *
     * @return MP3 文件路径的列表
     */
    public static List<String> getAllAudioFiles() {
        List<String> audioFiles = new ArrayList<>();

        // 获取 Music 目录路径
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        // 检查目录是否存在并且是一个目录
        if (musicDir.exists() && musicDir.isDirectory()) {
            // 遍历目录中的文件
            File[] files = musicDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    // 检查是否是 MP3 或 WAV 文件
                    if (file.isFile()) {
                        String fileName = file.getName().toLowerCase();
                        if (fileName.endsWith(".mp3") || fileName.endsWith(".wav")) {
                            // 添加 MP3 或 WAV 文件路径到列表中
                            audioFiles.add(file.getAbsolutePath());
                        }
                    }
                }
            } else {
                Log.e(TAG, "Music directory is empty or cannot be read.");
            }
        } else {
            Log.e(TAG, "Music directory does not exist or is not a directory.");
        }

        return audioFiles;
    }

    /**
     * 获取 sdcard/Music 目录下所有 MP3 文件的名称列表
     *
     * @return MP3 文件名称的列表
     */
    public static List<String> getAllMp3FileNames() {
        List<String> mp3FileNames = new ArrayList<>();

        // 获取 Music 目录路径
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        // 检查目录是否存在并且是一个目录
        if (musicDir.exists() && musicDir.isDirectory()) {
            // 遍历目录中的文件
            File[] files = musicDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".mp3")) {
                        // 添加 MP3 文件名称到列表中
                        mp3FileNames.add(file.getName());
                    }
                }
            } else {
                Log.e(TAG, "Music directory is empty or cannot be read.");
            }
        } else {
            Log.e(TAG, "Music directory does not exist or is not a directory.");
        }

        return mp3FileNames;
    }

    /**
     * 删除 sdcard/Music 目录下指定名称的音频文件
     *
     * @param audioFileName 要删除的音频文件名称
     * @return 是否删除成功
     */
    public static boolean deleteAudioFile(String audioFileName) {
        // 获取 Music 目录路径
        File musicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);

        // 确保 Music 目录存在
        if (musicDir.exists() && musicDir.isDirectory()) {
            // 构造要删除的文件的路径
            File fileToDelete = new File(musicDir, audioFileName);

            if (fileToDelete.exists() && fileToDelete.isFile()) {
                // 删除文件
                boolean isDeleted = fileToDelete.delete();
                if (isDeleted) {
                    Log.i(TAG, "File deleted successfully: " + audioFileName);
                    return true;
                } else {
                    Log.e(TAG, "Failed to delete file: " + audioFileName);
                }
            } else {
                Log.e(TAG, "File not found or not a file: " + audioFileName);
            }
        } else {
            Log.e(TAG, "Music directory does not exist or is not a directory.");
        }

        return false;
    }

    public static List<AudioModel> getAllMp3Files() {
        List<AudioModel> audioModelList = new ArrayList<>();
        List<String> filePaths = MusicFileUtil.getAllAudioFiles();
        for (String filePath : filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            audioModelList.add(new AudioModel(fileName, filePath, false));
        }
        return audioModelList;
    }
}
