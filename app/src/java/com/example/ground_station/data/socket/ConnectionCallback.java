package java.com.example.ground_station.data.socket;

import java.io.IOException;

public interface ConnectionCallback {
    void onConnectionSuccess();
    void onConnectionFailure(Exception e);
}
