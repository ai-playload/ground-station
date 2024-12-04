package java.com.example.ground_station.presentation.screen;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.NetworkUtils;
import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;
import com.lzf.easyfloat.EasyFloat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.com.example.ground_station.data.crash.CrashInfoListActivity;
import java.com.example.ground_station.data.model.CommonConstants;
import java.com.example.ground_station.data.model.SendFunctionProvider;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.service.GroundStationService;
import java.com.example.ground_station.data.socket.Clien;
import java.com.example.ground_station.data.socket.ConnectionCallback;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.com.example.ground_station.data.socket.UdpClientHelper;
import java.com.example.ground_station.data.utils.Utils;
import java.com.example.ground_station.presentation.ability.IFlytekAbilityManager;
import java.com.example.ground_station.presentation.util.AssetCopierUtil;
import java.com.example.ground_station.presentation.util.FilePathUtils;
import java.com.example.ground_station.presentation.util.TCPFileUploader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    public static final int REQUEST_CODE = 100;
    private GroundStationService groundStationService;
    private boolean isBound = false;
    private EditText shoutIpEditText;
    private EditText shoutPortEditText;
    private EditText descentEdIp;
    private EditText descentEdPort;
    private EditText cloudLightIpEditText;
    private EditText cloudLightPortEditText;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GroundStationService.LocalBinder binder = (GroundStationService.LocalBinder) service;
            groundStationService = binder.getService();
            groundStationService.setShoutcasterConfig(new ConnectionCallback() {
                @Override
                public void onConnectionSuccess() {

                }

                @Override
                public void onConnectionFailure(Exception e) {

                }
            });
            isBound = true;

            requestPermissions();
            Log.d(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_new_main_gs);

        Intent serviceIntent = new Intent(this, GroundStationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        initView();
    }

    private void initView() {
        TextView versionTv = findViewById(R.id.version_tv);

        initMedia();
        initLight();
        initDescent();

        if (BuildConfig.DEBUG) {
            versionTv.setText("版本号: " + getVersionName() + "\r\nIP：" + NetworkUtils.getIPAddress(true));
            ((ImageView) findViewById(R.id.bgIv)).setImageDrawable(null);
            findViewById(R.id.rfcBtn).setOnClickListener(view -> {
//                showFloatingWindow();
                requestFloatingPermissionsAndShow();
            });
            findViewById(R.id.crashLogView).setOnClickListener(view -> {
                startActivity(new Intent(this, CrashInfoListActivity.class));
            });
            findViewById(R.id.createCrashTestView).setOnClickListener(view -> {
                //创建崩溃日志
                int i = 3 / 0;
            });

            findViewById(R.id.testBtn).setOnClickListener(view -> {
                startActivity(new Intent(MainActivity.this, TestInstructActivity.class));
            });

            findViewById(R.id.updateBtn).setOnClickListener(view -> {
                //更新\n软件
                startActivity(new Intent(MainActivity.this, SettingActivity.class));
            });
        }
        ((ViewGroup) findViewById(R.id.testParentView)).setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
    }

    private void initMedia() {
        shoutIpEditText = findViewById(R.id.shout_ip_input);
        shoutPortEditText = findViewById(R.id.shout_port_input);
        initIpAndPort(shoutIpEditText, shoutPortEditText, ShoutcasterConfig.getMediaInfo());

        TextView clientBtn = findViewById(R.id.shout_connect_btn);
        clientBtn.setOnClickListener(view -> {
            requestPermissions();
            String[] info = Utils.checkIpProt(shoutIpEditText, shoutPortEditText, "喊话器");
            updateLoadInfoAndConnect(info, ShoutcasterConfig.getDescentInfo(), SocketClientHelper.getMedia().getClient());
        });
    }

    private void initDescent() {
        descentEdIp = findViewById(R.id.controller_ip_input);
        descentEdPort = findViewById(R.id.controller_port_input);
        initIpAndPort(descentEdIp, descentEdPort, ShoutcasterConfig.getDescentInfo());

        TextView clientBtn = findViewById(R.id.controller_connect_btn);
        clientBtn.setOnClickListener(view -> {
            requestPermissions();
            String[] info = Utils.checkIpProt(descentEdIp, descentEdPort, "索降");
            updateLoadInfoAndConnect(info, ShoutcasterConfig.getDescentInfo(), SocketClientHelper.getDessent().getClient());
        });
    }

    private void initLight() {
        //云台灯
        cloudLightIpEditText = findViewById(R.id.cloud_light_ip_input);
        cloudLightPortEditText = findViewById(R.id.cloud_light_port_input);
        initIpAndPort(cloudLightIpEditText, cloudLightPortEditText, ShoutcasterConfig.getCloudLightInfo());
        TextView cloudBtn = findViewById(R.id.cloud_connect_btn);
        cloudBtn.setOnClickListener(view -> {
            requestPermissions();
            String[] info = Utils.checkIpProt(cloudLightIpEditText, cloudLightPortEditText, "云台灯");
            updateLoadInfoAndConnect(info, ShoutcasterConfig.getCloudLightInfo(), UdpClientHelper.getInstance().getClient());
        });
    }

    private void updateLoadInfoAndConnect(String[] ipPortInfo, ShoutcasterConfig.DeviceInfo infoLoad, Clien client) {
        if (ipPortInfo != null) {
            // 将 和端口保存到 SharedPreferences
            infoLoad.setIp(ipPortInfo[0]);
            infoLoad.setPort(ipPortInfo[1]);
            // 更新 并连接
            client.update(ipPortInfo[0], ipPortInfo[1]);
        }
    }

    private void initIpAndPort(EditText edIp, EditText edPort, ShoutcasterConfig.DeviceInfo info) {
        String loadIp = info.getIp();
        if (!TextUtils.isEmpty(loadIp)) {
            edIp.setText(loadIp);
        }
        int loadPort = info.getPort();
        if (loadPort != 0) {
            edPort.setText(String.valueOf(loadPort));
        }
    }

    private String getVersionName() {
        String versionName = "";
        int versionCode = 0;

        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);

            versionName = packageInfo.versionName;
            versionCode = packageInfo.versionCode;  // 旧版的 versionCode 在新版本的 Android 中仍然适用

            Log.d(TAG, "Version Name: " + versionName + " Version Code: " + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    private void requestPermissions() {
        List<String> permissionsList = new ArrayList<>();
        permissionsList.add(Manifest.permission.RECORD_AUDIO);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissionsList.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissionsList.add(Manifest.permission.READ_MEDIA_AUDIO);
            permissionsList.add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
        } else {
            permissionsList.add(Manifest.permission.READ_MEDIA_IMAGES);
            permissionsList.add(Manifest.permission.READ_MEDIA_VIDEO);
            permissionsList.add(Manifest.permission.READ_MEDIA_AUDIO);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }

        ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[0]), 100);
