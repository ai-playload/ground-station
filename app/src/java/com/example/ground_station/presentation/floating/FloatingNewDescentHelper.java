package java.com.example.ground_station.presentation.floating;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.service.carrier.CarrierMessagingService;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.TimeUtils;
import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.service.ResultCallback;
import java.com.example.ground_station.data.socket.DeviceStatus;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.utils.RecvTaskHelper;
import java.com.example.ground_station.data.utils.SendTaskHelper;
import java.com.example.ground_station.data.utils.Utils;
import java.util.LinkedHashMap;
import java.util.Map;

public class FloatingNewDescentHelper extends BaseFloatingHelper {
    private final String tag = "settings_tag";
    private final String TAG = "FloatingSettingsHelper";
    private boolean isSafetyBtnEnable = SPUtil.INSTANCE.getBoolean("sj_kg", false);
    private int circuiStatus = DeviceStatus.NORMAL;
    private final int PARACHUTE_STATUS_CLOSE = 0;
    private final int PARACHUTE_STATUS_OPEN = 1;
    private final int PARACHUTE_STATUS_UP = 2;
    private final int PARACHUTE_STATUS_DOWN = 3;
    private final int PARACHUTE_STATUS_STOP = 4;
    private static final int SPEED_MULTIPLIER = 2;
    private static final int LENGTH_MULTIPLIER = 3;
    private static final int MIN_SPEED = 1;
    private static final int MIN_LENGTH = 1;

    private int speedValue = 1;
    private int lengthValue = 1;
    private int weightValue;
    SocketClientHelper helper = SocketClientHelper.getDessent();


    private TextView jsTv;
    private TextView fsTv;
    private TextView lenghtTv;
    private TextView weightTv;
    private TextView safetyButton;
    private TextView circuitButton;


    private int calculateLengthValue(int progress) {
        return progress == 0 ? MIN_LENGTH : progress * LENGTH_MULTIPLIER;
    }

    public void showFloatingSettings(Context context, CloseCallback closeCallback) {
        if (!helper.getClient().isConnected()) {
            ShoutcasterConfig.DeviceInfo config = ShoutcasterConfig.getCloudLightInfo();
            helper.getClient().update(config.getIp(), config.getPort());
        }

        EasyFloat.with(context)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true)
                .hasEditText(true)
                .setTag(tag)
                .setLayout(R.layout.floating_new_descent, view -> {
                    if (view != null) {
                        initFloatingView(view, tag, closeCallback);
                        initView(view);
                        init();
                    }
                })
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void dragEnd(@NonNull View view) {

                    }

                    @Override
                    public void hide(@NonNull View view) {

                    }

                    @Override
                    public void show(@NonNull View view) {

                    }

