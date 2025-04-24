package java.com.example.ground_station.presentation.fun.file;

import android.os.Environment;
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
        String palyPath = PathConstants.getPalyWebPath();
        int index = FileInfoUtils.getUploadAudioFileIndex();
        String hz = "";
        int index1 = name.lastIndexOf(".");
        if (index1 >= 0) {
            hz = name.substring(index1);
        }
        return palyPath + index + hz;
    }

    public static String getText2AudioFileName(String showFileName) {
        String palyPath = PathConstants.getPalyWebPath();
        String originName = FileInfoUtils.getAudioOriginName(showFileName);
        if (TextUtils.isEmpty(originName)) {
            int index = FileInfoUtils.getText2AudioFileIndex();
            String hz = ".mp3";
            return palyPath + "0" + index + hz;
        } else {
            return palyPath + originName;
        }
    }

    public static String getPalyWebPath() {
        String rootPath = getWebdavRootPath();
        return rootPath + "play/";
    }


    public static String getTextAudioWebPath() {
        return getPalyWebPath() + "textAudio/";
    }

    public static String creatTextAudioFileName(String str) {
        return getTextAudioWebPath() + str;
    }

    public static String creatUploadAudioFileName(String str) {
        return getLoadAudioWebPath() + str;
    }

    public static String getLoadAudioWebPath() {
        return getPalyWebPath() + "upLoadAudio/";
    }

    public static @NonNull String getWebdavRootPath() {
        ShoutcasterConfig.DeviceInfo mediaInfo = ShoutcasterConfig.getMediaInfo();
        String rootPath = "http://" + mediaInfo.getIp() + ":5000/";
        //String rootPath = "http://" + mediaInfo.getIp() + "/";
        return rootPath;
    }

    public static byte[] mapAudioPlayWebPath(String path) {
        if (path != null) {
            if (path.startsWith(getWebdavRootPath())) {
                path = path.substring(getWebdavRootPath().length());
            }
            if (path.startsWith("/play")) {
                path = path.substring("/play".length());
            }
            if (path.startsWith("play")) {
                path = path.substring("play".length());
            }
            return path.getBytes();
        }
        return new byte[]{};
    }

    public static File getMusicDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
    }

    public static File getTextAudioDir() {
        return new File(getMusicDir(), "textAudio");
    }

    public static File getUploadAudioDir() {
        return new File(getMusicDir(), "upLoadAudio");
    }

}
