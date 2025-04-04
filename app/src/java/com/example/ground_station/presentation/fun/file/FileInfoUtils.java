package java.com.example.ground_station.presentation.fun.file;

import android.text.TextUtils;

import androidx.core.math.MathUtils;

import com.blankj.utilcode.util.SPUtils;
import com.thegrizzlylabs.sardineandroid.DavResource;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.CannotWriteException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.MultimediaInfo;

public class FileInfoUtils {

    public static List<FileInfo> map(List<DavResource> list) {
        ArrayList<FileInfo> data = new ArrayList<>(list.size());
        for (DavResource davResource : list) {
            FileInfo fileInfo = new FileInfo(davResource);
            File file = PathConstants.getInstallApkFile(fileInfo.info.getName());
            fileInfo.loadFile = file;
            data.add(fileInfo);
        }
        return data;
    }

    public static List<AudioModel> getAllRemoteAudioToAudioModel(List<DavResource> list) {
        List<AudioModel> audioModelList = new ArrayList<>();
        for (DavResource davResource : list) {
            String fileName = davResource.getName();
            String filePath = davResource.getPath();
            String audioShowName = FileInfoUtils.getAudioShowName(fileName);
            if (!TextUtils.isEmpty(audioShowName)) {
                audioShowName = "语音内容：" + audioShowName;
            }
            audioModelList.add(new AudioModel(fileName, filePath, false, audioShowName));
        }
        return audioModelList;
    }

    public static List<AudioModel> getBjs() {
        List<AudioModel> audioModelList = new ArrayList<>();
        //警报声
        for (int i = 0; i < 3; i++) {
            audioModelList.add(new AudioModel("警报声" + (i + 1),
                    String.valueOf(SocketConstant.PLAY_ALARM), false));
        }
        return audioModelList;
    }


    /**
     * 是否是报警声
     *
     * @param audioFilePath
     * @return
     */
    public static boolean isBjs(String audioFilePath) {
        return TextUtils.equals(String.valueOf(SocketConstant.PLAY_ALARM), audioFilePath);
    }

    public static void writeTitle(File file, String aliasName) {
        try {
            addTag(file, aliasName);
        } catch (Exception e) {
            String message = e.getMessage();
        }
    }

    private static void addTag(File m4aFile, String aliasName) throws CannotWriteException, TagException, CannotReadException, InvalidAudioFrameException, ReadOnlyFileException, IOException {

        Encoder encoder = new Encoder();
        MultimediaInfo m = null;
        try {
            m = encoder.getInfo(m4aFile);

            long duration = m.getDuration();// 时长，单位：毫秒
            long fileTime = duration > 0 ? duration / 1000 : 0; // 转换为秒，毫秒不要了，简化处理
        } catch (EncoderException e) {
            throw new RuntimeException(e);
        }


        AudioFile audioFile = AudioFileIO.read(m4aFile);
        // 获取标签信息
        Tag tag = audioFile.getTag();
        if (tag == null) {
            tag = audioFile.createDefaultTag();
        }
        //  获取文件名
        String fileName = m4aFile.getName();
//        int lastDotIndex = fileName.lastIndexOf('.');
//
//        if (lastDotIndex > 0) {
//            // 存在文件后缀，截取文件名的部分
//            fileName.substring(0, lastDotIndex);
//        }

        // 修改标题
        tag.setField(FieldKey.TITLE, aliasName);
        tag.setField(FieldKey.ARTIST, fileName);
        audioFile.setTag(tag);
        // 保存修改
//        AudioFileIO.write(audioFile);
        audioFile.commit();
    }

    public static int file2Payload(File file) {
        String name = file.getName();
        return file2Payload(name);
    }

    public static int file2Payload(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index >= 0) {
            fileName = fileName.substring(0, index);
        }
        try {
            int payload = Integer.parseInt(fileName);
            return payload;
        } catch (Exception e) {
        }
        return -1;
    }

    private static final String AUDIO_INFO_MAP = "audio-name-map";

    public static void putAudioInfoMap(String path, String showFileName) {
        int index = path.lastIndexOf("/") + 1;
        if (index >= 1) {
            String originName = path.substring(index);
            SPUtils.getInstance(AUDIO_INFO_MAP).put(originName, showFileName);
        }
    }

    public static String getText2AduioFileName(String text) {
        return text + ".mp3";
    }

    public static String getAudioShowName(String originFileName) {
        return SPUtils.getInstance(AUDIO_INFO_MAP).getString(originFileName);
    }

    public static String getAudioOriginName(String showFileName) {
        Map<String, ?> all = SPUtils.getInstance(AUDIO_INFO_MAP).getAll();
        for (Map.Entry<String, ?> entry : all.entrySet()) {
            Object value = entry.getValue();
            if (Objects.equals(value, showFileName)) {
                return entry.getKey();
            }
        }
        return "";
    }

    private static int UP_LOAD_FILE_INDEX = SPUtils.getInstance().getInt("UP_LOAD_FILE_INDEX", 9);

    public static int getUploadAudioFileIndex() {
        int i = (UP_LOAD_FILE_INDEX + 1) % 10;
        i = UP_LOAD_FILE_INDEX = MathUtils.clamp(i, 0, 9);
        SPUtils.getInstance().put("TEXT_2_AUDIO_FILE_INDEX", UP_LOAD_FILE_INDEX);
        return i + 10;
    }

    private static int TEXT_2_AUDIO_FILE_INDEX = SPUtils.getInstance().getInt("TEXT_2_AUDIO_FILE_INDEX", -1);

    public static int getText2AudioFileIndex() {
        int i = (TEXT_2_AUDIO_FILE_INDEX + 1) % 10;
        i = TEXT_2_AUDIO_FILE_INDEX = MathUtils.clamp(i, 0, 9);
        SPUtils.getInstance().put("TEXT_2_AUDIO_FILE_INDEX", TEXT_2_AUDIO_FILE_INDEX);
        return i;
    }
}