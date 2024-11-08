package java.com.example.ground_station.presentation.fun.file;

import android.content.Context;
import android.util.Log;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.thegrizzlylabs.sardineandroid.DavResource;
import com.thegrizzlylabs.sardineandroid.Sardine;
import com.thegrizzlylabs.sardineandroid.impl.OkHttpSardine;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Objects;

public class SardineHelper {

    private static final String TAG = "SardineHelper";
    public static String HOST_URL = "http://81.70.35.199";

    private static Sardine sardine;
    private final Context context;
    private String path;

    public SardineHelper(Context context) {
        this.context = context;
    }

    public String getPath() {
        return path;
    }

    public void upLoad(String path, byte[] data) {
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<Objects>() {

            @Override
            public Objects doInBackground() throws Throwable {
                getSardine().put(path, data);
                return null;
            }

            @Override
            public void onSuccess(Objects result) {

            }
        });
    }

    //将文件转换成Byte数组
    public static byte[] getBytesByFile(String pathStr) {
        File file = new File(pathStr);
        try {
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream(1000);
            byte[] b = new byte[1000];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            byte[] data = bos.toByteArray();
            bos.close();
            return data;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void list(String p, SardineCallBack callBack) {
        this.path = checkPath(p);
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<List<DavResource>>() {
            @Override
            public List<DavResource> doInBackground() throws Throwable {
                List<DavResource> list = getSardine().list(path);
                if (list != null && list.size() > 1) {
                    DavResource davResource = list.get(0);
                    String path2 = davResource.getPath();
                    list.remove(0);
                    if (Objects.equals(path2, "")) {
                    }
                }
                return list;
            }

            @Override
            public void onSuccess(List<DavResource> result) {
                callBack.getResult(result);
            }
        });
    }

    private static String checkPath(String path) {
        if (!path.contains(HOST_URL)) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            path = HOST_URL + path;
        }
        return path;
    }

    private static Sardine getSardine() {
        synchronized (SardineHelper.class) {
            if (sardine == null) {
                sardine = new OkHttpSardine();
                sardine.setCredentials("sftp", "456sftp", true);
            }
            return sardine;
        }
    }

    public String getParentPath() {
        return getParentPath(HOST_URL);
    }

    public String getParentPath(String rootPath) {
        if (StringUtils.isEmpty(rootPath)) {
            rootPath = HOST_URL;
        }
        String path = getPath();
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 2);
        }
        int index = path.lastIndexOf("/");
        if (index >= 0) {
            String s = path.substring(0, index);
            if (s.contains(rootPath)) {
                return s;
            } else {
                return rootPath;
            }
        }
        return null;
    }

    public void downLoad(String p, FileLoadCallBack callBack) {
        this.path = checkPath(p);
        ThreadUtils.executeByIo(new ThreadUtils.SimpleTask<File>() {
            @Override
            public File doInBackground() throws Throwable {
                List<DavResource> list = getSardine().list(path);
                DavResource davResource = list.get(0);
                Long fileSize = davResource.getContentLength();
                Log.d(TAG, "开始fileSize:" + fileSize);
                File file = PathConstants.getInstallApkFile(davResource.getName());

                InputStream is = getSardine().get(path);
                FileHelper.createWriteBuilder()
                        .setFileSize(fileSize)
                        .setInputStream(is)
                        .setOutFile(file)
                        .setCallBack(callBack)
                        .build();
                // TODO: 2024/11/4 可以考虑实现断点续传
                return file;
            }

            @Override
            public void onSuccess(File result) {
            }
        });
    }


}
