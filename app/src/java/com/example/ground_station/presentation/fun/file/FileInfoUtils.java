package java.com.example.ground_station.presentation.fun.file;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.com.example.ground_station.data.model.AudioModel;
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

}
