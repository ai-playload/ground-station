package java.com.example.ground_station.presentation.screen;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.presentation.fun.file.FileListInfoView;
import java.com.example.ground_station.presentation.fun.file.PathConstants;
import java.com.example.ground_station.presentation.util.ViewUtils;


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

        initContentView(fliView0, "data/play/");
//        initContentView(fliView0, PathConstants.TAG_TEST_FUN);
        initContentView(fliView1, PathConstants.TAG_TEST_HISTORY);

        ViewUtils.setVisible(fliView1, false);
        ((RadioGroup) findViewById(R.id.rgBtn)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                boolean showTest = i == R.id.rb0;
                ViewUtils.setVisible(fliView0, showTest);
                ViewUtils.setVisible(fliView1, !showTest);
            }
        });
    }

    private void initContentView(FileListInfoView fv, String tag) {
        ShoutcasterConfig.DeviceInfo mediaInfo = ShoutcasterConfig.getMediaInfo();
        String rootPath = "http://" + mediaInfo.getIp() + "/webdav/";
//        String rootPath = this.getResources().getString(R.string.webdav_path) + "/apk/";
        rootPath += BuildConfig.DEBUG ? tag : PathConstants.TAG_ONLINE;
        fv.setRootPath(rootPath);
        fv.requestFileList(rootPath);
    }
}
