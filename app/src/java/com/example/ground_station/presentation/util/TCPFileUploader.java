package java.com.example.ground_station.presentation.util;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.aikitdemo.tool.SPUtil;

import org.greenrobot.eventbus.EventBus;

import java.com.example.ground_station.data.model.UploadFileEvent;
import java.com.example.ground_station.data.socket.SocketClient;
import java.com.example.ground_station.presentation.callback.UploadProgressListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPFileUploader {

    private String serverIp;    // 服务器 IP 地址
    private int serverPort;     // 服务器端口号
    private ExecutorService executorService;

    public TCPFileUploader() {
        this.serverIp = SPUtil.INSTANCE.getString("controller_ip", "");  // 从 SharedPreferences 获取 IP
        this.serverPort = 8518;  // 固定端口号
        this.executorService = Executors.newSingleThreadExecutor(); // 创建一个单线程线程池
    }

    /**
     * 上传文件到服务器
     *
     * @param filePath 需要上传的文件路径
     */
    public void uploadFile(Activity activity, String filePath, UploadProgressListener uploadProgressListener) {
        executorService.execute(() -> {
            SocketClient client = new SocketClient(serverIp, serverPort);
            try {
                client.connect(serverIp, serverPort);
                client.uploadFile(new File(filePath), uploadProgressListener);
                client.disconnect();

                activity.runOnUiThread(()-> {
                    Toast.makeText(activity, "上传文件成功", Toast.LENGTH_SHORT).show();
                });
                EventBus.getDefault().post(new UploadFileEvent(true));
            } catch (IOException e) {
                activity.runOnUiThread(()-> {
                    Toast.makeText(activity, "上传文件失败：" + e, Toast.LENGTH_SHORT).show();
                });

                Log.e("TCPFileUploader", "上传文件失败: " + e);
            }
        });
    }
}