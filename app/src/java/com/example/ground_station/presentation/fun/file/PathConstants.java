package java.com.example.ground_station.presentation.fun.file;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;
import com.example.ground_station.BuildConfig;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.io.File;

public class PathConstants {
    public static final String TAG_TEST_FUN = "funTest";
    public static final String TAG_TEST_HISTORY = "historyTest";
    public static final String TAG_ONLINE = "online";

    public static File getInstallApkParentFile() {
        File cacheDir = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            cacheDir = Utils.getApp().getDataDir();
        }
//        Utils.getApp().getDataDir()
        String parent = "/apk/";
        parent += BuildConfig.DEBUG ? "debug" : "release";
        parent += "/";
        return new File(cacheDir, parent);
    }

    public static File getInstallApkFile(String name) {
        return new File(getInstallApkParentFile(), name);
    }


    public static String getUploadAudioPalyPath(String name) {
        String palyPath = PathConstants.getPalyPath();
        int index = FileInfoUtils.getUploadAudioFileIndex();
        String hz = "";
        int index1 = name.lastIndexOf(".");
        if (index1 >= 0) {
            hz = name.substring(index1);
        }
        return palyPath + index + hz;
    }

    public static String getText2AudioFileName(String showFileName) {
        String palyPath = PathConstants.getPalyPath();
        String originName = FileInfoUtils.getAudioOriginName(showFileName);
        if (TextUtils.isEmpty(originName)) {
            int index = FileInfoUtils.getText2AudioFileIndex();
            String hz = ".mp3";
            return palyPath + "0" + index + hz;
        } else {
            return  palyPath + originName;
        }
    }

    public static String getPalyPath() {
        String rootPath = getWebdavRootPath();
        return rootPath + "/play/";
//        return rootPath + "data/play/";
    }

    private static @NonNull String getWebdavRootPath() {
        ShoutcasterConfig.DeviceInfo mediaInfo = ShoutcasterConfig.getMediaInfo();
        String rootPath = "http://" + mediaInfo.getIp() + ":5000/";
//        String rootPath = "http://" + mediaInfo.getIp() + "/";
        return rootPath;
    }
}
