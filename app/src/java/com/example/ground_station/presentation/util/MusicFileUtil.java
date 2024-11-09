package java.com.example.ground_station.presentation.util;

import android.os.Environment;
import android.util.Log;

import com.iflytek.aikitdemo.tool.ThreadExtKt;

import java.com.example.ground_station.data.model.AudioModel;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

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

    public static List<AudioModel> getAllRemoteAudioToAudioModel(List<String> filePaths) {
        List<AudioModel> audioModelList = new ArrayList<>();

        for (String filePath : filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            audioModelList.add(new AudioModel(fileName, filePath, false));
        }
        return audioModelList;
    }

    public static List<AudioModel> getRemoteAudioList(String response) {
        if (response != null && response.length() > 1) {
            // 查找第一个 '[' 和最后一个 ']' 的位置
            int firstIndex = response.indexOf("[");
            int lastIndex = response.lastIndexOf("]");

            // 如果 '[' 和 ']' 都存在，且顺序正确
            if (firstIndex != -1 && lastIndex != -1 && firstIndex < lastIndex) {
                // 截取从 '[' 到 ']' 之间的内容
                response = response.substring(firstIndex, lastIndex + 1);  // 保留到最后的 ']'
            }

            Log.d(TAG, "Received response after modification: " + response);

            try {
                GsonParser gsonParser = new GsonParser();
                return getAllRemoteAudioToAudioModel(gsonParser.parseAudioFileList(response));
            } catch (Exception e) {
                Log.e(TAG, " error: " + e);
            }
        }

        return null;
    }
}
