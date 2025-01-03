package java.com.example.ground_station.data.utils;

import com.blankj.utilcode.util.SPUtils;

public class FunctionManager {

    private static FunctionManager manager = new FunctionManager();
    private Callback callback;

    private FunctionManager() {
    }

    public static FunctionManager getInstance() {
        return manager;
    }

    public void addChangeListener(Callback callback) {
        this.callback = callback;
    }

    public void removeAllListener() {
        callback = null;
    }

    public boolean getHasFunction(String fcFlavor) {
        return SPUtils.getInstance().getBoolean(fcFlavor, true);
    }

    public void updateFunction(String fcFlavor, boolean has) {
        boolean hasFunction = getHasFunction(fcFlavor);
        SPUtils.getInstance().put(fcFlavor, has);
        if (hasFunction != has) {
            if (callback != null) {
                callback.callback(fcFlavor, has);
            }
        }
    }

    public interface Callback {
        void callback(String flavor, boolean show);
    }
}
