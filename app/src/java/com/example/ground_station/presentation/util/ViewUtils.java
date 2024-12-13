package java.com.example.ground_station.presentation.util;

import android.view.View;

public class ViewUtils {

    public static void setVisible(View v, boolean visiblity) {
        if (v != null) {
            v.setVisibility(visiblity ? View.VISIBLE :View.GONE);
        }
    }
    public static void setVisibility(View v, boolean visiblity) {
        if (v != null) {
            v.setVisibility(visiblity ? View.VISIBLE :View.GONE);
        }
    }
}
