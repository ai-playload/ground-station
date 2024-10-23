package java.com.example.ground_station.presentation.util;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.iflytek.aikitdemo.tool.SPUtil;
import com.iflytek.aikitdemo.tool.ThreadExtKt;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.security.Security;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;

import kotlin.Unit;

public class SFTPHelper {

    // 上传文件的 SFTP 配置
    private static final String SFTP_HOST = "81.70.35.199";  // SFTP 服务器地址
    private static final String SFTP_USER = "sz";   // SFTP 用户名
    private static final String SFTP_PASSWORD = "456";  // SFTP 密码
    private static final int SFTP_PORT = 22;  // SFTP 端口号
    private static final String SFTP_REMOTE_DIR = "/data/play";  // 远程服务器目录
    private Context context;
    private SFTPCallBack sftpCallBack;

    public SFTPHelper(Context context) {
        this.context = context;
        // Add the BouncyCastle provider
        Security.addProvider(new BouncyCastleProvider());
        Log.d("SFTP", "Providers: " + Arrays.toString(Security.getProviders()));
    }

    private void showMainThreadToast(String text) {
        ThreadExtKt.mainThread(() -> {
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            return Unit.INSTANCE;
        });
    }

    // 上传文件到服务器
    public void uploadFile(String localFilePath, SFTPCallBack sftpCallBack) {
        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            // 1. 连接 SFTP 服务器
            String savedControllerIp = SPUtil.INSTANCE.getString("controller_ip", "");

            session = jsch.getSession(SFTP_USER, savedControllerIp, SFTP_PORT);
            session.setPassword(SFTP_PASSWORD);

            // 跳过主机密钥检查
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig("kex", "diffie-hellman-group1-sha1");
            session.setConfig(config);

            session.connect();

            // 2. 打开 SFTP 通道
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            // 3. 上传文件
            File file = new File(localFilePath);
            FileInputStream fileInputStream = new FileInputStream(file);

            // 切换到远程目录并上传文件
            channelSftp.cd(SFTP_REMOTE_DIR);
            channelSftp.put(fileInputStream, file.getName());

            fileInputStream.close();
            channelSftp.disconnect();
            session.disconnect();

            Log.d("SFTP", "文件上传成功: " + file.getName());
            if (sftpCallBack != null) {
                sftpCallBack.upLoadFileSuccess();
            }
            showMainThreadToast("文件上传成功");
        } catch (Exception e) {
            Log.e("SFTP", "文件上传失败: " + e.getMessage());
            showMainThreadToast("文件上传失败: + " + e.getMessage());

            if (sftpCallBack != null) {
                sftpCallBack.upLoadFileFailed();
            }
            e.printStackTrace();
        } finally {
            if (channelSftp != null) {
                channelSftp.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
        }
    }

    public interface SFTPCallBack {
        void upLoadFileSuccess();

        void upLoadFileFailed();
    }
}