package java.com.example.ground_station.presentation.fun.file;

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


    public static String getPalyPath() {
        String rootPath = getWebdavRootPath();
        return rootPath + "data/play/";
    }

    private static @NonNull String getWebdavRootPath() {
        ShoutcasterConfig.DeviceInfo mediaInfo = ShoutcasterConfig.getMediaInfo();
        String rootPath = "http://" + mediaInfo.getIp() + "/webdav/";
        return rootPath;
    }
}
