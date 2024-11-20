package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.service.ResultCallBack;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.socket.UdpSocketClient2;
import java.com.example.ground_station.data.utils.Utils;
import java.com.example.ground_station.presentation.util.DisplayUtils;
import java.util.List;

public class FloatingLightHelper extends BaseFloatingHelper {
    private final String tag = "light_tag";
    private final String TAG = "FloatingLightHelper";

    private final int LEFT_ROTATION = 1;
    private final int RIGHT_ROTATION = 2;
    private final int UP_ROTATION = 3;
    private final int DOWN_ROTATION = 4;
    private final int CENTER = 5; //一键回中
    private final int YAW_CENTER = 6; //偏航回中
    private final int PITCH_CENTER = 7; //俯仰回中

    private boolean isLighting = false;
    private boolean isFlashing = false;
    private boolean isRedBlueLighting = false;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable sendLeftRotationRunnable = new Runnable() {
        @Override
        public void run() {
            groundStationService.sendServoCommand(LEFT_ROTATION);
            handler.postDelayed(this, 100); // 每100毫秒发送一次消息
        }
    };

    private Runnable sendRightRotationRunnable = new Runnable() {
        @Override
        public void run() {
            groundStationService.sendServoCommand(RIGHT_ROTATION);
            handler.postDelayed(this, 100); // 每100毫秒发送一次消息
        }
    };

    private Runnable sendUpRotationRunnable = new Runnable() {
        @Override
        public void run() {
            groundStationService.sendServoCommand(UP_ROTATION);
            handler.postDelayed(this, 100); // 每100毫秒发送一次消息
        }
    };

    private Runnable sendDownRotationRunnable = new Runnable() {
        @Override
        public void run() {
            groundStationService.sendServoCommand(DOWN_ROTATION);
            handler.postDelayed(this, 100); // 每100毫秒发送一次消息
        }
    };
    private TextView setkValueTv;
    private TextView driveWdTv;
    private TextView headWdTv;

    public void showFloatingLight(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                groundStationService.sendUdpSocketCommand(SocketConstant.BRIGHTNESS, 100);
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
                .setLayout(R.layout.floating_light, view -> {
                    RelativeLayout content = view.findViewById(R.id.rlContent);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();

                    ScaleImage scaleImage = view.findViewById(R.id.ivScale);
                    scaleImage.setOnScaledListener(new ScaleImage.OnScaledListener() {
                        @Override
                        public void onScaled(float x, float y, MotionEvent event) {
                            params.width = Math.max(params.width + (int) x, DisplayUtils.dpToPx(300));
                            params.height = Math.max(params.height + (int) y, DisplayUtils.dpToPx(348));
                            // Update the size of the floating window
                            content.setLayoutParams(params);
                            // Force redraw the view
//                            content.postInvalidate();

                            EasyFloat.updateFloat(TAG, params.width, params.height);
                        }
                    });

                    ImageView leftRotation = view.findViewById(R.id.keyboard_left);
                    leftRotation.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    // 按下按钮时启动定时任务
//                                    handler.post(sendLeftRotationRunnable);
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 6);
                                    return true;
                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    // 松开按钮时停止定时任务
//                                    handler.removeCallbacks(sendLeftRotationRunnable);
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 7);
                                    return true;
                            }
                            return false;
                        }
                    });

                    ImageView rightRotation = view.findViewById(R.id.keyboard_right);
                    rightRotation.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    // 按下按钮时启动定时任务
//                                    handler.post(sendRightRotationRunnable);
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 8);
                                    return true;
                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    // 松开按钮时停止定时任务
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 9);
//                                    handler.removeCallbacks(sendRightRotationRunnable);
                                    return true;
                            }
                            return false;
                        }
                    });

                    ImageView upRotation = view.findViewById(R.id.keyboard_up);
                    upRotation.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    // 按下按钮时启动定时任务
//                                    handler.post(sendUpRotationRunnable);
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 1);
                                    return true;
                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    // 松开按钮时停止定时任务
//                                    handler.removeCallbacks(sendUpRotationRunnable);
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 2);
                                    return true;
                            }
                            return false;
                        }
                    });

                    ImageView downRotation = view.findViewById(R.id.keyboard_down);
                    downRotation.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    // 按下按钮时启动定时任务
//                                    handler.post(sendDownRotationRunnable);
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 3);
                                    return true;
                                case MotionEvent.ACTION_UP:
                                case MotionEvent.ACTION_CANCEL:
                                    // 松开按钮时停止定时任务
