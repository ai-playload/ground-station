package java.com.example.ground_station.presentation.floating;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;
import com.google.android.material.textfield.TextInputEditText;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.service.ResultCallback;
import java.com.example.ground_station.data.socket.DeviceStatus;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.utils.RecvTaskHelper;
import java.com.example.ground_station.data.utils.SendTaskHelper;
import java.com.example.ground_station.data.utils.Utils;
import java.com.example.ground_station.presentation.util.DisplayUtils;
import java.com.example.ground_station.presentation.util.ViewUtils;
import java.util.LinkedHashMap;
import java.util.Map;

public class FloatingNewDescentHelper2 extends BaseFloatingHelper {
    private final String tag = "settings_tag";
    private final String TAG = "FloatingSettingsHelper";
    private boolean isSafetyBtnEnable = SPUtil.INSTANCE.getBoolean("sj_kg", false);
    private int circuiStatus = DeviceStatus.NORMAL;
    SocketClientHelper helper = SocketClientHelper.getDessent();

    private final int PARACHUTE_STATUS_CLOSE = 0;
    private final int PARACHUTE_STATUS_OPEN = 1;
    private final int PARACHUTE_STATUS_UP = 2;
    private final int PARACHUTE_STATUS_DOWN = 3;
    private final int PARACHUTE_STATUS_STOP = 4;
    private TextView lenghtTv;
    private TextView weightTv;
    private int speedValue = 1;
    private int lengthValue = 1;
    private int weightValue;
    private int speedInput = 1;
    private int lenghtInput = 1;
    private TextView jsTv;
    private TextView fsTv;
    private TextView circuitButton;
    private TextView safetyButton;

    public void showFloatingSettings(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, null);

