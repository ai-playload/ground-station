package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.example.ground_station.R;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.iflytek.aikitdemo.tool.ThreadExtKt;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.util.DisplayUtils;

import kotlin.Unit;

public class FloatingAudioHelper extends BaseFloatingHelper {
    private final String tag = "audio_tag";
    private final String TAG = "FloatingAudioHelper";

    public void showFloatingAudio(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                ThreadExtKt.mainThread(300, () -> {
                    int volume = SPUtil.INSTANCE.getInt("audio_volume", 100);
                    groundStationService.sendSetVolumeCommand(volume);
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
                .setLayout(R.layout.floating_audio, view -> {
                    RelativeLayout content = view.findViewById(R.id.rlContent);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();

                    ScaleImage scaleImage = view.findViewById(R.id.ivScale);
                    scaleImage.setOnScaledListener(new ScaleImage.OnScaledListener() {
                        @Override
                        public void onScaled(float x, float y, MotionEvent event) {
                            params.width = Math.max(params.width + (int) x, DisplayUtils.dpToPx(124));
                            params.height = Math.max(params.height + (int) y, DisplayUtils.dpToPx(180));
                            // Update the size of the floating window
                            content.setLayoutParams(params);
                            // Force redraw the view
//                            content.postInvalidate();

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

                        if (view != null) {
                            initFloatingView(view, tag, closeCallback);
                            Button button = view.findViewById(R.id.shout_button);
                            AppCompatSeekBar seekBar = view.findViewById(R.id.seek_bar);
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
                                        groundStationService.sendSetVolumeCommand(volume);
                                        SPUtil.INSTANCE.putBase("audio_volume", volume);
                                    }
                                    Log.d(TAG, "volume value: " + volume);
                                }
                            });

                            button.setOnClickListener(v -> {
                                if (isBound) {
                                    if (groundStationService.isShouting) {
                                        groundStationService.sendShoutCommand("");
                                        groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
                                        groundStationService.sendSocketCommand(SocketConstant.STOP_TALK, 0);
                                        button.setText("开始喊话");
                                    } else {
                                        ShoutcasterConfig.DeviceInfo shoutcaster = groundStationService.getConfig().getShoutcaster();

                                        String command = String.format(GstreamerCommandConstant.SHOUTT_COMMAND, shoutcaster.getIp(), shoutcaster.getPort());
                                        groundStationService.sendShoutCommand(command);
                                        groundStationService.sendSocketCommand(SocketConstant.STREAMER, 1);
                                        groundStationService.sendSocketCommand(SocketConstant.START_TALK, 0);
                                        button.setText("取消喊话");
                                    }
                                }
                            });

                            view.findViewById(R.id.up_rotation).setOnClickListener(v -> {
                                groundStationService.sendServoCommand(1);
                            });

                            view.findViewById(R.id.down_rotation).setOnClickListener(v -> {
                                groundStationService.sendServoCommand(2);
                            });

                            view.findViewById(R.id.center).setOnClickListener(v -> {
                                groundStationService.sendServoCommand(5);
                            });
                        }
                    }
                })
                .show();
    }
}
