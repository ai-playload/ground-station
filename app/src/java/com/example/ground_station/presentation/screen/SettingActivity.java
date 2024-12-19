package java.com.example.ground_station.presentation.screen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;

import com.example.ground_station.R;


public class SettingActivity extends ComponentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ((View) findViewById(R.id.softUpdateBtn)).setOnClickListener(view -> {
                startActivity(new Intent(SettingActivity.this, SoftUpdateActivity.class));
        });


    }
}
