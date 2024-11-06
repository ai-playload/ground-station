package java.com.example.ground_station.presentation.screen;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.blankj.utilcode.util.NetworkUtils;
import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.com.example.ground_station.data.crash.CrashInfoListActivity;
import java.com.example.ground_station.data.model.CommonConstants;
import java.com.example.ground_station.data.model.SendFunctionProvider;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.service.GroundStationService;
import java.com.example.ground_station.data.socket.ConnectionCallback;
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
    private EditText controllerIpEditText;
    private EditText controllerPortEditText;
    private EditText cloudLightIpEditText;
    private EditText cloudLightPortEditText;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GroundStationService.LocalBinder binder = (GroundStationService.LocalBinder) service;
            groundStationService = binder.getService();
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
        setContentView(R.layout.activity_new_main);

        Intent serviceIntent = new Intent(this, GroundStationService.class);
        startService(serviceIntent);
        bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        initView();
    }

    private void initView() {
        TextView shoutBtn = findViewById(R.id.shout_connect_btn);
        TextView cloudBtn = findViewById(R.id.cloud_connect_btn);
        TextView controllerBtn = findViewById(R.id.controller_connect_btn);

        TextView versionTv = findViewById(R.id.version_tv);
        versionTv.setText("版本号: " + getVersionName());

        shoutIpEditText = findViewById(R.id.shout_ip_input);
        shoutPortEditText = findViewById(R.id.shout_port_input);
        controllerIpEditText = findViewById(R.id.controller_ip_input);
        controllerPortEditText = findViewById(R.id.controller_port_input);
        cloudLightIpEditText = findViewById(R.id.cloud_light_ip_input);
        cloudLightPortEditText = findViewById(R.id.cloud_light_port_input);

        getSpValueToEditText();

        View.OnClickListener click = v -> {
            requestPermissions();
//            requestFloatingPermissionsAndShow();
//            checkInputsAndProceed();
        };

        shoutBtn.setOnClickListener(click);
        cloudBtn.setOnClickListener(click);
        controllerBtn.setOnClickListener(click);

        findViewById(R.id.rfcBtn).setOnClickListener(view -> {
            showFloatingWindow();
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
        ((ViewGroup) findViewById(R.id.testParentView)).setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);

    }

    private void getSpValueToEditText() {
        // 从 SharedPreferences 中读取并设置 EditText 的值
        String savedShoutIp = SPUtil.INSTANCE.getString("shoutcaster_ip", "");
        String savedShoutPort = SPUtil.INSTANCE.getString("shoutcaster_port", "");
        String savedControllerIp = SPUtil.INSTANCE.getString("controller_ip", "");
        String savedControllerPort = SPUtil.INSTANCE.getString("controller_port", "");
        String savedCloudLightIp = SPUtil.INSTANCE.getString("cloud_light_ip", "");
        String savedCloudLightPort = SPUtil.INSTANCE.getString("cloud_light_port", "");

        if (!savedShoutIp.isEmpty()) {
            shoutIpEditText.setText(savedShoutIp);
        }
        if (!savedShoutPort.isEmpty()) {
            shoutPortEditText.setText(savedShoutPort);
        }
        if (!savedControllerIp.isEmpty()) {
            controllerIpEditText.setText(savedControllerIp);
        }
        if (!savedControllerPort.isEmpty()) {
            controllerPortEditText.setText(savedControllerPort);
        }
        if (!savedCloudLightIp.isEmpty()) {
            cloudLightIpEditText.setText(savedCloudLightIp);
        }
        if (!savedCloudLightPort.isEmpty()) {
            cloudLightPortEditText.setText(savedCloudLightPort);
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

    private void checkInputsAndProceed() {
        // 获取输入内容
        String shoutIp = shoutIpEditText.getText().toString().trim();
        String shoutPort = shoutPortEditText.getText().toString().trim();
        String controllerIp = controllerIpEditText.getText().toString().trim();
        String controllerPort = controllerPortEditText.getText().toString().trim();
        String cloudLightIp = cloudLightIpEditText.getText().toString().trim();
        String cloudLightPort = cloudLightPortEditText.getText().toString().trim();

        // 检查输入是否为空
        if (shoutIp.isEmpty()) {
            shoutIpEditText.setError("喊话器 IP 不能为空");
            return;
        }

        if (shoutPort.isEmpty()) {
            shoutPortEditText.setError("喊话器 端口不能为空");
            return;
        }

        if (controllerIp.isEmpty()) {
            controllerIpEditText.setError("控制器 IP 不能为空");
            return;
        }

        if (controllerPort.isEmpty()) {
            controllerPortEditText.setError("控制器端口不能为空");
            return;
        }

        if (cloudLightIp.isEmpty()) {
            cloudLightIpEditText.setError("云台灯 IP 不能为空");
            return;
        }

        if (cloudLightPort.isEmpty()) {
            cloudLightPortEditText.setError("云台灯端口不能为空");
            return;
        }

        // 如果所有输入都不为空，执行你的操作
        proceedWithValidInputs(shoutIp, shoutPort, controllerIp, controllerPort, cloudLightIp, cloudLightPort);
    }

    private void proceedWithValidInputs(String shoutIp, String shoutPort, String controllerIp, String controllerPort, String cloudLightIp, String cloudLightPort) {
        // 将输入内容传递给服务或其他操作
        ShoutcasterConfig.DeviceInfo shoutcasterInfo = new ShoutcasterConfig.DeviceInfo(shoutIp, Integer.parseInt(shoutPort));
        ShoutcasterConfig.DeviceInfo controllerInfo = new ShoutcasterConfig.DeviceInfo(controllerIp, Integer.parseInt(controllerPort));
        ShoutcasterConfig.DeviceInfo cloudLightInfo = new ShoutcasterConfig.DeviceInfo(cloudLightIp, Integer.parseInt(cloudLightPort));
        ShoutcasterConfig config = new ShoutcasterConfig(shoutcasterInfo, controllerInfo, cloudLightInfo);

        // 将 shoutcaster 的 IP 和端口保存到 SharedPreferences
        SPUtil.INSTANCE.putBase("shoutcaster_ip", shoutIp);
        SPUtil.INSTANCE.putBase("shoutcaster_port", shoutPort);

        // 将 controller 的 IP 和端口保存到 SharedPreferences
        SPUtil.INSTANCE.putBase("controller_ip", controllerIp);
        SPUtil.INSTANCE.putBase("controller_port", controllerPort);

        // 将 cloud light 的 IP 和端口保存到 SharedPreferences
        SPUtil.INSTANCE.putBase("cloud_light_ip", cloudLightIp);
        SPUtil.INSTANCE.putBase("cloud_light_port", cloudLightPort);

        groundStationService.setShoutcasterConfig(config, new ConnectionCallback() {
            @Override
            public void onConnectionSuccess() {
                requestFloatingPermissionsAndShow();
//                Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionFailure(Exception e) {
//                Toast.makeText(MainActivity.this, "连接失败，正在重连", Toast.LENGTH_SHORT).show();
            }
        });
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
                    moveTaskToBack(true);

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
                checkInputsAndProceed();
            }

            @Override
            public void onCopyFailure(Exception e) {

            }
        });
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
                moveTaskToBack(true);
            }
        } else {
            // Android 6.0 以下不需要权限
            showFloatingWindow();
            moveTaskToBack(true);
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