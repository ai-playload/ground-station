package java.com.example.ground_station.presentation.util;

import android.util.DisplayMetrics;

import com.iflytek.aikitdemo.MyApp;

public class DisplayUtils {

    /**
     * Converts dp (density-independent pixels) to pixels.
     *
     * @param dp The value in dp (density-independent pixels) to convert.
     * @return The converted value in pixels.
     */
    public static int dpToPx(int dp) {
        DisplayMetrics displayMetrics = MyApp.Companion.getCONTEXT().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    /**
     * Converts dp (density-independent pixels) to pixels.
     *
     * @param dp The value in dp (density-independent pixels) to convert.
     * @return The converted value in pixels.
     */
    public static float dpToPxFloat(float dp) {
        DisplayMetrics displayMetrics = MyApp.Companion.getCONTEXT().getResources().getDisplayMetrics();
        return dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}

