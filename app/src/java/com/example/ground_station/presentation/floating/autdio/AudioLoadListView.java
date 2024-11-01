package java.com.example.ground_station.presentation.floating.autdio;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.CommonConstants;
import java.com.example.ground_station.data.model.MediaEvent;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.utils.FileUtils;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.util.MusicFileUtil;
import java.util.List;

public class AudioLoadListView extends AudioBaseListView {
    public AudioLoadListView(Context context) {
        super(context);
    }

    public AudioLoadListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioLoadListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void initView(Context context) {
        type = CommonConstants.TYPE_LOAD;
        super.initView(context);
        selectedListView.setSelectedDataCallback(new AudioDataProvider() {
            @Override
            public void provider(List<AudioModel> selectedList) {
                adapter.submitList(selectedList);
                adapter.notifyDataSetChanged();
                if (selectedList != null && selectedList.size() > 0) {
                //停止本地和网络所有
                send(SocketConstant.STREAMER, SocketConstant.PM.PLAY_BUNCH_STOP);
                send(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, 0, SocketConstant.PM.PLAY_BUNCH_STOP);
                    adapter.other(0);//开始循环播放第一首
                }
            }
        });

        adapter.setOnItemClickListener((audioModel, isPlaying, isPlayingPosition, position) -> {
            Log.d("tag", "isPlaying: " + isPlaying + " isPlayingPosition: " + isPlayingPosition);
            final int fileIndex = adapter.getCurrentList().indexOf(audioModel);
            if (helper.isBound) {
                itemClickLoad(isPlaying, isPlayingPosition, fileIndex, audioModel);
            }
        });

        adapter.setOnItemDeleteListener((audioModel, position) -> {

            String audioFileName = audioModel.getAudioFileName();
            boolean isDeleted = MusicFileUtil.deleteAudioFile(audioFileName);
            if (isDeleted) {
                Toast.makeText(getContext(), "删除成功", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "删除失败", Toast.LENGTH_SHORT).show();
            }

            List<AudioModel> allMp3Files = FileUtils.getAllMp3Files();
            adapter.submitList(allMp3Files);
        });
    }

    protected void itemClickLoad(boolean isPlaying, boolean isPlayingPosition, int fileIndex, AudioModel audioModel) {
        groundStationService.setPlaybackCallback(() -> {
            if (bfFlag == PlayerStates.xh) {
                if (adapter.getCurrentList() != null && adapter.getCurrentList().size() > 0) {
                    int size = adapter.getCurrentList().size();
                    int p = adapter.currentPlayingPosition + 1;//下一首
                    if (p >= size) {
                        p = 0;
                    }
                    p = MathUtils.clamp(p, 0, size - 1);
                    int finalP = p;
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            adapter.other(finalP);
                        }
                    }, 100);
                }
            } else {
                adapter.stopPlay(fileIndex);//暂停这个
            }
        });

        if (!isPlaying && !isPlayingPosition) {
            ShoutcasterConfig.DeviceInfo shouter = groundStationService.getConfig().getShoutcaster();
            String filePath = audioModel.getAudioFilePath();
            String command = String.format(GstreamerCommandConstant.TEXT_TO_SPEECH_COMMAND, filePath, shouter.getIp(), shouter.getPort());
            groundStationService.sendInstruct(SocketConstant.STREAMER, SocketConstant.PM.PLAY_BUNCH_START);
            groundStationService.sendMusicCommand(command);
        } else if (!isPlaying) {
            groundStationService.sendInstruct(SocketConstant.STREAMER, SocketConstant.PM.PLAY_BUNCH_PAUSE);
            groundStationService.pause();
        } else if (isPlayingPosition) {
            groundStationService.sendInstruct(SocketConstant.STREAMER, SocketConstant.PM.PLAY_BUNCH_RECOVER_PLAY);// TODO: 2024/10/24 暂停后恢复播放指令未实现
            groundStationService.play();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void receive(MediaEvent event) {
        if (event.PM == SocketConstant.PM.PLAY_BUNCH_STOP || event.PM == SocketConstant.PM.PLAY_BUNCH_PAUSE) {
            //暂停
            adapter.pause();
        }
    }

}
