package java.com.example.ground_station.presentation.floating;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import androidx.annotation.NonNull;

import com.example.ground_station.R;
import com.iflytek.aikitdemo.tool.SPUtil;
import com.lzf.easyfloat.EasyFloat;

import java.com.example.ground_station.data.service.GroundStationService;
import java.com.example.ground_station.data.socket.Clien;
import java.com.example.ground_station.data.socket.SocketClientHelper;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.data.view.ConnectStatusView;

public class BaseFloatingHelper {

    public GroundStationService groundStationService;
    public boolean isBound = false;
    public IServiceConnection iServiceConnection;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            GroundStationService.LocalBinder binder = (GroundStationService.LocalBinder) service;
            groundStationService = binder.getService();
            isBound = true;

            groundStationService.isShouting = false;

            if (iServiceConnection != null) {
                iServiceConnection.onServiceConnected();
            }
            onSuccessConnected();
            Log.d("BaseFloatingHelper", "Service connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;

            if (iServiceConnection != null) {
                iServiceConnection.onServiceDisconnected();
            }
            Log.d("BaseFloatingHelper", "Service disconnected");
        }
    };

    public boolean checkService() {
        return groundStationService != null;
    }

    public void onSuccessConnected() {
    }

    public void startGroundStationService(Context context, IServiceConnection iServiceConnection) {
        Intent serviceIntent = new Intent(context, GroundStationService.class);
        context.startService(serviceIntent);
        context.bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE);

        this.iServiceConnection = iServiceConnection;
    }

    public void initFloatingView(View view, String tag, CloseCallback closeCallback) {
        if (view != null) {
            view.findViewById(R.id.close_btn).setOnClickListener(v -> {
                if (closeCallback != null) {
                    closeCallback.onClose();
                }
                EasyFloat.dismiss(tag);
            });
            settingAudioSize(view);
        }
    }

    public void initConnectStatus(View view, Clien clien) {
        ConnectStatusView statusView = view.findViewById(R.id.statusView);
        if (statusView != null) {
            clien.setConnectCallBack(statusView);
            statusView.setStatus(clien.isConnected());
        }
    }

    public void settingAudioSize(@NonNull View view) {
        SeekBar seekBar = view.findViewById(R.id.audio_progressBar);
        if (seekBar != null) {
            SocketClientHelper helper = SocketClientHelper.getMedia();
            int volume = SPUtil.INSTANCE.getInt("audio_volume", 60);
            helper.send(SocketConstant.SET_VOLUME, volume);
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
                        helper.send(SocketConstant.SET_VOLUME, volume);
                        SPUtil.INSTANCE.putBase("audio_volume", volume);
                    }
                    Log.d("Audio setting", "volume value: " + volume);
                }
            });
        }
    }
}
