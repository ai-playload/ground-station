package java.com.example.ground_station.presentation.util;

import java.io.IOException;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class SendUtils {

    public static byte[] toData(byte msgId2, int payload) throws IOException {
        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;
        byte payloadByte = (byte) payload;
        int len = 3;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[5];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = payloadByte;

        byte checksum = calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

//        send(fullPacket);
        return fullPacket;
    }

    // CRC8 计算
    private static byte calculateCRC8(byte[] data) {
        Checksum crc = new CRC32();  // CRC32 为例，你可能需要换成 CRC8 实现
        crc.update(data, 0, data.length);
        long crcValue = crc.getValue();
        return (byte) (crcValue & 0xFF);
    }

//    public static void sendStr(@NotNull String msgId2, @NotNull String payloadValue) {
//
//
//    }


    public static byte[] sendStr(String msgId2Str, String payload) throws IOException {

        byte msgId2 = (byte) Integer.parseInt(msgId2Str.substring(2), 16);

        boolean hasP = payload != null && payload.length() >= 3;

        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;

        int len = hasP ? 3 : 2;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[len + 2];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;

        packetWithoutChecksum[3] = msgId2;

        if (hasP) {
        byte p = (byte) Integer.parseInt(payload.substring(2), 16);
            byte payloadByte = (byte) p;
            packetWithoutChecksum[4] = payloadByte;
        }

        byte checksum = calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

//        send(fullPacket);
        return fullPacket;
    }
}
