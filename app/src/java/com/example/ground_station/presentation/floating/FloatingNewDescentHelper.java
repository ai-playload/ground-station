package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ground_station.R;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.socket.SocketConstant;

public class FloatingNewDescentHelper extends BaseFloatingHelper {
    private final String tag = "settings_tag";
    private final String TAG = "FloatingSettingsHelper";
    private boolean isSafetyBtnEnable = true;
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

    private int calculateSpeedValue(int progress) {
        return progress == 0 ? MIN_SPEED : progress * SPEED_MULTIPLIER;
    }

    private int calculateLengthValue(int progress) {
        return progress == 0 ? MIN_LENGTH : progress * LENGTH_MULTIPLIER;
    }

    public void showFloatingSettings(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, null);

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
        TextView safetyButton = view.findViewById(R.id.safety_button);
        TextView brakingButton = view.findViewById(R.id.braking_tv);
        TextView circuitButton = view.findViewById(R.id.circuit_tv);
        TextView upActionButton = view.findViewById(R.id.up_action);
        TextView downActionButton = view.findViewById(R.id.down_action);
        TextView stopActionButton = view.findViewById(R.id.stop_action);
        SeekBar speedBar = view.findViewById(R.id.speed_progress);
        SeekBar lengthBar = view.findViewById(R.id.length_progress);

        TextView speedTv = view.findViewById(R.id.speed_tv);
        TextView lengthTv = view.findViewById(R.id.length_tv);

        int savedSpeedProgress = SPUtil.INSTANCE.getInt("speed_progress", 0);
        int savedLengthProgress = SPUtil.INSTANCE.getInt("length_progress", 0);
        speedBar.setProgress(savedSpeedProgress);
        lengthBar.setProgress(savedLengthProgress);

        // 计算并更新速度和步进长度的值
        speedValue = calculateSpeedValue(savedSpeedProgress);
        lengthValue = calculateLengthValue(savedLengthProgress);
        speedTv.setText(speedValue + "  m/min");
        lengthTv.setText(lengthValue + "  m");

        speedBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                SPUtil.INSTANCE.putBase("speed_progress", progress);
                speedValue = calculateSpeedValue(progress);
                speedTv.setText(speedValue + "  m/min");
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
                groundStationService.sendSjInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_CLOSE);
            } else {
                groundStationService.sendSjInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_OPEN);
            }
            isSafetyBtnEnable = !isSafetyBtnEnable;
        });

        brakingButton.setOnClickListener(v -> {
            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE_CONTROL, 1);
        });

        circuitButton.setOnClickListener(v -> {
            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE_CONTROL, 2);
        });

//        relieveButton.setOnClickListener(v -> {
//            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE_CONTROL, 0);
//        });

        upActionButton.setOnClickListener(v -> {
            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE_SPEED, speedValue);
            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE_LENGTH, lengthValue);
        });

        downActionButton.setOnClickListener(v -> {
            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE_SPEED, -speedValue);
            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE_LENGTH, -lengthValue);
        });

        //2上升 3下降 4停止
        stopActionButton.setOnClickListener(v -> {
            groundStationService.sendSjInstruct(SocketConstant.PARACHUTE, PARACHUTE_STATUS_STOP);
        });
    }


}
