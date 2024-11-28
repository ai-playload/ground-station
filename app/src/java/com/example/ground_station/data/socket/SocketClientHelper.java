package java.com.example.ground_station.data.socket;

import java.com.example.ground_station.data.service.ResultCallBack;

public class SocketClientHelper {

    Clien client;

    private SocketClientHelper() {
        client = new Sc();
    }

    private static SocketClientHelper dessnt = new SocketClientHelper();
    private static SocketClientHelper media = new SocketClientHelper();

    public synchronized static SocketClientHelper getDessent() {
        return dessnt;
    }

    public synchronized static SocketClientHelper getMedia() {
        return media;
    }

    public Clien getClient() {
        return client;
    }

}

class Sc implements Clien{

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void send(byte[] data) {

    }

    @Override
    public void connect() {

    }

    @Override
    public void update(String ip, String port) {

    }

    @Override
    public void update(String ip, int port) {

    }

    @Override
    public void disConnect() {

    }

    @Override
    public void setConnectCallBack(ConnectionCallback callBack) {

    }

    @Override
    public void setCallBack(ResultCallBack<byte[]> callBack) {

    }
}
