package java.com.example.ground_station.presentation.floating;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.R;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.iflytek.aikitdemo.tool.ThreadExtKt;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.utils.InputMethodUtils;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.ability.AudioFileGenerationCallback;
import java.com.example.ground_station.presentation.util.GsonParser;
import java.com.example.ground_station.presentation.util.MusicFileUtil;
import java.com.example.ground_station.presentation.util.TCPFileUploader;
import java.io.File;
import java.util.List;

import kotlin.Unit;

public class FloatingNewTextToSpeechHelper extends BaseFloatingHelper {
    private final String tag = "text_to_speech_tag";
    private final String TAG = "TextToSpeechHelper";
    private boolean isCheckedLoop = false;

    public void showFloatingTextToSpeech(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                int volume = SPUtil.INSTANCE.getInt("audio_volume", 100);
                groundStationService.sendSetVolumeCommand(volume);

                groundStationService.getAiSoundHelper().setVCN("xiaofeng");
                groundStationService.setPlaybackCallback(() -> {
                    if (isCheckedLoop) {
                        playGstreamerMusic();
                    } else {
                        groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
                        groundStationService.sendSocketCommand(SocketConstant.STOP_TALK, 0);
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
                .setLayout(R.layout.floating_new_text_to_speech, view -> {
                    if (view != null) {
                        initFloatingView(view, tag, closeCallback);

                        TextInputLayout textInputLayout = view.findViewById(R.id.audioContent);
                        EditText editText = textInputLayout.getEditText();

                        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
                        MaterialRadioButton maleRadioButton = view.findViewById(R.id.radio_button_male);
                        maleRadioButton.setChecked(true);

                        CheckBox checkBox = view.findViewById(R.id.tts_loop_cb);
                        TextView speakButton = view.findViewById(R.id.tts_play_btn);

                        SeekBar seekBar = view.findViewById(R.id.seek_bar);
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

                        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(RadioGroup group, int checkedId) {
                                if (checkedId == R.id.radio_button_male) {
                                    groundStationService.getAiSoundHelper().setVCN("xiaofeng");
                                } else if (checkedId == R.id.radio_button_female) {
                                    groundStationService.getAiSoundHelper().setVCN("xiaoyan");
                                }
                            }
                        });

                        editText.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                InputMethodUtils.openInputMethod(editText, tag);
                            }
                        });

                        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View v, boolean hasFocus) {
                                if (hasFocus) {
                                    InputMethodUtils.openInputMethod(editText, tag);
                                } else {
                                    InputMethodUtils.closedInputMethod(tag);
                                }
                            }
                        });

                        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                isCheckedLoop = isChecked;
                            }
                        });

                        speakButton.setOnClickListener(v -> {
                            if (isBound) {
                                if (!editText.getText().toString().trim().isEmpty()) {
                                    groundStationService.generateAudioFile(editText.getText().toString(), new AudioFileGenerationCallback() {
                                        long startTime = 0;
                                        long endTime = 0;

                                        @Override
                                        public void onAudioFileGenerationStart() {
                                            startTime = System.currentTimeMillis();
                                            Log.d(TAG, "onAudioFileGenerationStart ");
                                        }

                                        @Override
                                        public void onAudioFileGenerationEnd(String filePath) {
                                            endTime = System.currentTimeMillis();
                                            long duration = endTime - startTime;

                                            groundStationService.sendSocketCommand(SocketConstant.STREAMER, 1);
                                            groundStationService.sendSocketCommand(SocketConstant.START_TALK, 0);

                                            if (isCheckedLoop) {
                                                groundStationService.sendSocketCommand(SocketConstant.TEXT_TO_SPEECH_LOOP, 0);
                                                uploadAudioFile(new File(filePath));
                                            } else {
                                                playGstreamerMusic();
                                            }

                                            Log.d(TAG, "onAudioFileGenerationEnd " + filePath + " duration: " + duration);
                                        }
                                    });
                                } else {
                                    Toast.makeText(context, "请输入要播放的内容", Toast.LENGTH_SHORT).show();
                                }
                            }
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
                            groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
                            groundStationService.cancelGstreamerAudioCommand();

                            groundStationService.getAiSoundHelper().stop();
                        }
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    }

                    @Override
                    public void createdResult(boolean success, @Nullable String s, @Nullable View view) {
                    }
                })
                .show();
    }

    private void playGstreamerMusic() {
        ShoutcasterConfig shoutcasterConfig = groundStationService.getConfig();

        File recordFile = groundStationService.getAiSoundHelper().getRecordFile();
        if (recordFile != null && shoutcasterConfig.getShoutcaster() != null) {
            String filePath = recordFile.getPath();
            String command = String.format(GstreamerCommandConstant.TEXT_TO_SPEECH_COMMAND, filePath, shoutcasterConfig.getShoutcaster().getIp(), shoutcasterConfig.getShoutcaster().getPort());

            groundStationService.sendMusicCommand(command);
        }
    }

    private void uploadAudioFile(File file) {

        TCPFileUploader uploader = new TCPFileUploader();
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity != null) {
            String filePath = file.getPath();
            uploader.uploadFile(ActivityUtils.getTopActivity(), filePath, (progress -> {
                Log.d("uploadAudioFile", "文件：" + filePath + "  progress: " + progress);
                if (progress == 100) {
                    ToastUtils.showShort("文件上传成功   文件名：" + file.getName());
//                    playBpFile(file);
                    playRemoteTtsLoopFile(file);
                }
            }));
        }
    }

    private void playRemoteTtsLoopFile(File file) {
        groundStationService.sendSocketThanReceiveCommand(SocketConstant.GET_RECORD_LIST, 0, () -> {
            ThreadExtKt.mainThread(500, () -> {
                groundStationService.receiveResponse(response -> {
                    Log.d(TAG, "Received response Tts " + response);

                    List<AudioModel> remoteAudioList = MusicFileUtil.getRemoteAudioList(response);
                    if (remoteAudioList != null) {
                        String targetFileName = file.getName();
                        int index = -1;

                        for (int i = 0; i < remoteAudioList.size(); i++) {
                            if (remoteAudioList.get(i).getAudioFileName().equals(targetFileName)) {
                                index = i;
                                break;
                            }
                        }

                        if (index != -1) {
                            groundStationService.sendSocketCommand(SocketConstant.PLAY_RECORD_Bp, index);
                        }
                    }

                });

                return Unit.INSTANCE;
            });
        });
    }

}
