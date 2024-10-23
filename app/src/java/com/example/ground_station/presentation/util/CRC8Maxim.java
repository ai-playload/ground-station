package java.com.example.ground_station.presentation.util;

public class CRC8Maxim {

    private static final int WIDTH = 8;
    private static final int POLY = 0x31; // 生成多项式
    private static final int INIT = 0x00; // 初始值
    private static final int XOROUT = 0x00; // 异或输出
    private static final boolean REF_IN = true; // 输入数据反转
    private static final boolean REF_OUT = true; // 输出数据反转

    public static byte calculateCRC8(byte[] data) {
        int crc = INIT;

        for (byte b : data) {
            int inputByte = REF_IN ? reverseBits(b) : (b & 0xFF);
            crc ^= inputByte;

            for (int j = 0; j < WIDTH; j++) {
                if ((crc & 0x80) != 0) { // 如果最高位为1
                    crc = (crc << 1) ^ POLY;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFF; // 保持CRC在8位内
            }
        }

        return (byte) (REF_OUT ? reverseBits(crc) ^ XOROUT : crc ^ XOROUT);
    }

    private static int reverseBits(int b) {
        int reversed = 0;
        for (int i = 0; i < WIDTH; i++) {
            reversed <<= 1;
            reversed |= (b & 0x01);
            b >>= 1;
        }
        return reversed;
    }
}