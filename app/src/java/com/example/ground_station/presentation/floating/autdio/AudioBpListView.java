package java.com.example.ground_station.presentation.floating.autdio;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;

import androidx.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.CommonConstants;
import java.com.example.ground_station.data.model.MediaEvent;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.util.List;

public class AudioBpListView extends AudioBaseListView {
    public AudioBpListView(Context context) {
        super(context);
    }

    public AudioBpListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public AudioBpListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void initView(Context context) {
        setType(CommonConstants.TYPE_BP);
        super.initView(context);
        selectedListView.setSelectedDataCallback(new AudioDataProvider() {
            @Override
            public void provider(List<AudioModel> selectedList) {
                adapter.submitList(selectedList);
                adapter.notifyDataSetChanged();
                if (selectedList != null && list != null) {
                    //停止本地和网络所有
                    send(SocketConstant.STREAMER, SocketConstant.PM.PLAY_BUNCH_STOP);
                    send(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, 0, SocketConstant.PM.PLAY_BUNCH_STOP);
                    int[] ps = new int[selectedList.size()];
                    for (int i = 0; i < selectedList.size(); i++) {
                        ps[i] = list.indexOf(selectedList.get(i));
                    }
                    send(SocketConstant.PLAY_RECORD_Bp, ps);//循环播放
                }
            }
        });

        adapter.setOnItemClickListener((audioModel, isPlaying, isPlayingPosition, position) -> {
            Log.d("tag", "isPlaying: " + isPlaying + " isPlayingPosition: " + isPlayingPosition);
            final int fileIndex = adapter.getCurrentList().indexOf(audioModel);
            if (helper.isBound) {
                itemClickBp(isPlaying, isPlayingPosition, fileIndex);
            }
        });
        adapter.setOnItemDeleteListener((audioModel, position) -> {
            groundStationService.sendRemoteAudioCommand(SocketConstant.PLAY_REMOTE_AUDIO_BY_INDEX, position, SocketConstant.PM.PLAY_BUNCH_DELETE);
        });
    }

    protected void itemClickBp(boolean isPlaying, boolean isPlayingPosition, int fileIndex) {
        MediaEvent event = new MediaEvent();
        event.PM = SocketConstant.PM.PLAY_BUNCH_STOP;
        EventBus.getDefault().post(event);
        if (!isPlaying && !isPlayingPosition) {
            //groundStationService.sendMusicCommand(command);
            groundStationService.netBpStart(fileIndex);
        } else if (!isPlaying) {
            groundStationService.netBpPause(fileIndex);
        } else if (isPlayingPosition) {
            // TODO: 2024/10/22
            groundStationService.netBpStart(fileIndex);
            //groundStationService.netBpRecoverPlay();
        }
    }


}
