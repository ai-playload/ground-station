package java.com.example.ground_station.presentation.util;

import android.util.Log;

public class Utils {

    /**
     * 方法三：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes) {
        if (bytes != null) {
            StringBuilder buf = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) { // 使用String的format方法进行转换
                buf.append(String.format("%02x", new Integer(b & 0xff)));
            }
            return buf.toString();
        } else {
            return "null";
        }
    }

    /**
     * 将16进制字符串转换为byte[]
     *
     * @param str
     * @return
     */
    public static byte[] toBytes(String str) {
        if (str == null || str.trim().equals("")) {
            return new byte[0];
        }

        byte[] bytes = new byte[str.length() / 2];
        for (int i = 0; i < str.length() / 2; i++) {
            String subStr = str.substring(i * 2, i * 2 + 2);
            bytes[i] = (byte) Integer.parseInt(subStr, 16);
        }

        return bytes;
    }

    public static void test() {
        byte[] v = toBytes("8a3e");
        String s = bytesToHexFun3(v);
        Log.d("tnak", s);
    }
}
