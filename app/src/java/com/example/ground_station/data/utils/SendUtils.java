package java.com.example.ground_station.data.utils;

import android.util.Log;

import java.com.example.ground_station.presentation.util.CRC8Maxim;
import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class SendUtils {

    /**
     * 指令转换成byte[]
     * @param msgId2
     * @param payload
     * @return
     * @throws IOException
     */
    public static byte[] toData(String msgId2, String... payload) throws IOException {
        byte v = (byte) Integer.parseInt(msgId2, 16);
        int[] ps = new int[payload.length];
        for (int i = 0; i < payload.length; i++) {
            ps[i] =  Integer.parseInt(payload[i], 16);
        }
        return toData(v, ps);
    }

    public static byte[] toData(byte msgId2, int... payload)  {
        int len = 2 + payload.length;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[len + 2];

        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;

        packetWithoutChecksum[3] = msgId2;//指令
        for (int i = 0; i < payload.length; i++) {//参数
            int index = i + 4;
            int value = payload[i];
            packetWithoutChecksum[index] = (byte) value;//强转byte
        }

        byte checksum = CRC8Maxim.calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;
        Log.d("Send Data", "msgId2=" +msgId2 + " " + payload.toString() );
        return fullPacket;
    }

}
