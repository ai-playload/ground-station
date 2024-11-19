package java.com.example.ground_station.data.utils;

import android.util.Log;

import java.com.example.ground_station.data.service.ResultCallBack;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.util.List;

public class DataUtils {


    public static void parse(byte[] bytes, ResultCallBack<byte[]> callBack) {
        if (bytes != null && bytes.length > 0) {
            int length = bytes.length;
            for (int i = 0; i < length; i++) {
                byte v = bytes[i];
                if (v == SocketConstant.HEADER) {
                    if (i + 2 < length) {
                        byte dataSize = bytes[i + 1];
                        int sumSize = dataSize + 3;

                        if (sumSize + i <= length) {
                            byte[] temp = new byte[sumSize];
                            for (int n = 0; n < sumSize; n++) {
                                temp[n] = bytes[i + n];
                            }
                            i = i + sumSize - 1;
                            if (callBack != null) {
                                callBack.result(temp);
                                String str = Utils.bytesToHexFun3(temp);
                                Log.e("ReadThread", "receive：收到的数据：" + str);
                            }
                        }
                    }
                }
            }
        }
    }
}
