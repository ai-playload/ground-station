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

import java.com.example.ground_station.data.service.ResultCallback;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.socket.UdpClientHelper;
import java.com.example.ground_station.data.utils.Utils;
import java.com.example.ground_station.data.utils.ViewUtils;
import java.com.example.ground_station.data.view.ConnectStatusView;

public class FloatingNewLightHelper extends BaseFloatingHelper {
    private final String tag = "light_tag";
    private final String TAG = "FloatingLightHelper";
    private TextView setkValueTv;
    private TextView driveWdTv;
    private TextView headWdTv;
    private int driveWdValue, headWdValue = Integer.MIN_VALUE;
    private static int mLightValue = 50 - 1;
    private static boolean mSwOpen;
    private static boolean mSwFlash;
    private static boolean mSwRedBlue;
    private ConnectStatusView statusView;

    public void showFloatingLight(Context context, CloseCallback closeCallback) {
        EasyFloat.with(context)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true)
                .setTag(tag)
                .setLayout(R.layout.floating_new_light, view -> {
                    if (view != null) {
                        initFloatingView(view, tag, closeCallback);
//                        initConnectStatus(view, UdpClientHelper.getInstance().getClient());

                        statusView = view.findViewById(R.id.statusView);

                        view.findViewById(R.id.open_light_btn).setOnClickListener(v -> {
                            v.setSelected(mSwOpen = !v.isSelected());
                            sendSwitchInstrunt(SocketConstant.LIGHT, v.isSelected());
                        });
                        view.findViewById(R.id.open_light_btn).setSelected(mSwOpen);

                        view.findViewById(R.id.flashing_light_btn).setOnClickListener(v -> {
                            v.setSelected(mSwFlash = !v.isSelected());
                            sendSwitchInstrunt(SocketConstant.EXPLOSION_FLASH, v.isSelected());
                        });
                        view.findViewById(R.id.flashing_light_btn).setSelected(mSwFlash);


                        view.findViewById(R.id.red_blue_light_btn).setOnClickListener(v -> {
                            v.setSelected(mSwRedBlue = !v.isSelected());
                            sendSwitchInstrunt(SocketConstant.RED_BLUE_FLASH, v.isSelected());
                        });
                        view.findViewById(R.id.red_blue_light_btn).setSelected(mSwRedBlue);

                        AppCompatSeekBar seekBar = view.findViewById(R.id.seek_bar);
                        setkValueTv = view.findViewById(R.id.seekValueTv);
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                updateUISeekValueTv(progress + 1);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                mLightValue = seekBar.getProgress();
                                int progress = seekBar.getProgress() + 1;
                                UdpClientHelper.getInstance().send(SocketConstant.BRIGHTNESS, progress);
                                if (headWdValue > 60 && progress > 40) {
                                    ToastUtils.showLong("温度过高，降低亮度");
                                }
                                Log.d(TAG, "progress value: " + progress);
                                updateUISeekValueTv(progress);
                            }

                        });
                        seekBar.setProgress(mLightValue);
                        UdpClientHelper.getInstance().send(SocketConstant.BRIGHTNESS, mLightValue + 1);
                        updateUISeekValueTv(mLightValue + 1);

                        AppCompatImageButton leftRotation = view.findViewById(R.id.light_left_btn);
                        leftRotation.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                switch (event.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 6);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 7);
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
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 8);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        // 松开按钮时停止定时任务
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 9);
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
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 1);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 2);
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
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 3);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up_select);
                                        return true;
                                    case MotionEvent.ACTION_UP:
                                    case MotionEvent.ACTION_CANCEL:
                                        UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 4);
                                        ((AppCompatImageButton) v).setImageResource(R.drawable.ic_new_audio_up);
                                        return true;
                                }
                                return false;
                            }
                        });

                        view.findViewById(R.id.light_center_btn).setOnClickListener(v -> {
                            UdpClientHelper.getInstance().send(SocketConstant.DIRECTION, 5);
                        });

                        driveWdTv = view.findViewById(R.id.drive_temp_tv);
                        headWdTv = view.findViewById(R.id.lamp_holder_tempe_tv);
                        UdpClientHelper.getInstance().getClient().setCallBack(new ResultCallback<byte[]>() {
                            @Override
                            public void result(byte[] bytes) {
                                disCacllBack(bytes);
                            }
                        });

                        View testWdBtn = view.findViewById(R.id.testLightBtn);
                        ViewUtils.setVisibility(testWdBtn, BuildConfig.DEBUG);
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
                        requestWd();
                    }

                    @Override
                    public void drag(@NonNull View view, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void dismiss() {
//                        UdpClientHelper.getInstance().getClient().setConnectCallBack(null);
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

    private void sendSwitchInstrunt(byte msgid2, boolean open) {
        UdpClientHelper.getInstance().send(msgid2, open ? 1 : 0);
        if (setkValueTv != null && open) {
            setkValueTv.post(new Runnable() {
                @Override
                public void run() {
                    UdpClientHelper.getInstance().send(SocketConstant.BRIGHTNESS, mLightValue + 1);
                }
            });
        }
    }

    private void updateUISeekValueTv(int volume) {
        if (setkValueTv != null) {
            setkValueTv.setText(volume + "%");
        }
    }

    public void requestWd() {
        UdpClientHelper.getInstance().send(SocketConstant.EXPLOSION_WD, SocketConstant.EXPLOSION_WD_HEAD);
        UdpClientHelper.getInstance().send(SocketConstant.EXPLOSION_WD, SocketConstant.EXPLOSION_WD_DRIVE);
    }

    private void disCacllBack(byte[] data) {
        if (data != null && data.length >= 5) {
            if (statusView != null) {
                statusView.setStatus(true);
            }

            byte msgId2 = data[3];
            Byte v = data[4];
            switch (msgId2) {
                case SocketConstant.EXPLOSION_WD:
                    Byte v2 = data[5];
                    if (v == SocketConstant.EXPLOSION_WD_HEAD && v2 != headWdValue) {//灯头温度
                        headWdValue = v2;
                        if (headWdValue > 60) {
                            ToastUtils.showLong("温度过高，降低亮度");
                        }
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
