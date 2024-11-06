package java.com.example.ground_station.presentation.floating.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ground_station.R;
import com.lzf.easyfloat.EasyFloat;
import com.lzf.easyfloat.enums.ShowPattern;
import com.lzf.easyfloat.enums.SidePattern;
import com.lzf.easyfloat.interfaces.OnFloatCallbacks;

public class FloatingDeleteDialog {
    private final String tag = "audio_delete_tag";

    public void showDeleteDialog(Context context, DeleteDialogCallback deleteDialogCallback) {
        EasyFloat.with(context)
                .setShowPattern(ShowPattern.ALL_TIME)
                .setSidePattern(SidePattern.DEFAULT)
                .setGravity(Gravity.CENTER, 0, 0)
                .setDragEnable(false)
                .setTag(tag)
                .setLayout(R.layout.floating_delete_dialog, view -> {

                    view.findViewById(R.id.cancel_button).setOnClickListener(v -> {
                        deleteDialogCallback.onCancel();
                        EasyFloat.dismiss(tag);
                    });

                    view.findViewById(R.id.default_button).setOnClickListener(v -> {
                        deleteDialogCallback.onDelete();
                        EasyFloat.dismiss(tag);
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

                    }

                    @Override
                    public void touchEvent(@NonNull View view, @NonNull MotionEvent motionEvent) {
                    }

                    @Override
                    public void createdResult(boolean b, @Nullable String s, @Nullable View view) {

                    }
                })
                .show();

    }


    public interface DeleteDialogCallback {
        void onDelete();
        void onCancel();
    }

}
