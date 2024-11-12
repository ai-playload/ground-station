package java.com.example.ground_station.data.socket;

public interface MessageListener {
    void onMessageReceived(byte msg1, byte msg2, byte[] payload);
}
