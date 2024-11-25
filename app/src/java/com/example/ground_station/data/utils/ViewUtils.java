package java.com.example.ground_station.data.utils;

import android.view.View;

public class ViewUtils {

    public static void setVisibility(View view, boolean visible) {
        if (view != null) {
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

}
