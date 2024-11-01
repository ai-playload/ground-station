package java.com.example.ground_station.presentation.floating;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.example.ground_station.R;
import com.lzf.easyfloat.EasyFloat;

import java.com.example.ground_station.data.service.GroundStationService;

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
        }
    }
}
