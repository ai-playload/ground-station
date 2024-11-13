package java.com.example.ground_station.data.socket;

import android.util.Log;

import java.com.example.ground_station.presentation.callback.ResultCallback;
import java.com.example.ground_station.presentation.callback.UploadProgressListener;
import java.com.example.ground_station.presentation.util.CRC8Maxim;
import java.com.example.ground_station.presentation.util.SendUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;

public class SocketClient {
    private static final int CONNECTION_TIMEOUT_MS = 5000; // Set a 5-second timeout
    private static final int BYTE_LENGTH = 6;

    private String serverIp;
    private int serverPort;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    public SocketClient(String serverIp, int serverPort) {
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        socket = new Socket();
    }

    public void connect(String serverIp, int serverPort) throws IOException {
        this.serverIp = serverIp;
        this.serverPort = serverPort;

        try {
            socket.connect(new InetSocketAddress(serverIp, serverPort), CONNECTION_TIMEOUT_MS);
            outputStream = socket.getOutputStream();
            inputStream = socket.getInputStream();
        } catch (SocketTimeoutException e) {
            throw new IOException("Connection timed out", e);
        }
    }

    public void sendCommand(byte msgId2, int payload) throws IOException {

        // 1. Header (固定值 0x8d)
        byte header = SocketConstant.HEADER;

        // 2. MSG_ID1 (固定值 0x01)
        byte msgId1 = SocketConstant.MSG_ID1;
        // 4. Payload (控制方向)
        byte payloadByte = (byte) payload;

        // 5. Len (数据长度)
        // MSG_ID1 (1) + MSG_ID2 (1) + Payload (1)
        int len = 1 + 1 + 1;
        byte lenByte = (byte) len;

        // 6. 构造消息体（不包括 Checksum）
        byte[] packetWithoutChecksum = new byte[5];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = payloadByte;

        // 7. 计算 Checksum (CRC8)
        byte checksum = CRC8Maxim.calculateCRC8(packetWithoutChecksum);

        // 8. 构造完整消息
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        // 9. 发送完整消息
        send(fullPacket);
    }

    public void sendRemoteAudioCommand(byte msgId2, int payload, int playState) throws IOException {

        // 1. Header (固定值 0x8d)
        byte header = SocketConstant.HEADER;

        // 2. MSG_ID1 (固定值 0x01)
        byte msgId1 = SocketConstant.MSG_ID1;
        // 4. Payload (控制方向)
        byte payloadByte = (byte) payload;
        // 5. Len (数据长度)
        // MSG_ID1 (1) + MSG_ID2 (1) + Payload (1) + playState (1)
        int len = 1 + 1 + 1 + 1;
        byte lenByte = (byte) len;

        // 6. 构造消息体（不包括 Checksum）
        byte[] packetWithoutChecksum = new byte[6];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = payloadByte;
        packetWithoutChecksum[5] = (byte) playState;

        // 7. 计算 Checksum (CRC8)
        byte checksum = CRC8Maxim.calculateCRC8(packetWithoutChecksum);

        // 8. 构造完整消息
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        // 9. 发送完整消息
        send(fullPacket);
    }


    /**
     * 发送设置音量命令到服务器
     *
     * @param volume 音量大小，取值范围 0-100
     * @throws IOException 如果发送过程中发生 I/O 错误
     */
    public void sendSetVolumeCommand(int volume) throws IOException {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }

        // 1. Header (固定值 0x8d)
        byte header = (byte) 0x8d;

        // 2. MSG_ID1 (固定值 0x01)
        byte msgId1 = (byte) 0x01;

        // 3. MSG_ID2 (设置音量的 ID, 固定值 0x0e)
        byte msgId2 = (byte) 0x0e;

        // 4. Payload (音量大小, 转换为 16 进制并填充)
        byte volumeByte = (byte) volume;

