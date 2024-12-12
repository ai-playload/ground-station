package java.com.example.ground_station.data.socket;

import java.com.example.ground_station.data.service.ResultCallback;

public interface Clien {

    boolean isConnected();

    void send(byte[] data);

    void connect();

    void update(String ip, String port);

    void update(String ip, int port);

    void disConnect();

    void setConnectCallBack(ConnectionCallback callBack);

    void setCallBack(ResultCallback<byte[]> callBack);
}
