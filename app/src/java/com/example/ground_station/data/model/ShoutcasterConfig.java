package java.com.example.ground_station.data.model;

import android.text.TextUtils;

import com.iflytek.aikitdemo.tool.SPUtil;

public class ShoutcasterConfig {

    private final DeviceInfo sjInfo;
    private DeviceInfo shoutcaster;
    private DeviceInfo controller;

    private static final String TAG_LIGHT = "cloud_light_";
    private static DeviceInfo cloudLightInfo = new DeviceInfo(TAG_LIGHT);

    public ShoutcasterConfig(DeviceInfo shoutcaster, DeviceInfo controller, DeviceInfo sjInfo) {
        this.shoutcaster = shoutcaster;
        this.controller = controller;
        this.cloudLightInfo = cloudLightInfo;
        this.sjInfo = sjInfo;
    }

    public DeviceInfo getSjInfo() {
        return sjInfo;
    }

    public static DeviceInfo getCloudLightInfo() {
        return cloudLightInfo;
    }


    public DeviceInfo getShoutcaster() {
        return shoutcaster;
    }

    public void setShoutcaster(DeviceInfo shoutcaster) {
        this.shoutcaster = shoutcaster;
    }

    public DeviceInfo getController() {
        return controller;
    }

    public void setController(DeviceInfo controller) {
        this.controller = controller;
    }

    public static class DeviceInfo {
        private String ip;
        private int port;
        private String tag;


        private DeviceInfo(String tag) {
            this.tag = tag;
        }

        public DeviceInfo(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            if (TextUtils.isEmpty(ip)) {
                ip = SPUtil.INSTANCE.getString(tag + "ip", "");
            }
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
            SPUtil.INSTANCE.putBase(tag + "ip", ip);
        }

        public int getPort() {
            if (port == 0) {
                String v = SPUtil.INSTANCE.getString(tag + "port", "0");
                port = Integer.parseInt(v);
            }
            return port;
        }

        public void setPort(int port) {
            this.port = port;
            SPUtil.INSTANCE.putBase(tag + "port", String.valueOf(port));
        }

        public void setPort(String port) {
            int v = Integer.parseInt(port);
            setPort(v);
        }
    }
}

