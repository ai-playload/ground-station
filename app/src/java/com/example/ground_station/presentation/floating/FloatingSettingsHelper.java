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

import java.com.example.ground_station.data.socket.DeviceStatus;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.callback.ResultCallback;

import java.com.example.ground_station.presentation.helper.RecvTaskHelper;
import java.com.example.ground_station.presentation.helper.SendTaskHelper;
import java.com.example.ground_station.presentation.util.DisplayUtils;
import java.com.example.ground_station.presentation.util.Utils;
import java.com.example.ground_station.presentation.util.ViewUtils;
import java.util.LinkedHashMap;
import java.util.Map;

public class FloatingSettingsHelper extends BaseFloatingHelper {
    private final String tag = "settings_tag";
    private final String TAG = "FloatingSettingsHelper";
    private boolean isSafetyBtnEnable = SPUtil.INSTANCE.getBoolean("sj_kg", false);
    private int circuiStatus = DeviceStatus.NORMAL;


    private final int PARACHUTE_STATUS_CLOSE = 0;
    private final int PARACHUTE_STATUS_OPEN = 1;
    private final int PARACHUTE_STATUS_UP = 2;
    private final int PARACHUTE_STATUS_DOWN = 3;
    private final int PARACHUTE_STATUS_STOP = 4;
    private TextView lenghtTv;
    private TextView weightTv;

    private int speedInput = 1;
    private int lenghtInput = 1;
    private TextInputEditText speedInputView;
    private TextInputEditText lenghtInputView;
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
                    ViewGroup content = view.findViewById(R.id.rlContent_test);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();

                    ScaleImage scaleImage = view.findViewById(R.id.ivScale);
                    scaleImage.setOnScaledListener(new ScaleImage.OnScaledListener() {
                        @Override
                        public void onScaled(float x, float y, MotionEvent event) {
                            params.width = Math.max(params.width + (int) x, DisplayUtils.dpToPx(300));
                            params.height = Math.max(params.height + (int) y, DisplayUtils.dpToPx(340));
                            content.setLayoutParams(params);

                            EasyFloat.updateFloat(TAG, params.width, params.height);
                        }
                    });

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
                            Button brakingButton = view.findViewById(R.id.braking_tv);
                            circuitButton = view.findViewById(R.id.circuit_tv);
                            Button relieveButton = view.findViewById(R.id.relieve_tv);
                            Button upActionButton = view.findViewById(R.id.up_action);
                            Button downActionButton = view.findViewById(R.id.down_action);
                            Button stopActionButton = view.findViewById(R.id.stop_action);

                            speedInputView = view.findViewById(R.id.audioInputContent);
                            lenghtInputView = view.findViewById(R.id.speedInputContent);

                            lenghtTv = view.findViewById(R.id.put_line_tv);
                            weightTv = view.findViewById(R.id.weight_tv);
                            View testUpdateBtn = view.findViewById(R.id.location_tv);
                            testUpdateBtn.setOnClickListener(view1 -> {
//                                requestWeightInfo();
                                requestSwitchInfo();
                            });
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

                            brakingButton.setOnClickListener(v -> {
                                sendInstruct(SocketConstant.PARACHUTE_CONTROL, 1);
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
//                                    circuitButton.setText(context.getString(R.string.ciruit_loading_cancel));
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

                                boolean hc = checkSpeed();
                                boolean hl = checkLenght();
                                if (!hc || !hl) {
                                    ToastUtils.showShort("速度档位设置区间为:1~10, 长度设置区间为:1~30。");
                                }
//                                sendInstruct(SocketConstant.PARACHUTE_SPEED, Integer.parseInt(speedInputView.getText().toString()));
                                sendInstruct(SocketConstant.PARACHUTE_LENGTH, Integer.parseInt(lenghtInputView.getText().toString()));
                            });