//        PermissionUtils.permission(permissionsList.toArray(new String[0])).request();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allGranted = true;
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            copyAssets();
        }

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
            } else {
                // Permission is denied
                Toast.makeText(this, permissions[i] + "被拒绝了，请在应用设置里打开权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CommonConstants.AUDIO_REQUEST_CODE && resultCode == RESULT_OK) {
            try {
                Uri uri = data.getData();
                String filePath = FilePathUtils.getPath(this, uri);

                if (filePath != null) {
                    // 处理得到的文件路径
                    Log.d("SelectedAudio", "文件路径: " + filePath + " uri : " + uri);
                    uploadAudioFile(filePath);
                } else {
                    Log.e("SelectedAudio", "无法获取文件路径");
                }
            } catch (Exception e) {
                // 错误处理
                Log.e("SFTP", "Error during file upload", e);

            }
        }
        if (requestCode == REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    // 权限已授予，可以显示悬浮窗
                    showFloatingWindow();

                } else {
                    // 权限未授予，提示用户
                    Toast.makeText(this, "需要悬浮窗权限", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void uploadAudioFile(String filePath) {
        TCPFileUploader uploader = new TCPFileUploader();
        uploader.uploadFile(this, filePath, (progress -> {

        }));
    }

    private void copyAssets() {
        AssetCopierUtil.copyAssetsToSDCard(this, "iflytekAikit", Environment.getExternalStorageDirectory() + "/iflytekAikit", new AssetCopierUtil.CopyCallback() {
            @Override
            public void onCopySuccess() {
                Log.d(TAG, "onCopySuccess.....");
                IFlytekAbilityManager.getInstance().initializeSdk(MainActivity.this);

                connectMedia();
                connectLight();
                connectDescent();
            }

            @Override
            public void onCopyFailure(Exception e) {
            }
        });
    }

    private static void connectLight() {
        ShoutcasterConfig.DeviceInfo info = ShoutcasterConfig.getCloudLightInfo();
        UdpClientHelper.getInstance().getClient().update(info.getIp(), info.getPort());
    }

    private static void connectMedia() {
        ShoutcasterConfig.DeviceInfo info = ShoutcasterConfig.getMediaInfo();
        SocketClientHelper.getMedia().getClient().update(info.getIp(), info.getPort());
    }

    private static void connectDescent() {
        ShoutcasterConfig.DeviceInfo info = ShoutcasterConfig.getDescentInfo();
        SocketClientHelper.getDessent().getClient().update(info.getIp(), info.getPort());
    }

    private void requestFloatingPermissionsAndShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                // 权限已授予，可以显示悬浮窗
                showFloatingWindow();
            }
        } else {
            // Android 6.0 以下不需要权限
            showFloatingWindow();
        }
    }

    public void showFloatingWindow() {
        if (isBound) {
            groundStationService.showFloatingWindow(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        EasyFloat.dismiss("audio_tag");
        EasyFloat.dismiss("text_to_speech_tag");
        EasyFloat.dismiss("audio_file_tag");
        EasyFloat.dismiss("detector_alarm_tag");
        EasyFloat.dismiss("light_tag");
        EasyFloat.dismiss("settings_tag");
        EasyFloat.dismiss("floating_window");
        if (isBound) {
            unbindService(connection);
            isBound = false;
        }
        Intent serviceIntent = new Intent(this, GroundStationService.class);
        stopService(serviceIntent);
        EventBus.getDefault().unregister(this);
        Log.d(TAG, "Service stopped and unbound");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receive(SendFunctionProvider event) {
        groundStationService.sendInstruct(event.msgId2, event.ps);

    }
}