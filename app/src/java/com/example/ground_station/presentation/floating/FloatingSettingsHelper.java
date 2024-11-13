package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ground_station.R;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.callback.ResultCallback;
import java.com.example.ground_station.presentation.util.DisplayUtils;
import java.com.example.ground_station.presentation.util.Utils;
import java.util.concurrent.TimeUnit;

public class FloatingSettingsHelper extends BaseFloatingHelper {
    private final String tag = "settings_tag";
    private final String TAG = "FloatingSettingsHelper";
    private boolean isSafetyBtnEnable = true;
    private final int PARACHUTE_STATUS_CLOSE = 0;
    private final int PARACHUTE_STATUS_OPEN = 1;
    private final int PARACHUTE_STATUS_UP = 2;
    private final int PARACHUTE_STATUS_DOWN = 3;
    private final int PARACHUTE_STATUS_STOP = 4;
    private TextView lenghtTv;
    private TextView weightTv;
    private TextView hintTv;

    public void showFloatingSettings(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, null);

        EasyFloat.with(context)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true)
                .hasEditText(true)
                .setTag(tag)
                .setLayout(R.layout.floating_settings, view -> {
                    RelativeLayout content = view.findViewById(R.id.rlContent);
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
                        if (mHandler != null) {
                            mHandler.removeCallbacksAndMessages(null);
                        }
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void createdResult(boolean b, @Nullable String s, @Nullable View view) {
                        if (view != null) {
                            initFloatingView(view, tag, closeCallback);

                            Button safetyButton = view.findViewById(R.id.safety_button);
                            Button brakingButton = view.findViewById(R.id.braking_tv);
                            Button circuitButton = view.findViewById(R.id.circuit_tv);
                            Button relieveButton = view.findViewById(R.id.relieve_tv);
                            Button upActionButton = view.findViewById(R.id.up_action);
                            Button downActionButton = view.findViewById(R.id.down_action);
                            Button stopActionButton = view.findViewById(R.id.stop_action);

                            MaterialRadioButton radioButtonSpeed = view.findViewById(R.id.radio_button_speed);
                            MaterialRadioButton radioButtonLength = view.findViewById(R.id.radio_button_length);
                            TextInputEditText audioInputContent = view.findViewById(R.id.audioInputContent);
                            TextInputEditText speedInputContent = view.findViewById(R.id.speedInputContent);
                            radioButtonSpeed.setChecked(true);

                            hintTv = view.findViewById(R.id.hint_tv);
                            lenghtTv = view.findViewById(R.id.put_line_tv);
                            weightTv = view.findViewById(R.id.weight_tv);
                            view.findViewById(R.id.location_tv).setOnClickListener(view1 -> {
                                updateWeightInfo();
                                updateLenghtInfo();
                            });

                            radioButtonSpeed.setOnClickListener(v -> {
                                if (radioButtonSpeed.isChecked()) {
                                    radioButtonLength.setChecked(false); // 取消另一个按钮的选中状态
                                }
                            });

                            radioButtonLength.setOnClickListener(v -> {
                                if (radioButtonLength.isChecked()) {
                                    radioButtonSpeed.setChecked(false); // 取消另一个按钮的选中状态
                                }
                            });

                            safetyButton.setOnClickListener(v -> {
                                if (isSafetyBtnEnable) {
                                    groundStationService.sendSocketCommand(SocketConstant.PARACHUTE, PARACHUTE_STATUS_CLOSE);
                                    safetyButton.setText("关");
                                } else {
                                    groundStationService.sendSocketCommand(SocketConstant.PARACHUTE, PARACHUTE_STATUS_OPEN);
                                    safetyButton.setText("开");
                                }
                                isSafetyBtnEnable = !isSafetyBtnEnable;
                            });

                            brakingButton.setOnClickListener(v -> {
                                groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_CONTROL, 1);
                            });

                            circuitButton.setOnClickListener(v -> {
                                groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_CONTROL, 2);
                            });


                            relieveButton.setOnClickListener(v -> {
//                                groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_CONTROL, 0);
                                updateLenghtInfo();
                            });

                            upActionButton.setOnClickListener(v -> {
                                if (radioButtonSpeed.isChecked()) {
                                    groundStationService.send(SocketConstant.PARACHUTE_SPEED, Integer.parseInt(audioInputContent.getText().toString()));
                                }

                                if (radioButtonLength.isChecked()) {
                                    groundStationService.send(SocketConstant.PARACHUTE_LENGTH, Integer.parseInt(speedInputContent.getText().toString()));
                                }
                                getLenghtInfo();
                            });

                            downActionButton.setOnClickListener(v -> {
                                if (radioButtonSpeed.isChecked()) {
                                    groundStationService.send(SocketConstant.PARACHUTE_SPEED, -Integer.parseInt(audioInputContent.getText().toString()));
                                }

                                if (radioButtonLength.isChecked()) {
                                    groundStationService.send(SocketConstant.PARACHUTE_LENGTH, -Integer.parseInt(speedInputContent.getText().toString()));
                                }
                                getLenghtInfo();
                            });

                            //2上升 3下降 4停止
                            stopActionButton.setOnClickListener(v -> {
                                groundStationService.send(SocketConstant.PARACHUTE, PARACHUTE_STATUS_STOP);
                                stopGetLenghtInfo();
                            });
                        }
                    }
                })
                .show();
    }

    @Override
    protected void onConnectedSuccess() {
        setJsMsgCallback();
        if (weightTv != null) {
            weightTv.post(new Runnable() {
                @Override
                public void run() {
                    updateWeightInfo();
                    updateLenghtInfo();
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
                String s = Utils.bytesToHexFun3(bytes);
                if (hintTv != null) {
                    hintTv.post(new Runnable() {
                        @Override
                        public void run() {
                            if (bytes != null && bytes.length >= 4) {
                                byte aByte = bytes[4];
                                int v = aByte;
                                byte zl = bytes[3];
                                if (zl == SocketConstant.PARACHUTE_3E) {
                                    lenghtTv.setText("当前放线长度：" + v + "m");
                                } else if (zl == SocketConstant.PARACHUTE_3C) {
                                    weightTv.setText("重量：" + v + "kg");
                                }
                                if (zl != SocketConstant.HEART_BEAT) {
                                    hintTv.setText(s);
                                }
                            }
                        }
                    });
                }
            }
        });
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
        updateLenghtInfo();
        if (!getLoghtLoading) {
            getLoghtLoading = true;
//            mHandler.sendEmptyMessageDelayed(SocketConstant.PARACHUTE_3E, 1000);
        }
    }

    private void stopGetLenghtInfo() {
        updateLenghtInfo();
//        mHandler.removeMessages(SocketConstant.PARACHUTE_3E);
        getLoghtLoading = false;
    }

    android.os.Handler mHandler = new android.os.Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SocketConstant.PARACHUTE_3E:
                    updateLenghtInfo();
//                    mHandler.sendEmptyMessageDelayed(SocketConstant.PARACHUTE_3E, 1000);
                    break;
            }
        }
    };

}
