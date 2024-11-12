package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

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

import java.com.example.ground_station.data.socket.MessageListener;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.util.DisplayUtils;

public class FloatingSettingsHelper extends BaseFloatingHelper {
    private final String tag = "settings_tag";
    private final String TAG = "FloatingSettingsHelper";
    private boolean isSafetyBtnEnable = true;
    private final int PARACHUTE_STATUS_CLOSE = 0;
    private final int PARACHUTE_STATUS_OPEN = 1;
    private final int PARACHUTE_STATUS_UP = 2;
    private final int PARACHUTE_STATUS_DOWN = 3;
    private final int PARACHUTE_STATUS_STOP = 4;

    public void showFloatingSettings(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                groundStationService.setMessageListener(new MessageListener() {
                    @Override
                    public void onMessageReceived(byte msg1, byte msg2, byte[] payload) {
                        switch (msg1) {
                            case SocketConstant.DESCENT:
                                int weight = msg2;

                                break;
                        }
                    }
                });
            }

            @Override
            public void onServiceDisconnected() {

            }
        });

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
                                groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_CONTROL, 0);
                            });

                            upActionButton.setOnClickListener(v -> {
                                if (radioButtonSpeed.isChecked()) {
                                    groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_SPEED, Integer.parseInt(audioInputContent.getText().toString()));
                                }

                                if (radioButtonLength.isChecked()) {
                                    groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_LENGTH, Integer.parseInt(speedInputContent.getText().toString()));
                                }
                            });

                            downActionButton.setOnClickListener(v -> {
                                if (radioButtonSpeed.isChecked()) {
                                    groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_SPEED, -Integer.parseInt(audioInputContent.getText().toString()));
                                }

                                if (radioButtonLength.isChecked()) {
                                    groundStationService.sendSocketCommand(SocketConstant.PARACHUTE_LENGTH, -Integer.parseInt(speedInputContent.getText().toString()));
                                }
                            });

                            //2上升 3下降 4停止
                            stopActionButton.setOnClickListener(v -> {
                                groundStationService.sendSocketCommand(SocketConstant.PARACHUTE, PARACHUTE_STATUS_STOP);
                            });
                        }
                    }
                })
                .show();
    }

}