        // 5. Len (数据长度)
        // MSG_ID1 (1) + MSG_ID2 (1) + Payload (1)
        int len = 1 + 1 + 1;
        byte lenByte = (byte) len;

        // 6. 构造消息体（不包括 Checksum）
        byte[] packetWithoutChecksum = new byte[5];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = volumeByte;

        // 7. 计算 Checksum (CRC8)
        byte checksum = CRC8Maxim.calculateCRC8(packetWithoutChecksum);

        // 8. 构造完整消息
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        // 9. 发送完整消息
        send(fullPacket);
    }

    /**
     * 发送舵机控制命令到服务器
     *
     * @param direction 控制方向，上发送1，下发送2，居中发送5
     * @throws IOException 如果发送过程中发生 I/O 错误
     */
    public void sendServoCommand(int direction) throws IOException {
//        if (direction != 1 && direction != 2 && direction != 5) {
//            throw new IllegalArgumentException("Invalid direction, must be 1 (up), 2 (down), or 5 (center)");
//        }

        // 1. Header (固定值 0x8d)
        byte header = SocketConstant.HEADER;

        // 2. MSG_ID1 (固定值 0x01)
        byte msgId1 = SocketConstant.MSG_ID1;

        // 3. MSG_ID2 (舵机控制的 ID, 固定值为0x30)
        byte msgId2 = SocketConstant.SERVO;

        // 4. Payload (控制方向)
        byte directionByte = (byte) direction;

        // 5. Len (数据长度)
        // MSG_ID1 (1) + MSG_ID2 (1) + Payload (1)
        int len = 1 + 1 + 1;
        byte lenByte = (byte) len;

        // 6. 构造消息体（不包括 Checksum）
        byte[] packetWithoutChecksum = new byte[5];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = directionByte;

        // 7. 计算 Checksum (CRC8)
        byte checksum = CRC8Maxim.calculateCRC8(packetWithoutChecksum);

        // 8. 构造完整消息
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        // 9. 发送完整消息
        send(fullPacket);
    }

    /**
     * 发送舵机控制命令到服务器
     *
     * @param payload 警报指令，1-3
     * @throws IOException 如果发送过程中发生 I/O 错误
     */
    public void sendDetectorCommand(int payload) throws IOException {

        // 1. Header (固定值 0x8d)
        byte header = SocketConstant.HEADER;

        // 2. MSG_ID1 (固定值 0x01)
        byte msgId1 = SocketConstant.MSG_ID1;

        // 3. MSG_ID2 (舵机控制的 ID, 固定值为0x30)
        byte msgId2 = SocketConstant.PLAY_ALARM;

        // 4. Payload (控制方向)
        byte directionByte = (byte) payload;

        // 5. Len (数据长度)
        // MSG_ID1 (1) + MSG_ID2 (1) + Payload (1)
        int len = 1 + 1 + 1;
        byte lenByte = (byte) len;

        // 6. 构造消息体（不包括 Checksum）
        byte[] packetWithoutChecksum = new byte[5];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = directionByte;

        // 7. 计算 Checksum (CRC8)
        byte checksum = CRC8Maxim.calculateCRC8(packetWithoutChecksum);

        // 8. 构造完整消息
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        // 9. 发送完整消息
        send(fullPacket);
    }

    /**
     * 上传文件到服务器
     *
     * @param file 要上传的文件对象
     * @throws IOException 如果上传过程中发生 I/O 错误
     */
    public void uploadFile(File file, UploadProgressListener listener) throws IOException {
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file");
        }

        // 1. 读取文件内容到字节数组
        FileInputStream fileInputStream = new FileInputStream(file);
        byte[] buffer = new byte[4096]; // 每次读取 4KB
        int bytesRead;

        // 2. 获取文件总大小
        long fileSize = file.length();
        long totalBytesRead = 0; // 已读取的字节数

        // 3. 发送文件名和文件大小
        String fileName = file.getName();
        sendFileMetadata(fileName, fileSize, "/data/play/");
        String response = receiveResponse();
        Log.d("uploadAudioFile", " 接收字符串：" + response);
        // 4. 逐块读取并发送文件内容，同时计算进度
        while ((bytesRead = fileInputStream.read(buffer)) != -1) {
            totalBytesRead += bytesRead; // 累加已读取的字节数
            send(buffer, 0, bytesRead); // 发送读取到的数据

            // 计算上传进度
            int progress = (int) ((totalBytesRead * 100) / fileSize);

            // 通过回调接口通知进度更新
            if (listener != null) {
                listener.onProgressUpdate(progress);
            }
        }

        // 5. 关闭文件流
        fileInputStream.close();

        // 6. 通知服务器文件已上传完毕
        sendEndOfFileNotification();
    }

    /**
     * 发送文件的元数据（文件名和文件大小）
     *
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @throws IOException 如果发送过程中发生 I/O 错误
     */
    private void sendFileMetadata(String fileName, long fileSize, String parentPath) throws IOException {
        // 构造文件元数据：文件名 + 文件大小
        String metadata = parentPath + "|" + fileName + "|" + fileSize;
        byte[] metadataBytes = metadata.getBytes(StandardCharsets.UTF_8);

        // 发送文件元数据
        send(metadataBytes);
    }

    /**
     * 发送结束文件标志
     *
     * @throws IOException 如果发送过程中发生 I/O 错误
     */
    private void sendEndOfFileNotification() throws IOException {
        // 自定义协议：可以发送一个特殊字节来表示文件传输结束
        byte[] endOfFileMarker = new byte[]{0x04}; // 0x04 表示文件结束
        send(endOfFileMarker);
    }

    private void send(byte[] data, int offset, int length) throws IOException {
        if (outputStream != null) {
            outputStream.write(data, offset, length);
            outputStream.flush();
        }
    }

    // 发送字节数组到输出流
    private void send(byte[] data) throws IOException {
        if (outputStream != null) {
            outputStream.write(data);
            outputStream.flush();
        }
    }

    public String receiveResponse() throws IOException {
        if (inputStream == null) {
            return null;
        }
        byte[] buffer = new byte[1024];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            return null;
        }
        return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
    }

    public String receiveResponse(int size) throws IOException {
        if (inputStream == null) {
            return null;
        }
        byte[] buffer = new byte[size];
        int bytesRead = inputStream.read(buffer);
        if (bytesRead == -1) {
            return null;
        }
        return new String(buffer, 0, bytesRead, StandardCharsets.UTF_8);
    }

    public void disconnect() throws IOException {
        if (outputStream != null) {
            outputStream.close();
        }
        if (socket != null) {
            socket.close();
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public byte[] receiveResponseByte(int size) throws IOException {
        if (inputStream == null) {
            return null;
        }
        byte[] buffer = new byte[size];
        int bytesRead = inputStream.read(buffer, 0, size);
        if (bytesRead == -1) {
            return null;
        }

        byte[] bytes = subByte(buffer, 0, bytesRead);
        return bytes;
    }

    public byte[] subByte(byte[] b, int off, int length) {
        byte[] b1 = new byte[length];
        System.arraycopy(b, off, b1, 0, length);
        return b1;
    }

    public void jsX(ResultCallback<byte[]> callback) {
        if (inputStream == null) {
            return;
        }
        try {
            while (true) {
                byte[] bytes = new byte[BYTE_LENGTH];
                // 读取客户端发送的信息
                int count = inputStream.read(bytes, 0, BYTE_LENGTH);
                if (count > 0) {
                    callback.result(bytes);
                    // 接收到消息打印
                    System.out.println("接收到客户端的信息是:" + new String(bytes).trim());
                }
                count = 0;
            }
        } catch (Exception e) {

        }
    }

    public void send(byte msgId2, int... payload) throws IOException {
        byte[] data = SendUtils.toData(msgId2, payload);
        send(data);
    }
}

