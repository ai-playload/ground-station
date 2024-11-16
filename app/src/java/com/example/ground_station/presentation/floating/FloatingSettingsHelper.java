package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.R;
import com.google.android.material.textfield.TextInputEditText;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.callback.ResultCallback;

import java.com.example.ground_station.presentation.helper.RecvTaskHelper;
import java.com.example.ground_station.presentation.helper.SendTaskHelper;
import java.com.example.ground_station.presentation.util.DisplayUtils;
import java.com.example.ground_station.presentation.util.Utils;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.function.Predicate;

public class FloatingSettingsHelper extends BaseFloatingHelper {
    private final String tag = "settings_tag";
    private final String TAG = "FloatingSettingsHelper";
    private boolean isSafetyBtnEnable = SPUtil.INSTANCE.getBoolean("sj_kg", false);
    private final int PARACHUTE_STATUS_CLOSE = 0;
    private final int PARACHUTE_STATUS_OPEN = 1;
    private final int PARACHUTE_STATUS_UP = 2;
    private final int PARACHUTE_STATUS_DOWN = 3;
    private final int PARACHUTE_STATUS_STOP = 4;
    private TextView lenghtTv;
    private TextView weightTv;
    private TextView hintTv;

    private int speedInput = 1;
    private int lenghtInput = 1;
    private TextInputEditText speedInputView;
    private TextInputEditText lenghtInputView;
    private TextView jsTv;
    private TextView fsTv;

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

                            TextView safetyButton = view.findViewById(R.id.safety_button);
                            Button brakingButton = view.findViewById(R.id.braking_tv);
                            TextView circuitButton = view.findViewById(R.id.circuit_tv);
                            Button relieveButton = view.findViewById(R.id.relieve_tv);
                            Button upActionButton = view.findViewById(R.id.up_action);
                            Button downActionButton = view.findViewById(R.id.down_action);
                            Button stopActionButton = view.findViewById(R.id.stop_action);

//                            MaterialRadioButton radioButtonSpeed = view.findViewById(R.id.radio_button_speed);
//                            MaterialRadioButton radioButtonLength = view.findViewById(R.id.radio_button_length);
                            speedInputView = view.findViewById(R.id.audioInputContent);
                            lenghtInputView = view.findViewById(R.id.speedInputContent);
//                            radioButtonSpeed.setChecked(true);

                            hintTv = view.findViewById(R.id.hint_tv);
                            lenghtTv = view.findViewById(R.id.put_line_tv);
                            weightTv = view.findViewById(R.id.weight_tv);
                            view.findViewById(R.id.location_tv).setOnClickListener(view1 -> {
                                updateWeightInfo();
//                                updateLenghtInfo();
                            });

//                            radioButtonSpeed.setOnClickListener(v -> {
//                                if (radioButtonSpeed.isChecked()) {
//                                    radioButtonLength.setChecked(false); // 取消另一个按钮的选中状态
//                                }
//                            });
//
//                            radioButtonLength.setOnClickListener(v -> {
//                                if (radioButtonLength.isChecked()) {
//                                    radioButtonSpeed.setChecked(false); // 取消另一个按钮的选中状态
//                                }
//                            });
                            safetyButton.setBackgroundResource(isSafetyBtnEnable ? R.drawable.custom_btn_bg_green : R.drawable.custom_btn_bg);
                            safetyButton.setOnClickListener(v -> {
                                if (isSafetyBtnEnable) {
                                    groundStationService.send(SocketConstant.PARACHUTE, PARACHUTE_STATUS_CLOSE);
                                    safetyButton.setText("关机中");
                                } else {
                                    groundStationService.send(SocketConstant.PARACHUTE, PARACHUTE_STATUS_OPEN);
                                    safetyButton.setText("开机中");
                                }
                                isSafetyBtnEnable = !isSafetyBtnEnable;
                                safetyButton.setBackgroundResource(isSafetyBtnEnable ? R.drawable.custom_btn_bg_green : R.drawable.custom_btn_bg);
                                SPUtil.INSTANCE.putBase("sj_kg", isSafetyBtnEnable);
                            });

                            brakingButton.setOnClickListener(v -> {
                                groundStationService.send(SocketConstant.PARACHUTE_CONTROL, 1);
                            });