                            downActionButton.setOnClickListener(v -> {
                                if (isCircuiLoading()) {
                                    ToastUtils.showShort("熔断中不可操作");
                                    return;
                                }
                                boolean hc = checkSpeed();
                                boolean hl = checkLenght();
                                if (!hc || !hl) {
                                    ToastUtils.showShort("速度档位设置区间为:1~10, 长度设置区间为:1~30。");
                                }
                                // TODO: 2024/11/23
//                                sendInstruct(SocketConstant.PARACHUTE_SPEED, -Integer.parseInt(speedInputView.getText().toString()));
                                sendInstruct(SocketConstant.PARACHUTE_LENGTH, -Integer.parseInt(lenghtInputView.getText().toString()));
                            });

                            view.findViewById(R.id.speedComiftBtn).setOnClickListener(view1 -> {
                                sendInstruct(SocketConstant.PARACHUTE_SPEED, Integer.parseInt(speedInputView.getText().toString()));
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
                                    boolean hc = checkSpeed();
                                    boolean hc1 = checkLenght();
                                    int size = Integer.parseInt(lenghtInputView.getText().toString());
                                    for (int i = 0; i < size; i++) {
//                                        updateLenghtInfo();
//                                        updateWeightInfo();
                                        sendInstruct(SocketConstant.PARACHUTE_SPEED, -Integer.parseInt(speedInputView.getText().toString()));
                                        sendInstruct(SocketConstant.PARACHUTE_LENGTH, -Integer.parseInt(lenghtInputView.getText().toString()));
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
                            ViewUtils.setVisibility(testUpdateBtn, debug);
                            ViewUtils.setVisibility(relieveButton, debug);
                            ViewUtils.setVisibility(textSendMuiltBtn, debug);
                            ViewUtils.setVisibility(view.findViewById(R.id.testHintRoot), debug);
                        }
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
            safetyButton.setBackgroundResource(isSafetyBtnEnable ? R.drawable.custom_btn_bg_green : R.drawable.custom_btn_bg);
            SPUtil.INSTANCE.putBase("sj_kg", isSafetyBtnEnable);
        }
        updateCiruciStatus();
    }

    private boolean isCircuiLoading() {
        return circuiStatus == DeviceStatus.LOADING;
    }

    private boolean checkLenght() {
        boolean hf = true;
        try {
            String str = lenghtInputView.getText().toString();
            int v = Integer.parseInt(str);
            int clamp = MathUtils.clamp(v, 1, 30);
            lenghtInput = clamp;
            if (clamp != v) {
                lenghtInputView.setText(String.valueOf(clamp));
                hf = false;
            }
        } catch (Exception e) {
            lenghtInputView.setText(String.valueOf(lenghtInput));
            hf = false;
        }
        return hf;
    }

    private boolean checkSpeed() {
        boolean hf = true;
        try {
            String str = speedInputView.getText().toString();
            int v = Integer.parseInt(str);
            int clamp = MathUtils.clamp(v, 1, 10);
            speedInput = clamp;
            if (clamp != v) {
                speedInputView.setText(String.valueOf(clamp));
                hf = false;
            }
        } catch (Exception e) {
            speedInputView.setText(String.valueOf(speedInput));
            hf = false;
        }
        return hf;
    }

    @Override
    protected void onConnectedSuccess() {
        if (checkSerrvice()) {
            if (BuildConfig.DEBUG) {
                groundStationService.setResultCallback(new ResultCallback<Map<String, String>>() {

                    @Override
                    public void result(Map<String, String> fsMap) {
                        if (fsTv != null) {
                            fsTv.setText(formartJs(fsMap));
                        }
                    }
                });
            }
            requestSwitchInfo();
            requestLenghtInfo();
            requestWeightInfo();
            requestParachuteStatus();
            SendTaskHelper.getInstance().addInsturd(SocketConstant.PARACHUTE_3C, SocketConstant.PARACHUTE_3E);
        }
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
            groundStationService.send(msgId2, payload);//获取开关状态指令
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
        return groundStationService != null && weightTv != null;
    }

    int t;
    private int weightValue;
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


}
