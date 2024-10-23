package java.com.example.ground_station.data.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class UdpSocketClient {
    private static final int CONNECTION_TIMEOUT_MS = 1000; // Set a timeout if necessary
    private String serverIp;
    private int serverPort;
    private DatagramSocket socket;
    private InetAddress serverAddress;

    public UdpSocketClient(String serverIp, int serverPort) throws IOException {
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.serverAddress = InetAddress.getByName(serverIp);
        socket = new DatagramSocket();  // 使用默认的本地端口
    }

    // 发送字节数组到服务器
    private void send(byte[] data) throws IOException {
        DatagramPacket packet = new DatagramPacket(data, data.length, serverAddress, serverPort);
        socket.send(packet);
    }

    public void sendCommand(byte msgId2, String payload) throws IOException {
        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;
        byte[] payloadBytes = payload.getBytes(StandardCharsets.UTF_8);

        int len = 1 + 1 + 1 + 1 + payloadBytes.length;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[len];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        System.arraycopy(payloadBytes, 0, packetWithoutChecksum, 4, payloadBytes.length);

        byte checksum = calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        send(fullPacket);
    }

    public void sendCommand(byte msgId2, int payload) throws IOException {
        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;
        byte payloadByte = (byte) payload;
        int len = 1 + 1 + 1 + 1 + 1;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[len];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = payloadByte;

        byte checksum = calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        send(fullPacket);
    }

    public void sendSetVolumeCommand(int volume) throws IOException {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume must be between 0 and 100");
        }
        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;
        byte msgId2 = (byte) 0x0e;
        byte volumeByte = (byte) volume;
        int len = 1 + 1 + 1 + 1 + 1;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[len];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = volumeByte;

        byte checksum = calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        send(fullPacket);
    }

    public void sendServoCommand(int direction) throws IOException {
        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;
        byte msgId2 = (byte) 0x30;
        byte directionByte = (byte) direction;
        int len = 1 + 1 + 1 + 1 + 1;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[len];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = directionByte;

        byte checksum = calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        send(fullPacket);
    }

    public void sendDetectorCommand(int payload) throws IOException {
        byte header = (byte) 0x8d;
        byte msgId1 = (byte) 0x01;
        byte msgId2 = (byte) 0x30;
        byte payloadByte = (byte) payload;
        int len = 1 + 1 + 1 + 1 + 1;
        byte lenByte = (byte) len;

        byte[] packetWithoutChecksum = new byte[len];
        packetWithoutChecksum[0] = header;
        packetWithoutChecksum[1] = lenByte;
        packetWithoutChecksum[2] = msgId1;
        packetWithoutChecksum[3] = msgId2;
        packetWithoutChecksum[4] = payloadByte;

        byte checksum = calculateCRC8(packetWithoutChecksum);
        byte[] fullPacket = new byte[packetWithoutChecksum.length + 1];
        System.arraycopy(packetWithoutChecksum, 0, fullPacket, 0, packetWithoutChecksum.length);
        fullPacket[fullPacket.length - 1] = checksum;

        send(fullPacket);
    }

    // CRC8 计算
    private byte calculateCRC8(byte[] data) {
        Checksum crc = new CRC32();  // CRC32 为例，你可能需要换成 CRC8 实现
        crc.update(data, 0, data.length);
        long crcValue = crc.getValue();
        return (byte) (crcValue & 0xFF);
    }

    // 接收响应数据
    public String receiveResponse() throws IOException {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        socket.receive(packet);
        return new String(packet.getData(), 0, packet.getLength(), StandardCharsets.UTF_8);
    }

    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }
}