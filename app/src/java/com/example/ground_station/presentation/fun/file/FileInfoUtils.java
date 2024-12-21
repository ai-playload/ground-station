package java.com.example.ground_station.presentation.fun.file;

import android.text.TextUtils;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
            audioModelList.add(new AudioModel(fileName, filePath, false));
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
     * @param audioFilePath
     * @return
     */
    public static boolean isBjs(String audioFilePath) {
        return TextUtils.equals(String.valueOf(SocketConstant.PLAY_ALARM), audioFilePath);
    }
}
