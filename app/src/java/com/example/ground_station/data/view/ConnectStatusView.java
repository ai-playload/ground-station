package java.com.example.ground_station.data.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.R;

import java.com.example.ground_station.data.socket.ConnectionCallback;

public class ConnectStatusView extends FrameLayout implements ConnectionCallback {

    private View view;

    public ConnectStatusView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public ConnectStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public ConnectStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        view = new View(context);
        view.setBackgroundResource(R.drawable.shape_dot_red);
        int w = ConvertUtils.dp2px(9);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(w, w);
        layoutParams.gravity = Gravity.CENTER;
        addView(view, layoutParams);
        view.setOnClickListener(view1 -> {
            if (!StringUtils.isEmpty(errMsg)) {
                ToastUtils.showLong(errMsg);
            }
        });
    }

    String errMsg = "";
    @Override
    public void onConnectionSuccess() {
        errMsg = "";
        setStatus(true);
    }

    @Override
    public void onConnectionFailure(Exception e) {
        errMsg = e.getMessage();
        setStatus(false);
    }

    public void setStatus(boolean connected) {
        if (connected) {
            view.setBackgroundResource(R.drawable.shape_dot_green);
        }else {
            view.setBackgroundResource(R.drawable.shape_dot_red);
        }
    }
}
