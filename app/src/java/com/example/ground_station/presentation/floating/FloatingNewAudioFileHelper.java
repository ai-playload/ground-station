package java.com.example.ground_station.presentation.floating;

import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ground_station.R;
import com.google.android.material.tabs.TabLayout;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.iflytek.aikitdemo.tool.ThreadExtKt;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.floating.adapter.AudioAdapter;
import java.com.example.ground_station.presentation.floating.dialog.FloatingDeleteDialog;
import java.com.example.ground_station.presentation.util.GsonParser;
import java.com.example.ground_station.presentation.util.MusicFileUtil;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

public class FloatingNewAudioFileHelper extends BaseFloatingHelper {

    public static final int AUDIO_REQUEST_CODE = 9;
    private final String tag = "audio_file_tag";
    private final String TAG = "FloatingAudioFileHelper";
    private AudioAdapter adapter;
    private AudioAdapter remoteAdapter;
    private boolean isAudioPlayEnding = false;
    private boolean isRemotePlay = false;
    private int currentRemoteAudioPosition = -1;
    private boolean isSingleStatus = false;
    private boolean isSingleLoopStatus = false;
    private boolean isListLoopStatus = false;

    public void showFloatingAudioFile(AppCompatActivity activity, CloseCallback closeCallback) {
        startGroundStationService(activity, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                int volume = SPUtil.INSTANCE.getInt("audio_volume", 100);
                groundStationService.sendSetVolumeCommand(volume);

                getRemoteAudioList();
            }

            @Override
            public void onServiceDisconnected() {

            }
        });

        EasyFloat.with(activity)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true)
                .setTag(tag)
                .setLayout(R.layout.floating_new_audio_file, view -> {
                    initFloatingView(view, tag, closeCallback);
                    initView(view, activity);

//                    TextView textView = view.findViewById(R.id.audio_loop_select);
//                    textView.setOnClickListener(v -> {
//                        textView.setSelected(!textView.isSelected());
//                    });
//
//                    TabLayout tabLayout = view.findViewById(R.id.tab_layout);
//
//                    // 添加两个选项
//                    tabLayout.addTab(tabLayout.newTab().setText("本地音频"));
//                    tabLayout.addTab(tabLayout.newTab().setText("远程音频"));
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
//                            groundStationService.sendSocketCommand(SocketConstant.AMPLIFIER, 2);

                            if (isRemotePlay) {
                                groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, currentRemoteAudioPosition, 2);
                            } else {
                                groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);//停止播放
                            }
                        }
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
                        handleTouchEvent(view, motionEvent);
                    }

                    @Override
                    public void createdResult(boolean b, @Nullable String s, @Nullable View view) {
                    }
                })
                .show();
    }

    private void initView(View view, AppCompatActivity activity) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        RecyclerView remoteRecyclerView = view.findViewById(R.id.remote_recyclerview);