                            circuitButton.setOnClickListener(v -> {
                                String str = circuitButton.getText().toString();
                                String rd = "熔断";
                                String gb = "熔断中";
                                if (StringUtils.equals(str, rd)) {
                                    circuitButton.setText(gb);
                                    groundStationService.send(SocketConstant.PARACHUTE_CONTROL, 2);
                                    circuitButton.setBackgroundResource(R.drawable.custom_btn_bg_fa0);
                                } else {
                                    circuitButton.setText(rd);
                                    groundStationService.send(SocketConstant.PARACHUTE_CONTROL, 0);
                                    circuitButton.setBackgroundResource(R.drawable.custom_btn_bg);
                                }
                            });

//                            speedInputView.addTextChangedListener(new TextWatcher() {
//                                @Override
//                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                                }
//
//                                @Override
//                                public void onTextChanged(CharSequence c, int i, int i1, int i2) {
//                                    checkSpeed(c);
//                                }
//
//                                @Override
//                                public void afterTextChanged(Editable editable) {
//
//                                }
//                            });

//                            lenghtInputView.addTextChangedListener(new TextWatcher() {
//                                @Override
//                                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//                                }
//
//                                @Override
//                                public void onTextChanged(CharSequence c, int i, int i1, int i2) {
//
//                                }
//
//                                @Override
//                                public void afterTextChanged(Editable editable) {
//
//                                }
//                            });


                            relieveButton.setOnClickListener(v -> {
//                                groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_CONTROL, 0);
                                updateLenghtInfo();
                            });

                            view.findViewById(R.id.speedComiftBtn).setOnClickListener(view1 -> {
                                boolean hc = checkSpeed();
                                groundStationService.send(SocketConstant.PARACHUTE_SPEED, Integer.parseInt(speedInputView.getText().toString()));
                            });

                            view.findViewById(R.id.zoneTv).setOnClickListener(view1 -> {
                                //零位学习
                                groundStationService.send(SocketConstant.PARACHUTE_CONTROL, 5);
                            });

                            upActionButton.setOnClickListener(v -> {
//                                if (radioButtonSpeed.isChecked()) {
//                                }
//                                if (radioButtonLength.isChecked()) {
//                                }
                                boolean hc = checkSpeed();
                                boolean hl = checkLenght();
                                if (!hc || !hl) {
                                    ToastUtils.showShort("速度档位设置区间为:1~10, 长度设置区间为:1~30。");
                                }
//                                groundStationService.send(SocketConstant.PARACHUTE_SPEED, Integer.parseInt(speedInputView.getText().toString()));
                                groundStationService.send(SocketConstant.PARACHUTE_LENGTH, Integer.parseInt(lenghtInputView.getText().toString()));
                                getLenghtInfo();

                            });

                            downActionButton.setOnClickListener(v -> {
//                                if (radioButtonSpeed.isChecked()) {
//                                }
//                                if (radioButtonLength.isChecked()) {
//                                }
                                boolean hc = checkSpeed();
                                boolean hl = checkLenght();
                                if (!hc || !hl) {
                                    ToastUtils.showShort("速度档位设置区间为:1~10, 长度设置区间为:1~30。");
                                }
//                                groundStationService.send(SocketConstant.PARACHUTE_SPEED, -Integer.parseInt(speedInputView.getText().toString()));
                                groundStationService.send(SocketConstant.PARACHUTE_LENGTH, -Integer.parseInt(lenghtInputView.getText().toString()));
                                getLenghtInfo();
                            });

                            //2上升 3下降 4停止
                            stopActionButton.setOnClickListener(v -> {
                                groundStationService.send(SocketConstant.PARACHUTE, PARACHUTE_STATUS_STOP);
                                stopGetLenghtInfo();
                            });

                            RecvTaskHelper.getInstance().setCallback(new ResultCallback<byte[]>() {
                                @Override
                                public void result(byte[] bytes) {
                                    setRevInfo(bytes);
                                }
                            });

                            ((TextView) view.findViewById(R.id.initTv)).setText("已经完成初始化！！！");

                            jsTv = view.findViewById(R.id.jsTv);
                            fsTv = view.findViewById(R.id.fsTv);


                            view.findViewById(R.id.textSendBtn).setOnClickListener(view1 -> {
                                try {
                                    int size = Integer.parseInt(lenghtInputView.getText().toString());
                                    for (int i = 0; i < size; i++) {
                                        updateLenghtInfo();
                                        updateWeightInfo();
                                    }
                                } catch (Exception e) {
                                }
                            });

