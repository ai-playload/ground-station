package java.com.example.ground_station.data.utils;

import android.os.Looper;
import android.widget.EditText;

import java.util.List;

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

    public static String bytesToHexFun3(List<byte[]> list) {
        StringBuilder sb = new StringBuilder();
        for (byte[] bytes : list) {
            String s = bytesToHexFun3(bytes);
            sb.append(s);
            sb.append("|");
        }
        return sb.toString();
    }

    /**
     * 方法三：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun3(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }

    public static boolean isMainTread() {
        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    /**
     * 检查端口和IP
     * @param edIp
     * @param edPort
     * @param tag
     * @return
     */
    public static String[] checkIpProt(EditText edIp, EditText edPort, String tag) {
        String ip = edIp.getText().toString().trim();
        String port = edPort.getText().toString().trim();
        if (ip.isEmpty()) {
            edIp.setError(tag + " IP 不能为空");
            return null;
        }
        if (port.isEmpty()) {
            edPort.setError(tag + "端口不能为空");
            return null;
        }
        return new String[]{ip, port};
    }
}
