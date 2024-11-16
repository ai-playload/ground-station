package java.com.example.ground_station.data.crash;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ActivityUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.ground_station.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CrashInfoListActivity extends ComponentActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crashinfo_list);

        RecyclerView rv = findViewById(R.id.recyclerview);
        rv.setLayoutManager(new LinearLayoutManager(this));
        CrashInfoAdapter adapter = new CrashInfoAdapter();
        rv.setAdapter(adapter);
        List<File> data = getData(this);
        adapter.setNewInstance(data);

//        ((TextView) findViewById(R.id.fileParentPathTv)).setText(CrashHandler.getInstance().getCrashSdPath());
//        findViewById(R.id.openBtn).setOnClickListener(view -> {
//            FilePathUtils.openAssignFolder(data.get(0).getAbsolutePath());
//        });
    }

    private List<File> getData(Context context) {
        String crashCachePath = CrashHandler.getInstance().getCrashCachePath();
        File file = new File(crashCachePath);
        File[] files = file.listFiles();
        try {
            return Arrays.asList(files);
        } catch (Exception e) {
        }
        return new ArrayList<>();
    }

    static class CrashInfoAdapter extends BaseQuickAdapter<File, CrashInfoAdapter.Holder> {

        public CrashInfoAdapter() {
            super(R.layout.listitem_text);
        }

        @Override
        protected void convert(@NonNull Holder holder, File file) {
            holder.tv.setText(file.getName());
            holder.itemView.setOnClickListener(view -> {
                Activity activity = ActivityUtils.getTopActivity();
                Intent intent = new Intent(activity, CrashInfoActivity.class);
                intent.putExtra("filePath", file.getAbsolutePath());
                activity.startActivity(intent);
            });
        }

        static class Holder extends BaseViewHolder {

            private final TextView tv;

            public Holder(@NonNull View view) {
                super(view);
                tv = view.findViewById(R.id.tv);
            }
        }
    }
}
