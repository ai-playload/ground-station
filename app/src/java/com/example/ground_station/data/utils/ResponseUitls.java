package java.com.example.ground_station.data.utils;

import android.util.Log;

import java.com.example.ground_station.data.socket.SocketConstant;
import java.io.IOException;

public class ResponseUitls {


    public static Byte getSocketResult(byte[] bytes, byte msgId2) {
        if (bytes != null && bytes.length >= 5) {
            if (bytes[0] == SocketConstant.HEADER && bytes[3] == msgId2) {
                // TODO: 2024/11/12 未校验 bytes[5]
                return bytes[4];
            }
        }
        return null;
    }
}
