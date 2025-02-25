package java.com.example.ground_station.presentation.floating;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.math.MathUtils;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.R;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputLayout;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import com.lzf.easyfloat.utils.InputMethodUtils;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.service.ResultCallBack;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.ability.AudioFileGenerationCallback;
import java.com.example.ground_station.presentation.ability.tts.TtsHelper2;
import java.com.example.ground_station.presentation.floating.autdio.AudioFormatBean;
import java.com.example.ground_station.presentation.util.DisplayUtils;
import java.com.example.ground_station.presentation.util.DownloadUtil;
import java.com.example.ground_station.presentation.util.TCPFileUploader;
import java.io.File;
import java.util.List;

public class FloatingTextToSpeechHelper extends BaseFloatingHelper {
    private final String tag = "text_to_speech_tag";
    private final String TAG = "TextToSpeechHelper";
    private boolean isCheckedLoop = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable delayedTask;
    private CheckBox checkBox;
    private static int fileNameFlag = 0;
    private AppCompatSeekBar seekBarFb;

    public void showFloatingTextToSpeech(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                int volume = SPUtil.INSTANCE.getInt("audio_volume", 100);
                groundStationService.sendSetVolumeCommand(volume);

                groundStationService.getAiSoundHelper().setVCN("xiaofeng");
                groundStationService.setPlaybackCallback(() -> {
//                    if (isCheckedLoop) {
//                        playGstreamerMusic();
//                    } else {
//                        groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
//                        groundStationService.sendSocketCommand(SocketConstant.STOP_TALK, 0);
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
                .setLayout(R.layout.floating_text_to_speech, view -> {
                    RelativeLayout content = view.findViewById(R.id.rlContent);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();

                    ScaleImage scaleImage = view.findViewById(R.id.ivScale);
                    scaleImage.setOnScaledListener(new ScaleImage.OnScaledListener() {
                        @Override
                        public void onScaled(float x, float y, MotionEvent event) {
                            params.width = Math.max(params.width + (int) x, DisplayUtils.dpToPx(164));
                            params.height = Math.max(params.height + (int) y, DisplayUtils.dpToPx(320));
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
                            groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
                            groundStationService.cancelGstreamerAudioCommand();

                            groundStationService.getAiSoundHelper().stop();
                        }

                        // 取消延迟任务
                        if (delayedTask != null) {
                            handler.removeCallbacks(delayedTask);
                        }
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    }

                    @Override
                    public void createdResult(boolean success, @Nullable String s, @Nullable View view) {
                        if (view != null) {
                            initFloatingView(view, tag, closeCallback);

                            TextInputLayout textInputLayout = view.findViewById(R.id.audioContent);
                            EditText editText = textInputLayout.getEditText();

                            RadioGroup radioGroup = view.findViewById(R.id.radio_group);
                            MaterialRadioButton maleRadioButton = view.findViewById(R.id.radio_button_male);
                            maleRadioButton.setChecked(true);

                            Button speakButton = view.findViewById(R.id.speak_button);
                            checkBox = view.findViewById(R.id.loop_check_box);

                            editText.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    InputMethodUtils.openInputMethod(editText, tag);
                                }
                            });

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

                            TextView ylFbTv = view.findViewById(R.id.ylFbTv);
                            seekBarFb = view.findViewById(R.id.seek_bar_fb);
                            int volumeFb = SPUtil.INSTANCE.getInt("audio_volume_fb", 29);
                            seekBarFb.setProgress(volumeFb);
                            updateBs(volumeFb, ylFbTv);
                            seekBarFb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                    int volumeFb = seekBarFb.getProgress();
                                    updateBs(volumeFb, ylFbTv);
                                    SPUtil.INSTANCE.putBase("audio_volume_fb", volumeFb);
                                    Log.d(TAG, "volume value: " + volumeFb);
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
//                                        groundStationService.sendSocketCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, 2);
                                        groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, 0, 2);
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
                                    String inputStr = editText.getText().toString().trim();
                                    if (!inputStr.isEmpty()) {
                                        groundStationService.getTtsHelper().setFileName(fileNameFlag++);
                                        String fileName = groundStationService.getTtsHelper().getFileName();

                                        if (checkBox.isChecked()) {
                                            formartMp3(inputStr, fileName);
                                        } else {
                                            groundStationService.generateAudioFile(inputStr, new AudioFileGenerationCallback() {
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

                                                    ShoutcasterConfig.DeviceInfo shoutcaster = groundStationService.getConfig().getShoutcaster();

                                                    String command = String.format(GstreamerCommandConstant.TEXT_TO_SPEECH_COMMAND, filePath, shoutcaster.getIp(), shoutcaster.getPort());
                                                    groundStationService.sendMusicCommand(command);
                                                    Log.d(TAG, "onAudioFileGenerationEnd " + filePath + " duration: " + duration);
                                                }
                                            });
                                            groundStationService.getTtsHelper().setFileWriteListener(new TtsHelper2.IFileWriteListener() {
                                                @Override
                                                public void onSuccess(File file) {
                                                    Log.d("TtsEngine", "onSuccess 回调 file" + file.getName());
                                                    boolean equals = StringUtils.equals(file.getName(), fileName);
                                                    if (equals) {
                                                        playGstreamerMusic();
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
                                        }
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

    private void playGstreamerMusic() {
        ShoutcasterConfig shoutcasterConfig = groundStationService.getConfig();

        File recordFile = groundStationService.getAiSoundHelper().getRecordFile();
        if (recordFile != null && shoutcasterConfig.getShoutcaster() != null) {
            if (!checkBox.isChecked()) {
                groundStationService.sendSocketCommand(SocketConstant.STREAMER, 1);
                String filePath = recordFile.getPath();
                String command = String.format(GstreamerCommandConstant.TEXT_TO_SPEECH_COMMAND, filePath, shoutcasterConfig.getShoutcaster().getIp(), shoutcasterConfig.getShoutcaster().getPort());
                groundStationService.sendMusicCommand(command);
            } else {
                uploadAudioFile(recordFile);
            }
        }
    }

    /**
     *
     */
    private void formartMp3(String str, String fileName) {
        AudioFormatBean info = new AudioFormatBean();
        info.text = str;
        info.filename = fileName;
        if (seekBarFb != null) {
            String fb = getBs(seekBarFb.getProgress());
            info.DB = fb;
        } else {
            info.DB = "1";
        }

        info.num = "1";
        new DownloadUtil().download(info, new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(@Nullable File file) {
                uploadAudioFile(file);
            }

            @Override
            public void onDownloading(int progress) {

            }

            @Override
            public void onDownloadFailed(@Nullable Exception e) {
                String message = e.getMessage();
                ToastUtils.showLong("从服务器获取文字转语音文件失败：" + message);
            }
        });
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
                    playBpFile(file);
                }
            }));
        }
    }

    private void playBpFile(File file) {
        groundStationService.getAudioListInfo(new ResultCallBack<List<AudioModel>>() {
            @Override
            public void result(List<AudioModel> audioModelList) {
                if (audioModelList != null) {
                    String name = file.getName();
                    for (int i = 0; i < audioModelList.size(); i++) {
                        AudioModel audioModel = audioModelList.get(i);
                        String audioFileName = audioModel.getAudioFileName();
                        if (StringUtils.equals(audioFileName, name)) {
                            int fileIndex = i;
                            groundStationService.sendSocketCommand(SocketConstant.PLAY_RECORD_Bp, fileIndex);
                            Log.d("uploadAudioFile", "文件：" + name + "  循环播放指令fileIndex: " + fileIndex);
                            return;
                        }

                    }
                    Log.d("uploadAudioFile", "文件：" + name + "  循环播放指令fileIndex: " + -1);
                } else {
                    Log.d("uploadAudioFile", "文件：" + "null" + "  循环播放指令fileIndex: ");
                }
            }
        });
    }

    private static void updateBs(int volume, TextView ylBsTv) {
        String bs = getBs(volume);
        ylBsTv.setText("提升音量(分贝)：" + bs);
    }

    private static @NonNull String getBs(int volume) {
        int v = volume + 1;
        v = MathUtils.clamp(v, 1, 30);
        return v + "";
    }

}