package java.com.example.ground_station.presentation.floating;

import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ground_station.BuildConfig;
import com.example.ground_station.R;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

import java.com.example.ground_station.data.utils.Utils;
import java.com.example.ground_station.data.utils.ViewUtils;
import java.com.example.ground_station.presentation.screen.MainActivity;
import java.com.example.ground_station.presentation.floating.autdio.FloatingAudioFileHelper2;
import java.util.HashMap;
import java.util.Map;

public class FloatingWindowHelper {
    public static final String tag = "floating_window";

    private static final long DEBOUNCE_DELAY_MS = 400; // 防抖延迟时间（毫秒）
    private long lastClickTime = 0;

    private ImageView audioBtn;
    private ImageView textToSpeechBtn;
    private ImageView audioFileBtn;
    private ImageView lightBtn;
    private ImageView detectorAlarmBtn;
    private ImageView topMainActivityBtn;

    private ImageView currentSelectedBtn;

    public void showFloatingWindow(AppCompatActivity activity) {
        EasyFloat.with(activity)
                .setLayout(R.layout.floating_side_layout)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.RESULT_RIGHT)
                .setGravity(Gravity.RIGHT, 0, 0)
                .setDragEnable(true)
                .setTag(tag)
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

            }

            @Override
            public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {

            }

            @Override
            public void createdResult(boolean b, @Nullable String s, @Nullable View view) {

                audioBtn = view.findViewById(R.id.audio_btn);
                textToSpeechBtn = view.findViewById(R.id.text_to_speech_btn);
                audioFileBtn = view.findViewById(R.id.audio_file_btn);
                lightBtn = view.findViewById(R.id.light_btn);
                detectorAlarmBtn = view.findViewById(R.id.detector_alarm_btn);
                topMainActivityBtn = view.findViewById(R.id.tv_options_to_main_btn);

                View[] btns = {audioBtn, textToSpeechBtn, audioFileBtn, lightBtn, detectorAlarmBtn, topMainActivityBtn};
                for (View btn : btns) {
                    Object tag = btn.getTag();
                    if (tag != null) {
                     String str = tag.toString();
                     boolean show = Utils.flavorVisible(BuildConfig.FLAVOR, str);//不同的
                     ViewUtils.setVisibility(btn, show);
                    }
                }

                view.findViewById(R.id.btnParent).requestLayout();

                View.OnClickListener clickListener = v -> {
                    toggleButtonSelection((ImageView) v, activity);
                };

                audioBtn.setOnClickListener(clickListener);
                textToSpeechBtn.setOnClickListener(clickListener);
                audioFileBtn.setOnClickListener(clickListener);
                lightBtn.setOnClickListener(clickListener);
                detectorAlarmBtn.setOnClickListener(clickListener);
                topMainActivityBtn.setOnClickListener(v -> {
                    Context context = v.getContext().getApplicationContext();
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                });
//                        currentSelectedBtn = audioBtn;
            }
        }).show();
    }

    private void toggleButtonSelection(ImageView selectedBtn, AppCompatActivity activity) {
        // 当前时间
        long currentTime = SystemClock.elapsedRealtime();

        // 检查是否在防抖延迟时间内点击
        if (currentTime - lastClickTime < DEBOUNCE_DELAY_MS) {
            return; // 忽略点击
        }
        // 更新上次点击时间
        lastClickTime = currentTime;

        closeAllFloatingWindows();

        if (currentSelectedBtn != null) {
            if (currentSelectedBtn == selectedBtn) {
                setSelectedImage(currentSelectedBtn.getId(), currentSelectedBtn, false);
                currentSelectedBtn = null;
                return;
            } else {
                setSelectedImage(currentSelectedBtn.getId(), currentSelectedBtn, false);
            }
        }

        setSelectedImage(selectedBtn.getId(), selectedBtn, true);
        currentSelectedBtn = selectedBtn; // Update the currently selected button

        if (selectedBtn == audioBtn) {
            new FloatingNewAudioHelper().showFloatingNewAudio(selectedBtn.getContext(), () -> {
                changeCloseBackground();
            });
        } else if (selectedBtn == textToSpeechBtn) {
            new FloatingTextToSpeechHelper().showFloatingTextToSpeech(selectedBtn.getContext(), () -> {
//            new FloatingNewTextToSpeechHelper().showFloatingTextToSpeech(selectedBtn.getContext(), () -> {
                changeCloseBackground();
            });
        } else if (selectedBtn == audioFileBtn) {
            new FloatingNewAudioFileHelper().showFloatingAudioFile(activity, () -> {
//            new FloatingAudioFileHelper2().showFloatingAudioFile(activity, () -> {
                changeCloseBackground();
            });
        } else if (selectedBtn == lightBtn) {
            new FloatingNewLightHelper().showFloatingLight(selectedBtn.getContext(), () -> {
                changeCloseBackground();
            });
        } else if (selectedBtn == detectorAlarmBtn) {
            new FloatingNewDescentHelper2().showFloatingSettings(selectedBtn.getContext(), () -> {
                changeCloseBackground();
            });
        }
    }

    private void setSelectedImage(int id, ImageView currentSelectedBtn, boolean isSelected) {
        // 使用 Map 来存储 id 和对应的资源
        Map<Integer, int[]> imageMap = new HashMap<>();
        imageMap.put(R.id.audio_btn, new int[]{R.drawable.ic_shout_selected, R.drawable.ic_shout_unselected});
        imageMap.put(R.id.text_to_speech_btn, new int[]{R.drawable.ic_tts_selected, R.drawable.ic_tts_unselected});
        imageMap.put(R.id.audio_file_btn, new int[]{R.drawable.ic_audio_selected, R.drawable.ic_audio_unselected});
        imageMap.put(R.id.light_btn, new int[]{R.drawable.ic_light_selected, R.drawable.ic_light_unselected});
        imageMap.put(R.id.detector_alarm_btn, new int[]{R.drawable.ic_descent_selected, R.drawable.ic_descent_unselected});
        imageMap.put(R.id.tv_options_input_settings_btn, new int[]{R.drawable.ic_settings_selected, R.drawable.ic_settings_unselected});

        // 检查 Map 中是否有对应的资源
        if (imageMap.containsKey(id)) {
            int[] images = imageMap.get(id);
            currentSelectedBtn.setImageResource(isSelected ? images[0] : images[1]);
        }
    }

    private void changeCloseBackground() {
        if (currentSelectedBtn != null) {
            setSelectedImage(currentSelectedBtn.getId(), currentSelectedBtn, false);
//            currentSelectedBtn.setBackgroundResource(R.drawable.floating_bg_shape); // Deselect the button
            currentSelectedBtn = null;
        }
    }

    private void closeAllFloatingWindows() {
        // You should keep track of all tags or use a specific tag management strategy.
        EasyFloat.dismiss("audio_tag");
        EasyFloat.dismiss("text_to_speech_tag");
        EasyFloat.dismiss("audio_file_tag");
        EasyFloat.dismiss("light_tag");
        EasyFloat.dismiss("detector_alarm_tag");
        EasyFloat.dismiss("settings_tag");
    }

}
