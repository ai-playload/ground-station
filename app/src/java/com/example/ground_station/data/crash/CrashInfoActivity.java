package java.com.example.ground_station.data.crash;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.Nullable;


import com.example.ground_station.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class CrashInfoActivity extends ComponentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashinfo);
        String filePath = getIntent().getStringExtra("filePath");
        TextView tv = (TextView) findViewById(R.id.msgTv);
        tv.setText(getFileStr(filePath));
    }

    private String getFileStr(String filePath) {
        return readFileContent(filePath);
    }

    private String readFileContent(String filePath) {
        StringBuilder content = new StringBuilder();
        try {
            File file = new File(filePath);
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
                content.append("\r\n");
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}
