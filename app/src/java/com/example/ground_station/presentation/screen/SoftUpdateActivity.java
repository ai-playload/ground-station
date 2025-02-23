package java.com.example.ground_station.presentation.screen;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioGroup;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.presentation.fun.file.FileListInfoView;
import java.com.example.ground_station.presentation.fun.file.FilePathUtils;
import java.com.example.ground_station.presentation.fun.file.PathConstants;
import java.com.example.ground_station.presentation.fun.file.SardineCallBack;
import java.com.example.ground_station.presentation.fun.file.SardineHelper;
import java.com.example.ground_station.presentation.util.DownloadUtil;
import java.com.example.ground_station.presentation.util.ViewUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import okhttp3.MediaType;


/**
 * 软件更新
 */
public class SoftUpdateActivity extends ComponentActivity {

    private FileListInfoView fliView0;
    private FileListInfoView fliView1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_soft_update);
        fliView0 = (FileListInfoView) findViewById(R.id.fileListInfoView0);
        fliView1 = (FileListInfoView) findViewById(R.id.fileListInfoView1);

        initContentView(fliView0, "apk/funTest/");
//        initContentView(fliView0, PathConstants.TAG_TEST_FUN);
        initContentView(fliView1, "");
//        initContentView(fliView1, PathConstants.TAG_TEST_HISTORY);

        ViewUtils.setVisible(fliView1, false);
        ((RadioGroup) findViewById(R.id.rgBtn)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                boolean showTest = i == R.id.rb0;
                ViewUtils.setVisible(fliView0, showTest);
                ViewUtils.setVisible(fliView1, !showTest);
            }
        });

//        String uploadAudioPalyPath = PathConstants.getUploadAudioPalyPath("tn1.mp3");
        runReq(null);

    }

    private void initContentView(FileListInfoView fv, String tag) {
        ShoutcasterConfig.DeviceInfo mediaInfo = ShoutcasterConfig.getMediaInfo();
        String rootPath = "http://" + mediaInfo.getIp() + "/";
//        String rootPath = this.getResources().getString(R.string.webdav_path) + "/apk/";
        rootPath += BuildConfig.DEBUG ? tag : PathConstants.TAG_ONLINE;
        fv.setRootPath(rootPath);
        fv.requestFileList(rootPath);
    }

    public void runReq(View view) {
        String url = "http://81.70.35.199:5100/piper";
        File cacheDir = this.getCacheDir();
        String outFileName = "t31.mp3";
        new DownloadUtil().download(url, cacheDir.getAbsolutePath(), outFileName, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(@Nullable File file) {
                String name = file.getName();
                long length = file.length();
                uploadAudioFile(file.getAbsolutePath());

//                SardineHelper sardineHelper = new SardineHelper(null);
//                InputStream filePathForN = null;
//                try {
//                    filePathForN = new FileInputStream(file);
//                      byte[] byteArray = FilePathUtils.toByteArray(filePathForN);
//                     sardineHelper.getSardine().put(path, byteArray);
//                } catch (Exception e) {
//                    throw new RuntimeException(e);
//                }

//                sardineHelper.upLoad(palyPath, file, aliasName, new SardineCallBack<String>() {
//                    @Override
//                    public void getResult(String s) {
//
//                    }
//                });


            }

            @Override
            public void onDownloading(int progress) {
                Log.d("onDownloading", "" + progress);
            }

            @Override
            public void onDownloadFailed(@Nullable Exception e) {

            }
        });
    }

    private void uploadAudioFile(String filePath) {
        File file = new File(filePath);
        String name = file.getName();
        String palyPath = PathConstants.getUploadAudioPalyPath(name);
        palyPath = "http://81.70.35.199/apk/funTest/" + name;
        String aliasName = name;

        SardineHelper sardineHelper = new SardineHelper(null);
        sardineHelper.upLoad(palyPath, file, aliasName, new SardineCallBack<String>() {
            @Override
            public void getResult(String s) {

            }
        });
    }
}