                            view.findViewById(R.id.testLandAndSeeped).setOnClickListener(view1 -> {
                                groundStationService.send(SocketConstant.PARACHUTE_SPEED, -Integer.parseInt(speedInputView.getText().toString()));
                                groundStationService.send(SocketConstant.PARACHUTE_LENGTH, -Integer.parseInt(lenghtInputView.getText().toString()));
                            });
                        }
                    }
                })
                .show();
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
//        setJsMsgCallback();
//        if (weightTv != null) {
//            weightTv.post(new Runnable() {
//                @Override
//                public void run() {
//                    updateWeightInfo();
//                    updateLenghtInfo();
//                }
//            });
//        }

        if (groundStationService != null) {
            groundStationService.setResultCallback(new ResultCallback<Map<String, String>>() {

                @Override
                public void result(Map<String, String> fsMap) {
                    if (fsTv != null) {
                        fsTv.setText(formartJs(fsMap));
                    }
                }
            });
        }
    }

    private void setJsMsgCallback() {
        if (groundStationService == null || weightTv == null) {
            return;
        }
        groundStationService.sendMsgAndCallBack(new ResultCallback<byte[]>() {
            @Override
            public void result(byte[] bytes) {
                setRevInfo(bytes);
            }
        });
    }

    private void setRevInfo(byte[] bytes) {
        if (hintTv != null) {
            if (bytes != null && bytes.length >= 4 && bytes[0] == SocketConstant.HEADER) {
                byte aByte = bytes[3];
                if (aByte == SocketConstant.HEART_BEAT) {//心跳包数据
                    return;
                }
                String v = Utils.bytesToHexFun3(bytes);
                String hintText = hintTv.getText().toString();
                boolean hintChange = !TextUtils.equals(v, hintText);

                if (hintChange) {
                    Message message = new Message();
                    message.what = 0;
                    Bundle bundle = new Bundle();
                    bundle.putByteArray("gs", bytes);
                    message.setData(bundle);
                    mHandler.sendMessage(message);

//                    hintTv.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            byte aByte = bytes[4];
//                            int v = aByte;
//                            byte zl = bytes[3];
//                            if (zl == SocketConstant.PARACHUTE_3E) {
//                                lenghtTv.setText("当前放线长度：" + v + "m");
//                            } else if (zl == SocketConstant.PARACHUTE_3C) {
//                                weightTv.setText("重量：" + v + "kg");
//                            }
//                            if (hintChange) {
//                                hintTv.setText(v);
//                            }
//                        }
//                    });
                }
            }
        }
    }

    private void updateWeightInfo() {
        if (groundStationService == null || weightTv == null) {
            return;
        }
        groundStationService.send(SocketConstant.PARACHUTE_3C);
    }

    private void updateLenghtInfo() {
        if (groundStationService == null || weightTv == null) {
            return;
        }
        groundStationService.send(SocketConstant.PARACHUTE_3E);
    }

    private boolean getLoghtLoading = false;

    private void getLenghtInfo() {
        SendTaskHelper.getInstance().setInsturd(SocketConstant.PARACHUTE_3E);
        SendTaskHelper.getInstance().getLoop().start();

//        updateLenghtInfo();
//        if (!getLoghtLoading) {
//            getLoghtLoading = true;
////            mHandler.sendEmptyMessageDelayed(SocketConstant.PARACHUTE_3E, 1000);
//        }
    }

    private void stopGetLenghtInfo() {
        SendTaskHelper.getInstance().getLoop().stop();

//        updateLenghtInfo();
////        mHandler.removeMessages(SocketConstant.PARACHUTE_3E);
//        getLoghtLoading = false;
    }

    int t;
    android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    byte[] bytes = msg.getData().getByteArray("gs");
                    byte aByte = bytes[4];
                    int v = aByte;
                    byte zl = bytes[3];
                    if (zl == SocketConstant.PARACHUTE_3E) {
                        lenghtTv.setText("当前放线长度：" + v + "m");
                    } else if (zl == SocketConstant.PARACHUTE_3C) {
                        weightTv.setText("重量：" + v + "kg");
                    }
                    String str = Utils.bytesToHexFun3(bytes);
                    jsMap.put(TimeUtils.getNowString() + t++, str);
                    hintTv.setText(str);
                    jsTv.setText(formartJs(jsMap));
                    break;
//                case SocketConstant.PARACHUTE_3E:
//                    updateLenghtInfo();
////                    mHandler.sendEmptyMessageDelayed(SocketConstant.PARACHUTE_3E, 1000);
//                    break;
            }
        }
    };

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
            return this.size() > 8;
        }
    };


}
