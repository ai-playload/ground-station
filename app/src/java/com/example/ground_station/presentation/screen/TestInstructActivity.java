package java.com.example.ground_station.presentation.screen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.ground_station.R;

import org.greenrobot.eventbus.EventBus;

import java.com.example.ground_station.data.model.SendFunctionProvider;
import java.util.ArrayList;
import java.util.List;

public class TestInstructActivity extends AppCompatActivity {
    EditText msgId2Ed;
    LinearLayout paramsParent;
    List<EditText> edVies = new ArrayList<>();
//    SendFunctionProvider sendFunctionProvider;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sky_data2);
        initView();
    }

    private void initView() {
        paramsParent = findViewById(R.id.paramsParent);
        msgId2Ed = ((EditText) findViewById(R.id.msgId2Ed));
        findViewById(R.id.addParams).setOnClickListener(view -> {
            addParamsItemLayout();
        });
        findViewById(R.id.btn_send).setOnClickListener(view -> {
            sendInstruct();
        });
        addParamsItemLayout();
    }

    private void addParamsItemLayout() {
        ViewGroup itemView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.item_params_layout, null, false);
        ((TextView) itemView.findViewById(R.id.nameTv)).setText("payload" + paramsParent.getChildCount() + ":");
        EditText ed = itemView.findViewById(R.id.payloadValue);
        edVies.add(ed);
        itemView.findViewById(R.id.delete_params).setOnClickListener(view -> {
            edVies.remove(ed);
            paramsParent.removeView(itemView);
        });
        paramsParent.addView(itemView);
    }

    private void sendInstruct() {
        String msgId2 = msgId2Ed.getText().toString();

        String[] ps = new String[edVies.size()];
        for (int i = 0; i < edVies.size(); i++) {
            String p = edVies.get(i).getText().toString();
            ps[i] = p;
        }

        EventBus.getDefault().post(new SendFunctionProvider(msgId2, ps));
    }


}
