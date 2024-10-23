package java.com.example.ground_station.presentation.floating.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ground_station.R;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.ShoutcasterConfig;
import java.com.example.ground_station.data.service.GroundStationService;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.GstreamerCommandConstant;
import java.com.example.ground_station.presentation.floating.adapter.AudioAdapter;
import java.com.example.ground_station.presentation.util.MusicFileUtil;
import java.util.ArrayList;
import java.util.List;

public class LocalAudioFragment extends Fragment {
    public static String TAG = "LocalAudioFragment";
    private AudioAdapter adapter;
    private GroundStationService groundStationService;
    private boolean isBound = false;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GroundStationService.LocalBinder binder = (GroundStationService.LocalBinder) service;
            groundStationService = binder.getService();
            isBound = true;
            Log.d(TAG, "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
            Log.d(TAG, "Service disconnected");
        }
    };
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        startGsService();

        View view = inflater.inflate(R.layout.fragment_local_audio, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        initAudioPlayer(recyclerView);

        return view;
    }

    private void startGsService() {
        Intent serviceIntent = new Intent(getContext(), GroundStationService.class);
        getContext().startService(serviceIntent);
        getContext().bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private void initAudioPlayer(RecyclerView recyclerView) {
        adapter = new AudioAdapter((audioModel, isPlaying, isPlayingPosition, position) -> {
            Log.d(TAG, "isPlaying: " + isPlaying + " isPlayingPosition: " + isPlayingPosition);

            if (isBound) {
                groundStationService.setPlaybackCallback( ()-> {
                    groundStationService.sendSocketCommand(SocketConstant.STREAMER, 2);
                });

                ShoutcasterConfig.DeviceInfo shoutcaster = groundStationService.getConfig().getShoutcaster();

                String filePath = audioModel.getAudioFilePath();
                String command = String.format(GstreamerCommandConstant.TEXT_TO_SPEECH_COMMAND, filePath, shoutcaster.getIp(), shoutcaster.getPort());

                if (!isPlaying && !isPlayingPosition) {
                    groundStationService.sendMusicCommand(command);
                    groundStationService.sendSocketCommand(SocketConstant.STREAMER, 1);
                } else if (!isPlaying) {
                    groundStationService.pause();
                } else if (isPlayingPosition) {
                    groundStationService.play();
                }
            }
        });

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        List<AudioModel> allMp3Files = getAllMp3Files();
        adapter.submitList(allMp3Files);

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
}