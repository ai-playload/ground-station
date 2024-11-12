package java.com.example.ground_station.data.utils;

import java.nio.charset.StandardCharsets;

public class Utils {
    /**
     * 截取byte数组   不改变原数组
     *
     * @param b      原数组
     * @param off    偏差值（索引）
     * @param length 长度
     * @return 截取后的数组
     */
    public static byte[] subByte(byte[] b, int off, int length) {
        byte[] b1 = new byte[length];
        System.arraycopy(b, off, b1, 0, length);
        return b1;
    }

    public static String types2String(byte[] bytes) {
        if (bytes == null) {
            return null;
        } else {

            return new String(bytes, 0, bytes.length, StandardCharsets.UTF_8);
        }
    }
}
