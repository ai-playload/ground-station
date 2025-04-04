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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.example.ground_station.R;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.utils.InputMethodUtils;

import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.view.ConnectStatusView;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.ability.AudioFileGenerationCallback;
import java.com.example.ground_station.presentation.ability.tts.TtsHelper2;
import java.com.example.ground_station.presentation.fun.file.FileInfoUtils;
import java.com.example.ground_station.presentation.fun.file.PathConstants;
import java.com.example.ground_station.presentation.fun.file.SardineCallBack;
import java.com.example.ground_station.presentation.fun.file.SardineHelper;
import java.io.File;

public class FloatingTextToSpeechHelper extends BaseFloatingHelper {
    private final String tag = "text_to_speech_tag";
    private final String TAG = "TextToSpeechHelper";
    private boolean isCheckedLoop = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private CheckBox checkBox;
    //    private static int fileNameFlag = 0;
    private SocketClientHelper helper = SocketClientHelper.getMedia();

    public void showFloatingTextToSpeech(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {

                groundStationService.getAiSoundHelper().setVCN("xiaofeng");
                groundStationService.setPlaybackCallback(() -> {
//                    if (isCheckedLoop) {
//                        playGstreamerMusic();
//                    } else {
//                        send(SocketConstant.STREAMER, 2);
//                        send(SocketConstant.STOP_TALK, 0);
//                    }
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
                            send(SocketConstant.STREAMER, 2);
                            groundStationService.cancelGstreamerAudioCommand();

                            groundStationService.getAiSoundHelper().stop();
                        }
                        helper.setConnectCallBack(null);
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    }

                    @Override
                    public void createdResult(boolean success, @Nullable String s, @Nullable View view) {
                        if (view != null) {
                            initFloatingView(view, tag, closeCallback);
                            initConnectStatus(view, helper);

                            TextInputLayout textInputLayout = view.findViewById(R.id.audioContent);
                            EditText editText = textInputLayout.getEditText();

                            RadioGroup radioGroup = view.findViewById(R.id.radio_group);
                            MaterialRadioButton maleRadioButton = view.findViewById(R.id.radio_button_male);
                            maleRadioButton.setChecked(true);

                            TextView speakButton = view.findViewById(R.id.tts_play_btn);
                            checkBox = view.findViewById(R.id.tts_loop_cb);

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
                                    if (!isChecked) {
//                                        send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, 2);
                                        groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, 0, 2);
                                    }
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

                            speakButton.setOnClickListener(v -> {
                                if (isBound) {
                                    String editValue = editText.getText().toString().trim();
                                    if (!editValue.isEmpty()) {
                                        String audioFileName = FileInfoUtils.getText2AduioFileName(editValue);
                                        groundStationService.getTtsHelper().setFileName(audioFileName);
                                        String fileName = groundStationService.getTtsHelper().getFileName();
                                        groundStationService.generateAudioFile(editValue, new AudioFileGenerationCallback() {
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
                                                Log.d(TAG, "onAudioFileGenerationEnd " + filePath + " duration: " + duration);
                                            }
                                        });
                                        groundStationService.getTtsHelper().setFileWriteListener(new TtsHelper2.IFileWriteListener() {
                                            @Override
                                            public void onSuccess(File file, String aduidoStr) {
                                                Log.d("TtsEngine", "onSuccess 回调 file" + file.getName());
                                                boolean equals = StringUtils.equals(file.getName(), fileName);
                                                if (equals) {
                                                    playGstreamerMusic(audioFileName);
                                                }
                                                speakButton.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        speakButton.setEnabled(true);
                                                    }
                                                });
                                            }
                                        });
                                        speakButton.setEnabled(false);
                                    } else {
                                        Toast.makeText(context, "请输入要播放的内容", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                })
                .show();
    }

    private void playGstreamerMusic(String aduidoStr) {
        ShoutcasterConfig.DeviceInfo shoutcasterConfig = ShoutcasterConfig.getShoutcaster();
        File recordFile = groundStationService.getAiSoundHelper().getRecordFile();
        if (recordFile != null) {
            if (!checkBox.isChecked()) {
                send(SocketConstant.STREAMER, 1);
                String filePath = recordFile.getPath();
                String command = String.format(GstreamerCommandConstant.TEXT_TO_SPEECH_COMMAND, filePath, shoutcasterConfig.getIp(), shoutcasterConfig.getPort());
                groundStationService.sendMusicCommand(command);
            } else {
                uploadAudioFile(recordFile, aduidoStr);
            }
        }
    }

    private void uploadAudioFile(File file, String aduidoStr) {
        Activity topActivity = ActivityUtils.getTopActivity();
        if (topActivity != null) {
            SardineHelper sardineHelper = new SardineHelper(null);
            String palyPath = PathConstants.getText2AudioFileName(aduidoStr);
            sardineHelper.upLoad(palyPath, file, aduidoStr, new SardineCallBack<String>() {
                @Override
                public void getResult(String s) {
                    playBpFile(palyPath);
                }
            });
        }
    }

    private void playBpFile(String palyPath) {
        int index = palyPath.lastIndexOf("/") + 1;
        if (index >= 0 && index < palyPath.length()) {
            String palyName = palyPath.substring(index);
            int payload = FileInfoUtils.file2Payload(palyName);
            if (payload >= 0) {
                send(SocketConstant.PLAY_REMOTE_AUDIO_BY_RECORD_NAME, payload);
            }
        }
    }

    public void send(byte msgId2, int... payload) {
        helper.send(msgId2, payload);
    }

}