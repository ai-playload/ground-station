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

import com.example.ground_station.R;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.iflytek.aikitdemo.tool.ThreadExtKt;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;

import kotlin.Unit;

public class FloatingNewAudioHelper extends BaseFloatingHelper {
    private final String tag = "audio_tag";
    private final String TAG = "FloatingAudioHelper";


    SocketClientHelper helper = SocketClientHelper.getMedia();

    public void showFloatingNewAudio(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                ThreadExtKt.mainThread(300, () -> {
                    int volume = SPUtil.INSTANCE.getInt("audio_volume", 100);
                    helper.send(SocketConstant.SET_VOLUME, volume);
                    return Unit.INSTANCE;
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
                .setTag(tag)
                .setLayout(R.layout.floating_new_audio, view -> {
                    if (view != null) {
                        initFloatingView(view, tag, closeCallback);
                        AppCompatImageButton shoutButton = view.findViewById(R.id.audio_shout_btn);
                        TextView shoutTv = view.findViewById(R.id.audio_shout_tv);

                        AppCompatSeekBar seekBar = view.findViewById(R.id.progressBar);
                        int volume = SPUtil.INSTANCE.getInt("audio_volume", 100);

                        seekBar.setProgress(volume);
                        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {
                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {
                                int volume = seekBar.getProgress();
                                if (isBound) {
                                    helper.send(SocketConstant.SET_VOLUME, volume);
                                    SPUtil.INSTANCE.putBase("audio_volume", volume);
                                }
                                Log.d(TAG, "volume value: " + volume);
                            }
                        });

                        shoutButton.setOnClickListener(v -> {
                            if (isBound) {
                                if (groundStationService.isShouting) {
                                    groundStationService.sendShoutCommand("");
                                    groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
                                    groundStationService.sendSocketCommand(SocketConstant.STOP_TALK, 0);
                                    shoutButton.setImageResource(R.drawable.ic_new_shout_play);
                                    shoutTv.setText("点击开始喊话");
                                } else {
                                    ShoutcasterConfig.DeviceInfo shoutcaster = groundStationService.getConfig().getShoutcaster();

                                    String command = String.format(GstreamerCommandConstant.SHOUTT_COMMAND, shoutcaster.getIp(), shoutcaster.getPort());
                                    groundStationService.sendShoutCommand(command);
                                    groundStationService.sendSocketCommand(SocketConstant.STREAMER, 1);
                                    groundStationService.sendSocketCommand(SocketConstant.START_TALK, 0);
                                    shoutButton.setImageResource(R.drawable.ic_new_shout_stop);
                                    shoutTv.setText("点击结束喊话");
                                }
                            }
                        });

                        view.findViewById(R.id.audio_up_btn).setOnClickListener(v -> {
                            groundStationService.sendServoCommand(1);
                        });

                        view.findViewById(R.id.audio_center_btn).setOnClickListener(v -> {
                            groundStationService.sendServoCommand(5);
                        });

                        view.findViewById(R.id.audio_down_btn).setOnClickListener(v -> {
                            groundStationService.sendServoCommand(2);
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

                        if (isBound) {
//                            groundStationService.isShouting = true;
//                            groundStationService.sendShoutCommand("");
                            groundStationService.cancelGstreamerAudioCommand();
                            groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
                        }

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
}
