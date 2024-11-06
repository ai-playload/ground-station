package java.com.example.ground_station.data.utils;

import org.greenrobot.eventbus.EventBus;

import java.com.example.ground_station.presentation.floating.autdio.FloatingAudioFileHelper2;

public class Bus {


    public static void regsiter(Object obj) {
        if (!EventBus.getDefault().isRegistered(obj)) {
            EventBus.getDefault().register(obj);
        }
    }

    public static void unRegsiter(Object obj) {
        if (EventBus.getDefault().isRegistered(obj)) {
            EventBus.getDefault().unregister(obj);
        }
    }
}
