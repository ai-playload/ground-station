package java.com.example.ground_station.data.model;

public class UploadFileEvent {

    private final boolean success;

    public UploadFileEvent(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