//                                    handler.removeCallbacks(sendDownRotationRunnable);
                                    groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 4);
                                    return true;
                            }
                            return false;
                        }
                    });

                    view.findViewById(R.id.center).setOnClickListener(v -> {
                        groundStationService.sendUdpSocketCommand(SocketConstant.DIRECTION, 5);

//                        groundStationService.sendServoCommand(CENTER);
                    });

//                    view.findViewById(R.id.yaw_center).setOnClickListener(v -> {
//                        groundStationService.sendServoCommand(1);
//                    });
//
//                    view.findViewById(R.id.pitch_center).setOnClickListener(v -> {
//                        groundStationService.sendServoCommand(2);
//                    });

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

                            view.findViewById(R.id.open_light_btn).setOnClickListener(v -> {
                                if (isBound) {
                                    if (isLighting) {
                                        groundStationService.sendUdpSocketCommand(SocketConstant.LIGHT, 0);
                                        ((TextView) v).setText("开灯");
                                    } else {
                                        groundStationService.sendUdpSocketCommand(SocketConstant.LIGHT, 1);
                                        ((TextView) v).setText("关灯");
                                    }
                                    isLighting = !isLighting;
                                }
                            });

                            view.findViewById(R.id.flashing_light_btn).setOnClickListener(v -> {
                                if (isBound) {
                                    if (isFlashing) {
                                        groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_FLASH, 0);
                                        ((TextView) v).setText("爆闪灯开");
                                    } else {
                                        groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_FLASH, 1);
                                        ((TextView) v).setText("爆闪灯关");
                                    }
                                    isFlashing = !isFlashing;
                                }
                            });


                            view.findViewById(R.id.red_blue_light_btn).setOnClickListener(v -> {
                                if (isBound) {
                                    if (isRedBlueLighting) {
                                        groundStationService.sendUdpSocketCommand(SocketConstant.RED_BLUE_FLASH, 0);
                                        ((TextView) v).setText("红蓝灯开");
                                    } else {
                                        groundStationService.sendUdpSocketCommand(SocketConstant.RED_BLUE_FLASH, 1);
                                        ((TextView) v).setText("红蓝灯关");
                                    }
                                    isRedBlueLighting = !isRedBlueLighting;
                                }
                            });

                            setkValueTv = view.findViewById(R.id.seekValueTv);
                            AppCompatSeekBar seekBar = view.findViewById(R.id.seek_bar);
                            updateUISeekValueTv(seekBar.getProgress());
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
                                    int volume = seekBar.getProgress();
                                    if (isBound) {
                                        groundStationService.sendUdpSocketCommand(SocketConstant.BRIGHTNESS, volume);
                                    }
                                    Log.d(TAG, "volume value: " + volume);
                                }
                            });

                            driveWdTv = view.findViewById(R.id.drive_temp_tv);
                            headWdTv = view.findViewById(R.id.lamp_holder_tempe_tv);

//                            UdpSocketClient2.getInstance().setCallBack(new ResultCallBack<byte[]>() {
//                                @Override
//                                public void result(byte[] bytes) {
//                                    disCacllBack(bytes);
//                                }
//                            });

                            reuestWd();


                            View testWdBtn = view.findViewById(R.id.testLightBtn);
                            testWdBtn.setVisibility(BuildConfig.DEBUG ? View.VISIBLE : View.GONE);
                            testWdBtn.setOnClickListener(view1 -> {
                                reuestWd();
                            });
                        }
                    }
                })
                .show();
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
                                    headWdTv.setText("灯头温度：" + v2 + "°C");
                                }
                            });
                        }
                    } else if (v == SocketConstant.EXPLOSION_WD_DRIVE && v2 != driveWdValue) {
                        driveWdValue = v2;
                        if (driveWdTv != null && v != null) {
                            driveWdTv.post(new Runnable() {
                                @Override
                                public void run() {
                                    driveWdTv.setText("驱动温度：" + v2 + "°C");
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

    private int driveWdValue, headWdValue = Integer.MIN_VALUE;

    private void updateUISeekValueTv(int volume) {
        if (setkValueTv != null) {
            setkValueTv.setText(volume + "%");
        }
    }

    public void reuestWd() {
        if (checkService()) {
            groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_WD, SocketConstant.EXPLOSION_WD_HEAD);
            groundStationService.sendUdpSocketCommand(SocketConstant.EXPLOSION_WD, SocketConstant.EXPLOSION_WD_DRIVE);
        }
    }

}