                    @Override
                    public void drag(@NonNull View view, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void dismiss() {

                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void createdResult(boolean b, @Nullable String s, @Nullable View view) {

                    }
                })
                .show();
    }

    //步进长度1 - 30米，速度，1 - 20m/min
    private void initView(View view) {
        safetyButton = view.findViewById(R.id.safety_button);
        circuitButton = view.findViewById(R.id.circuit_tv);
        TextView upActionButton = view.findViewById(R.id.up_action);
        TextView downActionButton = view.findViewById(R.id.down_action);
        TextView stopActionButton = view.findViewById(R.id.stop_action);
        SeekBar speedInputView = view.findViewById(R.id.speed_progress);
        SeekBar lengthBar = view.findViewById(R.id.length_progress);

        TextView speedTv = view.findViewById(R.id.speed_tv);
        TextView lengthTv = view.findViewById(R.id.length_tv);

        lenghtTv = view.findViewById(R.id.put_line_tv);
        weightTv = view.findViewById(R.id.weight_tv);

        view.findViewById(R.id.removePeelBtn).setOnClickListener(view1 -> {
            SPUtil.INSTANCE.putBase(SocketConstant.DELETE_INIT_WEIGHT, weightValue);
            updateWeightUI();
        });
        view.findViewById(R.id.lenghtResetBtn).setOnClickListener(view1 -> {
            helper.sendInstruct(SocketConstant.SERVO_WEIGHT_RESET_LENGHT);
        });

        int savedSpeedProgress = SPUtil.INSTANCE.getInt("speed_progress", 1);
        int savedLengthProgress = SPUtil.INSTANCE.getInt("length_progress", 1);
        speedInputView.setProgress(savedSpeedProgress);
        lengthBar.setProgress(savedLengthProgress);

        // 计算并更新速度和步进长度的值
        speedValue = savedSpeedProgress;
        lengthValue = calculateLengthValue(savedLengthProgress);
        speedTv.setText(speedValue + "  档");
        lengthTv.setText(lengthValue + "  m");

        speedInputView.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SPUtil.INSTANCE.putBase("speed_progress", progress);
                speedValue = progress;
                speedTv.setText(speedValue + "  档");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        lengthBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SPUtil.INSTANCE.putBase("length_progress", progress);
                lengthValue = calculateLengthValue(progress);
                lengthTv.setText(lengthValue + "  m");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        safetyButton.setOnClickListener(v -> {
            safetyButton.setSelected(!safetyButton.isSelected());
            if (isSafetyBtnEnable) {
                helper.send(SocketConstant.PARACHUTE, PARACHUTE_STATUS_CLOSE);
            } else {
                helper.send(SocketConstant.PARACHUTE, PARACHUTE_STATUS_OPEN);
            }
            isSafetyBtnEnable = !isSafetyBtnEnable;
        });

        circuitButton.setOnClickListener(v -> {
            helper.send(SocketConstant.PARACHUTE_CONTROL, 2);
        });

        //速度确认
        view.findViewById(R.id.speedComiftBtn).setOnClickListener(view1 -> {
            helper.sendInstruct(SocketConstant.PARACHUTE_SPEED, speedInputView.getProgress());
        });

        upActionButton.setOnClickListener(v -> {
//            helper.send(SocketConstant.PARACHUTE_SPEED, speedValue);
            helper.send(SocketConstant.PARACHUTE_LENGTH, lengthValue);
        });

        downActionButton.setOnClickListener(v -> {
//            helper.send(SocketConstant.PARACHUTE_SPEED, -speedValue);
            helper.send(SocketConstant.PARACHUTE_LENGTH, -lengthValue);
        });

        //2上升 3下降 4停止
        stopActionButton.setOnClickListener(v -> {
            helper.send(SocketConstant.PARACHUTE, PARACHUTE_STATUS_STOP);
        });

        RecvTaskHelper.getInstance().setCallback(new ResultCallback<byte[]>() {
            @Override
            public void result(byte[] bytes) {
                setRevInfo(bytes);
            }
        });
// TODO: 2024/12/12
//        jsTv = view.findViewById(R.id.jsTv);
//        fsTv = view.findViewById(R.id.fsTv);
    }

    private void sendInstruct(byte msgId2, int... payload) {
        helper.send(msgId2, payload);//获取开关状态指令
    }

    private void requestSwitchInfo() {
        sendInstruct(SocketConstant.SERVO_SWITCH_STATUS);//获取开关状态指令
    }

    private void requestWeightInfo() {
        sendInstruct(SocketConstant.PARACHUTE_3C);
    }

    private void requestLenghtInfo() {
        sendInstruct(SocketConstant.PARACHUTE_3E);
    }

    private void requestParachuteStatus() {
        sendInstruct(SocketConstant.PARACHUTE_CIRCUI_STATUS);
    }

    public void init() {
        SendTaskHelper.getInstance().setSocketManager(helper);
        requestSwitchInfo();
        requestLenghtInfo();
        requestWeightInfo();
        requestParachuteStatus();
        SendTaskHelper.getInstance().addInsturd(SocketConstant.PARACHUTE_3C, SocketConstant.PARACHUTE_3E);
    }

    private void setRevInfo(byte[] bytes) {
        if (bytes != null && bytes.length >= 4 && bytes[0] == SocketConstant.HEADER) {
            byte aByte = bytes[3];
            if (aByte == SocketConstant.HEART_BEAT) {//心跳包数据
                return;
            }
            Message message = new Message();
            message.what = 0;
            Bundle bundle = new Bundle();
            bundle.putByteArray("gs", bytes);
            message.setData(bundle);
            mHandler.sendMessage(message);
        }
    }

    int t;
    android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    byte[] bytes = msg.getData().getByteArray("gs");
                    int v = bytes[4];
                    byte zl = bytes[3];
                    if (zl == SocketConstant.PARACHUTE_3E) {
                        lenghtTv.setText("当前放线长度：" + v + "m");
                    } else if (zl == SocketConstant.PARACHUTE_3C) {
                        weightValue = v;
                        updateWeightUI();
                    } else if (zl == SocketConstant.SERVO_SWITCH_STATUS) {
                        //更新开关状态
                        if (v == 1 || v == 0) {
                            isSafetyBtnEnable = v == 1;
                            updateSwitchUi();
                        }
                    } else if (zl == SocketConstant.PARACHUTE_CIRCUI_STATUS) {
                        boolean change = circuiStatus != v;
                        //熔断状态
                        if (v == DeviceStatus.COMPLETE) {//完成状态恢复默认
                            v = DeviceStatus.NORMAL;
                        }
                        circuiStatus = v;
                        if (change) {
                            updateCiruciStatus();
//                            requestSwitchInfo();
                            requestWeightInfo();
                            requestLenghtInfo();
                        }
                        if (circuiStatus != DeviceStatus.LOADING) {//如果熔断完成，则取消掉固定频率问询
                            SendTaskHelper.getInstance().remove(SocketConstant.PARACHUTE_CIRCUI_STATUS);
                        }
                    }


                    if (BuildConfig.DEBUG && !isIgonreIn(zl)) {
                        String str = Utils.bytesToHexFun3(bytes);
                        jsMap.put(TimeUtils.getNowString() + t++, str);

                        jsTv.setText(formartJs(jsMap));
                    }
                    break;
            }
        }
    };

