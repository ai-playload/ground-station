package java.com.example.ground_station.presentation.floating.autdio;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ground_station.R;
import com.google.android.material.tabs.TabLayout;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.iflytek.aikitdemo.tool.ThreadExtKt;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.CommonConstants;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.floating.BaseFloatingHelper;
import java.com.example.ground_station.presentation.floating.CloseCallback;
import java.com.example.ground_station.presentation.util.DisplayUtils;
import java.com.example.ground_station.presentation.util.GsonParser;
import java.com.example.ground_station.presentation.util.MusicFileUtil;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class FloatingAudioFileHelper2 extends BaseFloatingHelper {
    private final String tag = "audio_file_tag";
    private final String TAG = "FloatingAudioFileHelper";
    static Activity activity;
    private int bfFlag = PlayerStates.normal;
    TabLayout tabLayout;
    boolean isLoad = true;

    private AudioLoadListView c0;
    private AudioBpListView c1;

    public void showFloatingAudioFile(AppCompatActivity activity, CloseCallback closeCallback) {
        startGroundStationService(activity, null);
        this.activity = activity;
        EasyFloat.with(activity)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true)
                .setTag(tag)
                .setLayout(R.layout.floating_audio_file2, view -> {
                    RelativeLayout content = view.findViewById(R.id.rlContent);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();

                    ScaleImage scaleImage = view.findViewById(R.id.ivScale);
                    scaleImage.setOnScaledListener(new ScaleImage.OnScaledListener() {
                        @Override
                        public void onScaled(float x, float y, MotionEvent event) {
                            params.width = Math.max(params.width + (int) x, DisplayUtils.dpToPx(196));
                            params.height = Math.max(params.height + (int) y, DisplayUtils.dpToPx(260));
                            content.setLayoutParams(params);

                            EasyFloat.updateFloat(TAG, params.width, params.height);
                        }
                    });

                    initView(view, activity);
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
                            groundStationService.cancelGstreamerAudioCommand();
                            groundStationService.sendSocketCommand(SocketConstant.STOP_TALK, 0);
                        }
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
                        handleTouchEvent(view, motionEvent);
                    }

                    @Override
                    public void createdResult(boolean b, @Nullable String s, @Nullable View view) {
                        initFloatingView(view, tag, closeCallback);

                    }
                })
                .show();
    }

    @Override
    public void onSuccessConnected() {
        c0.setObj(groundStationService);
        c1.setObj(groundStationService);
        List data = c1.getAdapter().getCurrentList();
        if (data == null || data.size() == 0) {
            getOriAudioData(c1);
        }
    }

    private void initView(View view, AppCompatActivity activity) {
        tabLayout = view.findViewById(R.id.tab_layout);
        // 添加两个选项
        tabLayout.addTab(tabLayout.newTab().setText("本地音频"));
        tabLayout.addTab(tabLayout.newTab().setText("远程音频"));

        c0 = view.findViewById(R.id.c0);
        c0.setType(CommonConstants.TYPE_LOAD);
        c0.setObj(this);
        List<AudioModel> allMp3Files = getAllMp3Files();
        c0.submitList(allMp3Files);

        c1 = view.findViewById(R.id.c1);
        c1.setType(CommonConstants.TYPE_BP);
        c1.setObj(this);

        // TabLayout点击事件监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                isLoad = tab.getPosition() == 0;
                c0.setVisibility(isLoad ? View.VISIBLE : View.GONE);
                c1.setVisibility(!isLoad ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        view.findViewById(R.id.button).setOnClickListener(v -> {
            selectAudioFileFromFloatingWindow(activity);
        });
        //音量调整
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
    }

    private void getOriAudioData(AudioBaseListView audioListView) {
        groundStationService.sendSocketThanReceiveCommand(SocketConstant.GET_RECORD_LIST, 0, () -> {
            groundStationService.receiveResponse(response -> {
                if (response != null && response.length() > 1) {
                    Log.d(TAG, "ttkx Received response: " + response);

                    // 查找最后一个 ']' 的位置，并截取到该位置为止
                    int lastIndex = response.lastIndexOf("]");
                    if (lastIndex != -1) {
                        response = response.substring(0, lastIndex + 1);  // 保留到最后的 ']'
                    }

                    Log.d(TAG, "Received response after modification: " + response);

                    try {
                        GsonParser gsonParser = new GsonParser();
                        List<AudioModel> audioModelList = getAllRemoteAudioToAudioModel(gsonParser.parseAudioFileList(response));
                        Log.d(TAG, "Received response: " + response);
                        audioListView.submitList(audioModelList);

                    } catch (Exception e) {
                        Log.e(TAG, " error: " + e);
                    }

                    ThreadExtKt.mainThread(() -> {
                        return Unit.INSTANCE;
                    });
                }
            });
        });
    }

    private void handleTouchEvent(View view, MotionEvent motionEvent) {
        RecyclerView recyclerView = view.findViewById(isLoad ? R.id.c0 : R.id.c1).findViewById(R.id.recyclerview);
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (isTouchInsideView(recyclerView, motionEvent)) {
                    EasyFloat.dragEnable(false, tag);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                EasyFloat.dragEnable(true, tag);
                break;
        }
    }

    private boolean isTouchInsideView(View view, MotionEvent motionEvent) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        float x = motionEvent.getRawX();
        float y = motionEvent.getRawY();
        return x >= location[0] && x <= location[0] + view.getWidth() &&
                y >= location[1] && y <= location[1] + view.getHeight();
    }


    public static List<AudioModel> getAllMp3Files() {
        List<AudioModel> audioModelList = new ArrayList<>();
        List<String> filePaths = MusicFileUtil.getAllAudioFiles();
//        if (activity != null) {
//            String s = FloatingAudioFileHelper.copyAssetGetFilePath(activity, "1.mp3");
//            filePaths.add(s);
//        }

        for (String filePath : filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            audioModelList.add(new AudioModel(fileName, filePath, false));
        }
        return audioModelList;
    }

    private List<AudioModel> getAllRemoteAudioToAudioModel(List<String> filePaths) {
        List<AudioModel> audioModelList = new ArrayList<>();
        for (String filePath : filePaths) {
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            audioModelList.add(new AudioModel(fileName, filePath, false));
        }
        return audioModelList;
    }

    private void selectAudioFileFromFloatingWindow(AppCompatActivity activity) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(Intent.createChooser(intent, "选择音频文件"), CommonConstants.AUDIO_REQUEST_CODE);
    }

}
