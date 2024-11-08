package java.com.example.ground_station.data.model;

public class ShoutcasterConfig {

    private final DeviceInfo sjInfo;
    private DeviceInfo shoutcaster;
    private DeviceInfo controller;
    private DeviceInfo cloudLightInfo;

    public ShoutcasterConfig(DeviceInfo shoutcaster, DeviceInfo controller, DeviceInfo cloudLightInfo, DeviceInfo sjInfo) {
        this.shoutcaster = shoutcaster;
        this.controller = controller;
        this.cloudLightInfo = cloudLightInfo;
        this.sjInfo = sjInfo;
    }

    public DeviceInfo getSjInfo() {
        return sjInfo;
    }

    public DeviceInfo getCloudLightInfo() {
        return cloudLightInfo;
    }

    public void setCloudLightInfo(DeviceInfo cloudLightInfo) {
        this.cloudLightInfo = cloudLightInfo;
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

        public DeviceInfo(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}