//    private byte[] ignoreIn = {SocketConstant.PARACHUTE_3C, SocketConstant.PARACHUTE_3E};
    private byte[] ignoreIn = {};

    public boolean isIgonreIn(int v) {
        for (byte b : ignoreIn) {
            if (b == v) {
                return true;
            }
        }
        return false;
    }

    private void updateWeightUI() {
        int v = weightValue - SPUtil.INSTANCE.getInt(SocketConstant.DELETE_INIT_WEIGHT, 0);
        weightTv.setText("重量：" + v + "kg");
    }

    private void updateCiruciStatus() {
        Application context = com.blankj.utilcode.util.Utils.getApp();
        if (circuiStatus == DeviceStatus.LOADING) {
            circuitButton.setText(context.getString(R.string.ciruit_loading));
            circuitButton.setBackgroundResource(R.drawable.custom_btn_bg_fa0);
        } else {
            circuiStatus = DeviceStatus.NORMAL;
            circuitButton.setText(context.getString(R.string.ciruit));
            circuitButton.setBackgroundResource(isSafetyBtnEnable ? R.drawable.custom_btn_bg : R.drawable.custom_btn_disable_bg);
        }
    }

    private String formartJs(Map<String, String> jsMap) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> en : jsMap.entrySet()) {
            sb.append(en.getValue());
            sb.append("\r\n");
        }
        return sb.toString();
    }


    private Map<String, String> jsMap = new LinkedHashMap<String, String>() {

        @Override
        protected boolean removeEldestEntry(Entry<String, String> eldest) {
            super.removeEldestEntry(eldest);
            return this.size() > 10;
        }
    };

    private void updateSwitchUi() {
        if (safetyButton != null) {
            safetyButton.setText(isSafetyBtnEnable ? "开机中" : "关机中");
            safetyButton.setBackgroundResource(isSafetyBtnEnable ? R.drawable.custom_btn_bg_green : R.drawable.custom_btn_bg);
            SPUtil.INSTANCE.putBase("sj_kg", isSafetyBtnEnable);
        }
        updateCiruciStatus();
    }

    private boolean isCircuiLoading() {
        return circuiStatus == DeviceStatus.LOADING;
    }

}