        EasyFloat.with(context)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true)
                .hasEditText(true)
                .setTag(tag)
                .setLayout(R.layout.floating_settings_test, view -> {
//                    ViewGroup content = view.findViewById(R.id.rlContent_test);
//                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();
//
//                    ScaleImage scaleImage = view.findViewById(R.id.ivScale);
//                    scaleImage.setOnScaledListener(new ScaleImage.OnScaledListener() {
//                        @Override
//                        public void onScaled(float x, float y, MotionEvent event) {
//                            params.width = Math.max(params.width + (int) x, DisplayUtils.dpToPx(300));
//                            params.height = Math.max(params.height + (int) y, DisplayUtils.dpToPx(340));
//                            content.setLayoutParams(params);
//
//                            EasyFloat.updateFloat(TAG, params.width, params.height);
//                        }
//                    });

                    RecvTaskHelper.getInstance().getLoop().start();
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
                        RecvTaskHelper.getInstance().getLoop().stop();
                        if (mHandler != null) {
                            mHandler.removeCallbacksAndMessages(null);
                        }
                        SendTaskHelper.getInstance().getLoop().stop();
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void createdResult(boolean b, @Nullable String s, @Nullable View view) {
                        if (view != null) {
                            initFloatingView(view, tag, closeCallback);

                            safetyButton = view.findViewById(R.id.safety_button);
                            circuitButton = view.findViewById(R.id.circuit_tv);
                            TextView relieveButton = view.findViewById(R.id.relieve_tv);
                            TextView upActionButton = view.findViewById(R.id.up_action);
                            TextView downActionButton = view.findViewById(R.id.down_action);
                            TextView stopActionButton = view.findViewById(R.id.stop_action);

                            SeekBar speedInputView = view.findViewById(R.id.speed_progress);
                            SeekBar lengthBar = view.findViewById(R.id.length_progress);
                            TextView speedTv = view.findViewById(R.id.speed_tv);
                            TextView lengthTv = view.findViewById(R.id.length_tv);

                            int savedSpeedProgress = SPUtil.INSTANCE.getInt("speed_progress", 1);
                            int savedLengthProgress = SPUtil.INSTANCE.getInt("length_progress", 1);

                            speedInputView.setProgress(savedSpeedProgress);
                            lengthBar.setProgress(savedLengthProgress);

                            // 计算并更新速度和步进长度的值
                            speedValue = savedSpeedProgress;
                            lengthValue = savedLengthProgress;
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
                                    lengthValue = progress;
                                    lengthTv.setText(lengthValue + "  m");
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {

                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {

                                }
                            });

                            lenghtTv = view.findViewById(R.id.put_line_tv);
                            weightTv = view.findViewById(R.id.weight_tv);
                            updateSwitchUi();
                            safetyButton.setOnClickListener(v -> {
                                //熔断中且是关机状态，点击开机按钮
                                if (circuiStatus == DeviceStatus.LOADING && !isSafetyBtnEnable) {
                                    ToastUtils.showLong("熔断过程中，需保持关机状态");
                                    return;
                                }
                                isSafetyBtnEnable = !isSafetyBtnEnable;
                                updateSwitchState();
                            });

                            circuitButton.setOnClickListener(v -> {
                                if (circuiStatus != DeviceStatus.LOADING && !isSafetyBtnEnable) {//关机状态下，不可进行熔断
                                    ToastUtils.showLong("熔断需开机执行");
                                    return;
                                }

                                if (circuiStatus != DeviceStatus.LOADING) {
                                    circuiStatus = DeviceStatus.LOADING;

                                    isSafetyBtnEnable = false;
                                    updateSwitchUi();
                                    sendInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_CLOSE);
                                    sendInstruct(SocketConstant.PARACHUTE_CONTROL, 2);//进行熔断
                                    SendTaskHelper.getInstance().addInsturd(SocketConstant.PARACHUTE_CIRCUI_STATUS);//增加固定频率问询熔断状态
//                                    sendInstruct(SocketConstant.SERVO_SWITCH_STATUS);//询问开关状态
                                } else {
                                    SendTaskHelper.getInstance().remove(SocketConstant.PARACHUTE_CIRCUI_STATUS);
                                    circuiStatus = DeviceStatus.NORMAL;
                                    sendInstruct(SocketConstant.PARACHUTE_CONTROL, 0);//取消熔断
                                    sendInstruct(SocketConstant.PARACHUTE_CIRCUI_STATUS);//取消熔断
//                                    sendInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_CLOSE);//关机
//                                    circuitButton.setText(c ontext.getString(R.string.ciruit_loading_cancel));
                                }
                                updateCiruciStatus();
                            });

                            relieveButton.setOnClickListener(v -> {
                                requestLenghtInfo();
                            });

                            View zoneTv = view.findViewById(R.id.zoneTv);
                            zoneTv.setOnClickListener(view1 -> {
                                //零位学习
                                sendInstruct(SocketConstant.PARACHUTE_CONTROL, 5);
                            });

                            upActionButton.setOnClickListener(v -> {
                                if (isCircuiLoading()) {
                                    ToastUtils.showShort("熔断中不可操作");
                                    return;
                                }
//                                sendInstruct(SocketConstant.PARACHUTE_SPEED, Integer.parseInt(speedInputView.getText().toString()));
                                sendInstruct(SocketConstant.PARACHUTE_LENGTH, lengthBar.getProgress());
                            });

                            downActionButton.setOnClickListener(v -> {
                                if (isCircuiLoading()) {
                                    ToastUtils.showShort("熔断中不可操作");
                                    return;
                                }
                                //TODO: 2024/11/23
//                                sendInstruct(SocketConstant.PARACHUTE_SPEED, -speedInputView);
                                sendInstruct(SocketConstant.PARACHUTE_LENGTH, -lengthBar.getProgress());
                            });

                            view.findViewById(R.id.speedComiftBtn).setOnClickListener(view1 -> {
                                sendInstruct(SocketConstant.PARACHUTE_SPEED, speedInputView.getProgress());
                            });

                            //2上升 3下降 4停止
                            stopActionButton.setOnClickListener(v -> {
                                sendInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_STOP);
                            });

                            RecvTaskHelper.getInstance().setCallback(new ResultCallback<byte[]>() {
                                @Override
                                public void result(byte[] bytes) {
                                    setRevInfo(bytes);
                                }
                            });

                            jsTv = view.findViewById(R.id.jsTv);
                            fsTv = view.findViewById(R.id.fsTv);

                            View textSendMuiltBtn = view.findViewById(R.id.textSendBtn);
                            textSendMuiltBtn.setOnClickListener(view1 -> {
                                try {
                                    int size = lengthBar.getProgress();
                                    for (int i = 0; i < size; i++) {
//                                        updateLenghtInfo();
//                                        updateWeightInfo();
                                        sendInstruct(SocketConstant.PARACHUTE_SPEED, -speedInputView.getProgress());
                                        sendInstruct(SocketConstant.PARACHUTE_LENGTH, -lengthBar.getProgress());
                                    }
                                } catch (Exception e) {
                                }
                            });

                            view.findViewById(R.id.removePeelBtn).setOnClickListener(view1 -> {
//                                sendInstruct(SocketConstant.SERVO_WEIGHT_REMOVE_PEEL);
//                                requestWeightInfo();
                                SPUtil.INSTANCE.putBase(SocketConstant.DELETE_INIT_WEIGHT, weightValue);
                                updateWeightUI();
                            });
                            view.findViewById(R.id.lenghtResetBtn).setOnClickListener(view1 -> {
                                sendInstruct(SocketConstant.SERVO_WEIGHT_RESET_LENGHT);
                            });

                            SendTaskHelper.getInstance().getLoop().setTime(3000);
                            SendTaskHelper.getInstance().getLoop().start();

                            boolean debug = BuildConfig.DEBUG;

                            ViewUtils.setVisibility(zoneTv, debug);
                            ViewUtils.setVisibility(relieveButton, debug);
                            ViewUtils.setVisibility(textSendMuiltBtn, debug);
                            ViewUtils.setVisibility(view.findViewById(R.id.testHintRoot), debug);
                        }
                        init();
                    }
                })
                .show();
    }

    private void updateSwitchState() {
        if (groundStationService != null) {
            if (!isSafetyBtnEnable) {
                sendInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_CLOSE);
            } else {
                sendInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_OPEN);
            }
        }
        updateSwitchUi();
    }

    private void updateSwitchUi() {
        if (safetyButton != null) {
            safetyButton.setText(isSafetyBtnEnable ? "开机中" : "关机中");
//            safetyButton.setBackgroundResource(isSafetyBtnEnable ? R.drawable.custom_btn_bg_green : R.drawable.custom_btn_bg);
            SPUtil.INSTANCE.putBase("sj_kg", isSafetyBtnEnable);
            safetyButton.setSelected(isSafetyBtnEnable);
        }
        updateCiruciStatus();
    }

    private boolean isCircuiLoading() {
        return circuiStatus == DeviceStatus.LOADING;
    }


    protected void init() {
        if (BuildConfig.DEBUG) {
            helper.setResultCallback(new ResultCallback<Map<String, String>>() {

                @Override
                public void result(Map<String, String> fsMap) {
                    if (fsTv != null) {
                        fsTv.setText(formartJs(fsMap));
                    }
                }
            });
        }
        RecvTaskHelper.getInstance().setCallback(new ResultCallback<byte[]>() {
            @Override
            public void result(byte[] bytes) {
                setRevInfo(bytes);
            }
        });
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

    private void sendInstruct(byte msgId2, int... payload) {
        if (checkSerrvice()) {
            helper.send(msgId2, payload);//获取开关状态指令
        }
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

    private boolean checkSerrvice() {
        return helper.isConnected();
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

    private byte[] ignoreIn = {SocketConstant.PARACHUTE_3C, SocketConstant.PARACHUTE_3E};

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
        weightTv.setText(v + "kg");
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


}