//        RadioGroup playGroup = view.findViewById(R.id.play_group);

        initRecyclerView(recyclerView);
        initRemoteRecyclerView(remoteRecyclerView);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        // 添加两个选项
        tabLayout.addTab(tabLayout.newTab().setText("本地音频"));
        tabLayout.addTab(tabLayout.newTab().setText("远程音频"));

        // TabLayout点击事件监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    recyclerView.setVisibility(View.VISIBLE);
                    remoteRecyclerView.setVisibility(View.GONE);
                } else {
                    recyclerView.setVisibility(View.GONE);
                    remoteRecyclerView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        view.findViewById(R.id.upload_button).setOnClickListener(v -> {
            selectAudioFileFromFloatingWindow(activity);
        });

        TextView textView = view.findViewById(R.id.audio_loop_select);
        textView.setOnClickListener(v -> {
            boolean isSelected = !textView.isSelected();
            textView.setSelected(isSelected);
            isListLoopStatus = isSelected;
        });

        view.findViewById(R.id.audio_delete_btn).setOnClickListener(v -> {
            int position = isRemotePlay ? remoteAdapter.getCurrentPlayingPosition() : adapter.getCurrentPlayingPosition();
            int size = isRemotePlay ? remoteAdapter.getItemCount() : adapter.getItemCount();

            if (position == -1 || size == 0) {
                Toast.makeText(recyclerView.getContext(), "请选择文件", Toast.LENGTH_SHORT).show();
                return;
            }

            new FloatingDeleteDialog().showDeleteDialog(recyclerView.getContext(), new FloatingDeleteDialog.DeleteDialogCallback() {
                @Override
                public void onDelete() {
                    //本地删除
                    if (!isRemotePlay) {
                        AudioModel currentItem = adapter.getCurrentItem(position);
                        boolean isDeleted = MusicFileUtil.deleteAudioFile(currentItem.getAudioFileName());
                        if (isDeleted) {
                            Toast.makeText(recyclerView.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(recyclerView.getContext(), "删除失败", Toast.LENGTH_SHORT).show();
                        }
                        //更新数据
                        List<AudioModel> allMp3Files = getAllMp3Files();
                        adapter.submitList(allMp3Files);
                    } else {
                        //远程删除
                        groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, position, 3);
                        getRemoteAudioList();
                    }
                }

                @Override
                public void onCancel() {

                }
            });
        });
    }

    private void initRemoteRecyclerView(RecyclerView remoteRecyclerView) {
        remoteAdapter = new AudioAdapter((audioModel, isPlaying, isPlayingPosition, position) -> {
            Log.d(tag, "isPlaying: " + isPlaying + " isPlayingPosition: " + isPlayingPosition);
            if (!isRemotePlay) { //播放远程音频的时候如果此时是本地播放，将停止本地播放
                isRemotePlay = true;
                groundStationService.cancelGstreamerAudioCommand();
            }

            currentRemoteAudioPosition = position;

            if (isBound) {
                if (!isPlaying && !isPlayingPosition) {
                    groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, position, 1);
                } else if (!isPlaying) {
                    groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, position, 2);
                } else if (isPlayingPosition) {
                    groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, position, 1);
                }
            }
        });

        remoteAdapter.setOnItemDeleteListener((audioModel, position) -> {
            groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, position, 3);
        });

        remoteRecyclerView.setLayoutManager(new LinearLayoutManager(remoteRecyclerView.getContext()));
        remoteRecyclerView.setAdapter(remoteAdapter);
    }

    private void getRemoteAudioList() {
        groundStationService.sendSocketThanReceiveCommand(SocketConstant.GET_RECORD_LIST, 0, () -> {
            ThreadExtKt.mainThread(500, () -> {
                groundStationService.receiveResponse(response -> {
                    Log.d(TAG, "Received response before: " + response);

                    if (response != null && response.length() > 1) {
                        // 查找第一个 '[' 和最后一个 ']' 的位置
                        int firstIndex = response.indexOf("[");
                        int lastIndex = response.lastIndexOf("]");

                        // 如果 '[' 和 ']' 都存在，且顺序正确
                        if (firstIndex != -1 && lastIndex != -1 && firstIndex < lastIndex) {
                            // 截取从 '[' 到 ']' 之间的内容
                            response = response.substring(firstIndex, lastIndex + 1);  // 保留到最后的 ']'
                        }

                        Log.d(TAG, "Received response after modification: " + response);

                        try {
                            GsonParser gsonParser = new GsonParser();
                            List<AudioModel> audioModelList = getAllRemoteAudioToAudioModel(gsonParser.parseAudioFileList(response));
                            remoteAdapter.submitList(audioModelList);

                        } catch (Exception e) {
                            Log.e(TAG, " error: " + e);
                        }
                    }
                });

                return Unit.INSTANCE;
            });
        });
    }


    public void initRecyclerView(RecyclerView recyclerView) {
        adapter = new AudioAdapter((audioModel, isPlaying, isPlayingPosition, position) -> {
            Log.d(tag, "isPlaying: " + isPlaying + " isPlayingPosition: " + isPlayingPosition);
            if (isRemotePlay) { //当前是远程播放就停止远程播放
                isRemotePlay = false;
                groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, position, 2);
            }

            if (isBound) {
                groundStationService.setPlaybackCallback(() -> {
//                    groundStationService.sendSocketCommand(SocketConstant.AMPLIFIER, 2);

                    if (isListLoopStatus) {
                        adapter.playNextAudio();
                    } else {
                        isAudioPlayEnding = true;
                        adapter.getCurrentItem(position).setPlaying(false);
                        adapter.notifyItemChanged(position);
                    }
                });

                ShoutcasterConfig.DeviceInfo shouter = groundStationService.getConfig().getShoutcaster();

                String filePath = audioModel.getAudioFilePath();
                String command = String.format(GstreamerCommandConstant.TEXT_TO_SPEECH_COMMAND, filePath, shouter.getIp(), shouter.getPort());

                //0x9b  0x01 创建播放 0x02 停止播放，0x03 暂停播放 ， 0x04 恢复播放
                if (!isPlaying && !isPlayingPosition || isAudioPlayEnding) {
                    groundStationService.sendMusicCommand(command);
//                    groundStationService.sendSocketCommand(SocketConstant.AMPLIFIER, 1);
                    groundStationService.sendSocketCommand(SocketConstant.STREAMER, 1);
                    isAudioPlayEnding = false;
                } else if (!isPlaying) {
                    groundStationService.pause();
                    groundStationService.sendSocketCommand(SocketConstant.STREAMER, 3);
                } else if (isPlayingPosition) {
                    groundStationService.play();
                    groundStationService.sendSocketCommand(SocketConstant.STREAMER, 4);
                }
            }
        });

        adapter.setOnItemDeleteListener((audioModel, position) -> {

            String audioFileName = audioModel.getAudioFileName();
            boolean isDeleted = MusicFileUtil.deleteAudioFile(audioFileName);
            if (isDeleted) {
                Toast.makeText(recyclerView.getContext(), "删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(recyclerView.getContext(), "删除失败", Toast.LENGTH_SHORT).show();
            }

            List<AudioModel> allMp3Files = getAllMp3Files();
            adapter.submitList(allMp3Files);
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);

        List<AudioModel> allMp3Files = getAllMp3Files();
        adapter.submitList(allMp3Files);
    }

    private void handleTouchEvent(View view, MotionEvent motionEvent) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
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

    private List<AudioModel> getAllMp3Files() {
        List<AudioModel> audioModelList = new ArrayList<>();
        List<String> filePaths = MusicFileUtil.getAllAudioFiles();
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
        activity.startActivityForResult(Intent.createChooser(intent, "选择音频文件"), AUDIO_REQUEST_CODE);
    }
}
