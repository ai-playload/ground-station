package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ground_station.R;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.example.widget.ScaleImage;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.util.DisplayUtils;

public class FloatingDetectorHelper extends BaseFloatingHelper {
    private final String tag = "detector_alarm_tag";
    private final String TAG = "FloatingDetectorHelper";

    public void showFloatingDetector(Context context, CloseCallback closeCallback) {
        startGroundStationService(context, null);

        EasyFloat.with(context)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(true)
                .setTag(tag)
                .setLayout(R.layout.floating_detector,  view -> {
                    RelativeLayout content = view.findViewById(R.id.rlContent);
                    FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) content.getLayoutParams();

                    ScaleImage scaleImage = view.findViewById(R.id.ivScale);
                    scaleImage.setOnScaledListener(new ScaleImage.OnScaledListener() {
                        @Override
                        public void onScaled(float x, float y, MotionEvent event) {
                            params.width = Math.max(params.width + (int) x, DisplayUtils.dpToPx(164));
                            params.height = Math.max(params.height + (int) y, DisplayUtils.dpToPx(160));
                            // Update the size of the floating window
                            content.setLayoutParams(params);
                            // Force redraw the view
//                            content.postInvalidate();

                            EasyFloat.updateFloat(TAG, params.width, params.height);
                        }
                    });
                })
                .registerCallbacks(new OnFloatCallbacks() {
                    @Override
                    public void dragEnd(@NonNull View view) {

                    }

                    @Override
                    public void hide(@NonNull View view) {

                    }

                    @Override
                    public void show(@NonNull View view) {

                    }

                    @Override
                    public void drag(@NonNull View view, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void dismiss() {
                        if (isBound) {
                            groundStationService.sendSocketCommand(SocketConstant.STOP_PLAY_ALARM, 0);
                        }
                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {

                    }

                    @Override
                    public void createdResult(boolean b, @Nullable String s, @Nullable View view) {
                        if (view != null) {
                            initFloatingView(view, tag, closeCallback);

                            view.findViewById(R.id.alarm_1).setOnClickListener(v -> {
                                if (isBound) {
                                    groundStationService.sendDetectorCommand(0);
//                                    groundStationService.sendSocketCommand(SocketConstant.STOP_PLAY_ALARM, "");
//                                    ThreadExtKt.mainThread(50, () -> {
//                                        groundStationService.sendDetectorCommand(0);
//                                        return Unit.INSTANCE;
//                                    });
                                }
                            });

                            view.findViewById(R.id.alarm_2).setOnClickListener(v -> {
                                if (isBound) {
                                    groundStationService.sendDetectorCommand(1);
//                                    groundStationService.sendSocketCommand(SocketConstant.STOP_PLAY_ALARM, "");
//                                    ThreadExtKt.mainThread(50, () -> {
//                                        return Unit.INSTANCE;
//                                    });
                                }
                            });
                            
                            view.findViewById(R.id.alarm_3).setOnClickListener(v -> {
                                if (isBound) {
                                    groundStationService.sendDetectorCommand(2);
//                                    groundStationService.sendSocketCommand(SocketConstant.STOP_PLAY_ALARM, "");
//                                    ThreadExtKt.mainThread(50, () -> {
//                                        return Unit.INSTANCE;
//                                    });
                                }
                            });
                        }
                    }
                })
                .show();
    }

}
