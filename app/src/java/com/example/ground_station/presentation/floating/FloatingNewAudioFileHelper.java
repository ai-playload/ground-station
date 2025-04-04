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

import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.R;
import com.google.android.material.tabs.TabLayout;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.ConnectionCallback;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.view.ConnectStatusView;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.floating.adapter.AudioAdapter;
import java.com.example.ground_station.presentation.floating.dialog.FloatingDeleteDialog;
import java.com.example.ground_station.presentation.fun.file.FileInfoUtils;
import java.com.example.ground_station.presentation.fun.file.SardineCallBack;
import java.com.example.ground_station.presentation.util.MusicFileUtil;
import java.com.example.ground_station.presentation.util.ViewUtils;
import java.util.ArrayList;
import java.util.List;

public class FloatingNewAudioFileHelper extends BaseFloatingHelper {

    public static final int AUDIO_REQUEST_CODE = 9;
    private final String tag = "audio_file_tag";
    private AudioAdapter adapter;
    private AudioAdapter remoteAdapter;
    private boolean isAudioPlayEnding = false;
    private boolean isRemotePlay = false;
    private int currentRemoteAudioPosition = -1;
    private boolean isListLoopStatus = false;

    SocketClientHelper helper = SocketClientHelper.getMedia();

    public void showFloatingAudioFile(AppCompatActivity activity, CloseCallback closeCallback) {
        startGroundStationService(activity, new IServiceConnection() {
            @Override
            public void onServiceConnected() {
                getRemoteAudioList();
            }

            @Override
            public void onServiceDisconnected() {

            }
        });

        EasyFloat.with(activity).setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true).setTag(tag)
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
                }).registerCallbacks(new OnFloatCallbacks() {
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
                        helper.setConnectCallBack(null);
                        if (isBound) {
                            groundStationService.cancelGstreamerAudioCommand();
//                            send(SocketConstant.AMPLIFIER, 2);

                            if (isRemotePlay) {
                                List<AudioModel> currentList = remoteAdapter.getCurrentList();
                                if (currentList != null && currentList.size() > currentRemoteAudioPosition && currentRemoteAudioPosition >= 0) {
                                    AudioModel audioModel = currentList.get(currentRemoteAudioPosition);
//                                  send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, currentRemoteAudioPosition, 2);
                                    sendAudioInstruct(audioModel, 2);
                                }
                            } else {
                                send(SocketConstant.STREAMER, 2);//停止播放
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
                }).show();
    }

    private void initView(View view, AppCompatActivity activity) {
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        RecyclerView remoteRecyclerView = view.findViewById(R.id.remote_recyclerview);

        initRecyclerView(recyclerView);
        initRemoteRecyclerView(remoteRecyclerView);

        initConnectStatus(view, helper);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);

        // 添加两个选项
        tabLayout.addTab(tabLayout.newTab().setText("本地音频"));
        tabLayout.addTab(tabLayout.newTab().setText("远程音频"));

        // TabLayout点击事件监听
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                boolean load = tab.getPosition() == 0;
                tabPositionChange(load, recyclerView, remoteRecyclerView);
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
//            if (tabLayout.getSelectedTabPosition() == 1) {
//                List<AudioModel> currentList = remoteAdapter.getCurrentList();
//                int position = remoteAdapter.getCurrentPlayingPosition();
//                if (currentList != null && currentList.size() > position && position >= 0) {
//                    AudioModel audioModel = currentList.get(position);
//                    if (audioModel.isPlaying()) {
//                        if (isListLoopStatus) {
//                            playRmoteAudio(audioModel);
//                        } else {
//                            audioModel.setPlaying(false);
//                            remoteAdapter.notifyDataSetChanged();
//                            sendAudioInstruct(audioModel, 2);
//                        }
//                    }
//                }
//            }
        });

        view.findViewById(R.id.audio_delete_btn).setOnClickListener(v -> {
            int position = isRemotePlay ? remoteAdapter.getCurrentPlayingPosition() : adapter.getCurrentPlayingPosition();
            int size = isRemotePlay ? remoteAdapter.getItemCount() : adapter.getItemCount();

            if (position == -1 || size == 0) {
                Toast.makeText(recyclerView.getContext(), "请选择文件", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isRemotePlay) {
                List<AudioModel> currentList = remoteAdapter.getCurrentList();
                if (currentList != null && currentList.size() > position && position >= 0) {
                    AudioModel audioModel = remoteAdapter.getCurrentItem(position);
                    if (FileInfoUtils.isBjs(audioModel.getAudioFilePath())) {
                        ToastUtils.showShort("警报声不可删除");
                        return;
                    }
                }
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
                        AudioAdapter audioAdapter = isRemotePlay ? remoteAdapter : adapter;
                        List<AudioModel> currentList = audioAdapter.getCurrentList();
                        if (currentList != null && currentList.size() > position) {
                            AudioModel audioModel = currentList.get(position);
                            sendAudioInstruct(audioModel, 3);
//                        send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, position, 3);
                        }
                        getRemoteAudioList();
                    }
                }

                @Override
                public void onCancel() {

                }
            });
        });
        tabLayout.getTabAt(1).select();
        tabPositionChange(false, recyclerView, remoteRecyclerView);
    }

    private static void tabPositionChange(boolean load, RecyclerView recyclerView, RecyclerView remoteRecyclerView) {
        ViewUtils.setVisibility(recyclerView, load);
        ViewUtils.setVisibility(remoteRecyclerView, !load);
    }

    private void initRemoteRecyclerView(RecyclerView remoteRecyclerView) {
        remoteAdapter = new AudioAdapter((audioModel, isPlaying, isPlayingPosition, position) -> {
            Log.d(tag, "isPlaying: " + isPlaying + " isPlayingPosition: " + isPlayingPosition);
            if (!isRemotePlay) { //播放远程音频的时候如果此时是本地播放，将停止本地播放
                isRemotePlay = true;
                groundStationService.cancelGstreamerAudioCommand();
            }

            if (FileInfoUtils.isBjs(audioModel.getAudioFilePath())) {
                if ((!isPlaying && !isPlayingPosition) || (isPlaying && isPlayingPosition)) {
                    if (isListLoopStatus) {
                        int index = position + 100;//100~102
                        helper.send(SocketConstant.PLAY_REMOTE_AUDIO_BY_RECORD_NAME, index);
                    } else {
                        helper.send(SocketConstant.PLAY_ALARM, position);
                    }
                } else if (!isPlaying) {//暂停
                    sendAudioInstruct(audioModel, 2);
                }
                return;
            }

            currentRemoteAudioPosition = position;

            if (isBound) {
                if (!isPlaying && !isPlayingPosition) {
//                    helper.send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, position, 1);
//                    sendAudioInstruct(audioModel, 1);
                    playRmoteAudio(audioModel);
                } else if (!isPlaying) {
//                    helper.send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, position, 2);
                    sendAudioInstruct(audioModel, 2);
                } else if (isPlayingPosition) {
//                    helper.send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, position, 1);
//                    sendAudioInstruct(audioModel, 1);
                    playRmoteAudio(audioModel);
                }
            }
        });

        remoteAdapter.setOnItemDeleteListener((audioModel, position) -> {
//            send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, position, 3);
            sendAudioInstruct(audioModel, 3);
        });

        remoteRecyclerView.setLayoutManager(new LinearLayoutManager(remoteRecyclerView.getContext()));
        remoteRecyclerView.setAdapter(remoteAdapter);

        remoteAdapter.submitList(FileInfoUtils.getBjs());
    }

    private void playRmoteAudio(AudioModel audioModel) {
        if (isListLoopStatus) {
            //循环播放
            int payload1 = getAudioPayload(audioModel);
            if (payload1 >= 0) {
                helper.send(SocketConstant.PLAY_REMOTE_AUDIO_BY_RECORD_NAME, payload1);
            }
        } else {
            sendAudioInstruct(audioModel, 1);
        }
    }

    private void sendAudioInstruct(AudioModel audioModel, int payload2) {
        int payload1 = getAudioPayload(audioModel);
        if (payload1 >= 0) {
            helper.send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, payload1, payload2);
        }
    }

    private int getAudioPayload(AudioModel audioModel) {
        if (audioModel != null) {
            String audioFileName = audioModel.getAudioFileName();
            int payload = FileInfoUtils.file2Payload(audioFileName);
            if (payload >= 0) {
                return payload;
            }
        }
        return -1;
    }


    private void getRemoteAudioList() {
        groundStationService.getWebdavFiles(new SardineCallBack<List<AudioModel>>() {
            @Override
            public void getResult(List<AudioModel> audioModelList) {
                if (audioModelList != null) {
                    audioModelList.addAll(0, FileInfoUtils.getBjs());
                }
                remoteAdapter.submitList(audioModelList);
            }
        });
    }

    public void initRecyclerView(RecyclerView recyclerView) {
        adapter = new AudioAdapter((audioModel, isPlaying, isPlayingPosition, position) -> {
            Log.d(tag, "isPlaying: " + isPlaying + " isPlayingPosition: " + isPlayingPosition);
            if (isRemotePlay) { //当前是远程播放就停止远程播放
                isRemotePlay = false;
//                send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, position, 2);
                sendAudioInstruct(audioModel, 2);
            }

            if (isBound) {
                groundStationService.setPlaybackCallback(() -> {
//                    send(SocketConstant.AMPLIFIER, 2);
                    if (isListLoopStatus) {
                        adapter.playLoopAudio();
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
//                    send(SocketConstant.AMPLIFIER, 1);
                    send(SocketConstant.STREAMER, 1);
                    isAudioPlayEnding = false;
                } else if (!isPlaying) {
                    groundStationService.pause();
                    send(SocketConstant.STREAMER, 3);
                } else if (isPlayingPosition) {
                    groundStationService.play();
                    send(SocketConstant.STREAMER, 4);
                }
            }
        });

        adapter.setOnItemDeleteListener((audioModel, position) -> {
            if (FileInfoUtils.isBjs(audioModel.getAudioFilePath())) {
                ToastUtils.showShort("警报声不可删除");
                return;
            }
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
        return x >= location[0] && x <= location[0] + view.getWidth() && y >= location[1] && y <= location[1] + view.getHeight();
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

    public void send(byte msgId2, int... payload) {
        helper.send(msgId2, payload);
    }
}
