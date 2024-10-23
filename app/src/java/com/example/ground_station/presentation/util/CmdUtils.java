package java.com.example.ground_station.presentation.util;

public class CmdUtils {

    public static byte[] intToByteArray(int value) {
        // 取高 8 位和低 8 位，分别转换为 byte
        byte highByte = (byte) ((value >> 8) & 0xFF);  // 高 8 位
        byte lowByte = (byte) (value & 0xFF);          // 低 8 位

        // 返回包含高低位的 byte 数组
        return new byte[] { highByte, lowByte };
    }

    // CRC8 计算
    public static byte calculateCRC8(byte[] data) {
        int sum = 0;

        // 逐个字节累加
        for (byte b : data) {
            sum += b & 0xFF;  // 将 byte 转换为无符号整数进行累加
        }
        // 取低 8 位的结果
        return (byte) (sum & 0xFF);
    }
}
