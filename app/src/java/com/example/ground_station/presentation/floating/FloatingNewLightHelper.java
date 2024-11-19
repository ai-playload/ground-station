package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.service.ResultCallBack;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.socket.UdpSocketClient2;
import java.com.example.ground_station.data.utils.Utils;

public class FloatingNewLightHelper extends BaseFloatingHelper {
    private final String tag = "light_tag";
    private final String TAG = "FloatingLightHelper";
    private boolean isLighting = false;
    private boolean isFlashing = false;
    private boolean isRedBlueLighting = false;
    private TextView setkValueTv;
    private TextView driveWdTv;
    private TextView headWdTv;
    private int driveWdValue, headWdValue = Integer.MIN_VALUE;

    public void showFloatingLight(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                int progress = SPUtil.INSTANCE.getInt("light_progress", 100);
                groundStationService.sendUdpSocketCommand(SocketConstant.BRIGHTNESS, progress);
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
                .setTag(tag)
                .setLayout(R.layout.floating_new_light, view -> {
                    if (view != null) {
                        initFloatingView(view, tag, closeCallback);

                        view.findViewById(R.id.open_light_btn).setOnClickListener(v -> {
                            v.setSelected(!v.isSelected());

                            if (isBound) {
                                if (isLighting) {
                                    groundStationService.sendUdpSocketCommand(SocketConstant.LIGHT, 0);
                                } else {
                                    groundStationService.sendUdpSocketCommand(SocketConstant.LIGHT, 1);
                                }
                                isLighting = !isLighting;
                            }
                        });

                        view.findViewById(R.id.flashing_light_btn).setOnClickListener(v -> {
                            v.setSelected(!v.isSelected());

                            if (isBound) {
                                if (isFlashing) {
                                    groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_FLASH, 0);
                                } else {
                                    groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_FLASH, 1);
                                }
                                isFlashing = !isFlashing;
                            }
                        });


                        view.findViewById(R.id.red_blue_light_btn).setOnClickListener(v -> {
                            v.setSelected(!v.isSelected());

                            if (isBound) {
                                if (isRedBlueLighting) {
                                    groundStationService.sendUdpSocketCommand(SocketConstant.RED_BLUE_FLASH, 0);
                                } else {
                                    groundStationService.sendUdpSocketCommand(SocketConstant.RED_BLUE_FLASH, 1);
                                }
                                isRedBlueLighting = !isRedBlueLighting;
                            }
                        });

                        AppCompatSeekBar seekBar = view.findViewById(R.id.seek_bar);
                        int progress = SPUtil.INSTANCE.getInt("light_progress", 100);
                        seekBar.setProgress(progress);

                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                updateUISeekValueTv(progress);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                int progress = seekBar.getProgress();
                                SPUtil.INSTANCE.putBase("light_progress", progress);
                                if (isBound) {
                                    groundStationService.sendUdpSocketCommand(SocketConstant.BRIGHTNESS, progress);
                                }
                                Log.d(TAG, "progress value: " + progress);
                            }
                        });
                        setkValueTv = view.findViewById(R.id.seekValueTv);
                        updateUISeekValueTv(seekBar.getProgress());


                        AppCompatImageButton leftRotation = view.findViewById(R.id.light_left_btn);
                        leftRotation.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 6);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 7);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up);
                                        return true;
                                }
                                return false;
                            }
                        });

                        AppCompatImageButton rightRotation = view.findViewById(R.id.light_right_btn);
                        rightRotation.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 8);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        // 松开按钮时停止定时任务
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 9);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up);
                                        return true;
                                }
                                return false;
                            }
                        });

                        AppCompatImageButton upRotation = view.findViewById(R.id.light_up_btn);
                        upRotation.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 1);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 2);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up);
                                        return true;
                                }
                                return false;
                            }
                        });

                        AppCompatImageButton downRotation = view.findViewById(R.id.light_down_btn);
                        downRotation.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 3);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 4);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up);
                                        return true;
                                }
                                return false;
                            }
                        });

                        view.findViewById(R.id.light_center_btn).setOnClickListener(v -> {
                            groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 5);
                        });

                        driveWdTv = view.findViewById(R.id.drive_temp_tv);
                        headWdTv = view.findViewById(R.id.lamp_holder_tempe_tv);

                        UdpSocketClient2.getInstance().setCallBack(new ResultCallBack<byte[]>() {
                            @Override
                            public void result(byte[] bytes) {
                                disCacllBack(bytes);
                            }
                        });

                        requestWd();


                        View testWdBtn = view.findViewById(R.id.testLightBtn);
                        testWdBtn.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
                        testWdBtn.setOnClickListener(view1 -> {
                            requestWd();
                        });
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

    @Override
    public void onSuccessConnected() {
        super.onSuccessConnected();
        requestWd();
    }

    private void updateUISeekValueTv(int volume) {
        if (setkValueTv != null) {
            setkValueTv.setText(volume + "%");
        }
    }

    public void requestWd() {
        if (checkService()) {
            groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_WD, SocketConstant.EXPLOSION_WD_HEAD);
            groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_WD, SocketConstant.EXPLOSION_WD_DRIVE);
        }
    }

    private void disCacllBack(byte[] data) {
        if (data != null && data.length >= 5) {
            byte msgId2 = data[3];
            Byte v = data[4];
            switch (msgId2) {
                case SocketConstant.EXPLOSION_WD:
                    Byte v2 = data[5];
                    if (v == SocketConstant.EXPLOSION_WD_HEAD && v2 != headWdValue) {//灯头温度
                        headWdValue = v2;
                        if (headWdTv != null && v != null) {
                            headWdTv.post(new Runnable() {
                                @Override
                                public void run() {
                                    headWdTv.setText("灯头温度：" + v2 + " °C");
                                }
                            });
                        }
                    } else if (v == SocketConstant.EXPLOSION_WD_DRIVE && v2 != driveWdValue) {
                        driveWdValue = v2;
                        if (driveWdTv != null && v != null) {
                            driveWdTv.post(new Runnable() {
                                @Override
                                public void run() {
                                    driveWdTv.setText("驱动温度：" + v2 + " °C");
                                }
                            });
                        }
                    }
                    break;
            }
        }
        String s = Utils.bytesToHexFun3(data);
        ToastUtils.showLong(s);
    }
}
