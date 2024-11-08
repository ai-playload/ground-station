package java.com.example.ground_station.presentation.fun.file;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemClickListener;
import com.example.ground_station.R;
import com.thegrizzlylabs.sardineandroid.DavResource;

import java.io.File;
import java.util.List;

public class FileListInfoView extends LinearLayout {

    private RecyclerView recyclerView;
    private SimpleAdapter adapter;
    private TextView curPath;
    private SardineHelper sardineHelper = new SardineHelper(getContext());
    private String rootPath;

    public FileListInfoView(Context context) {
        this(context, null);
    }

    public FileListInfoView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FileListInfoView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        setOrientation(LinearLayout.VERTICAL);
        LayoutInflater.from(context).inflate(R.layout.view_file_list_info_content, this);
        curPath = findViewById(R.id.tvCurPath);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        //1先登录； 再Parent Directory  2:展示列表  3. 下载  4.上传

        adapter = new SimpleAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        recyclerView.setAdapter(adapter);

        adapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(@NonNull BaseQuickAdapter<?, ?> adapter, @NonNull View view, int position) {
                List<FileInfo> data = (List<FileInfo>) adapter.getData();
                DavResource davResource = data.get(position).info;
                String path = davResource.getPath();
                requestFileList(path);
            }
        });

        adapter.setOnItemChildClickListener((adapter1, view, position) -> {
            List<FileInfo> data = (List<FileInfo>) adapter1.getData();
            FileInfo fileInfo = data.get(position - adapter1.getHeaderLayoutCount());
            if (fileInfo.hasLoadFile() && fileInfo.loadFile.getName().endsWith(".apk")) {
                AppUtils.installApp(fileInfo.loadFile);
            } else {
                downLoad(fileInfo, position);
            }
        });

        adapter.setHeaderViewClickListener(view -> {
            goBack();
        });
    }


    private void downLoad(FileInfo fileInfo, int position) {
        String path = fileInfo.info.getPath();
        sardineHelper.downLoad(path, new FileLoadCallBack() {
            @Override
            public void progress(float progress) {
                if (fileInfo.progress != progress) {
                    fileInfo.progress = progress;
                    adapter.notifyItemChanged(position, "progress");
                }
            }

            @Override
            public void getResult(File file) {
                ToastUtils.showLong("下载成功", Toast.LENGTH_LONG);
                fileInfo.loadFile = file;
                fileInfo.progress = 100;
                adapter.notifyItemChanged(position, "progress");
                if (FileHelper.checkApk(fileInfo)) {
                    AppUtils.installApp(file);
                }
            }
        });
    }

    private void goBack() {
        String path = sardineHelper.getParentPath(rootPath);
        requestFileList(path);
    }

    public void requestFileList(String path) {
        curPath.setText("当前文件路径：" + path);

        adapter.setGoParentVisiblity(!StringUtils.equals(this.rootPath, path));
        sardineHelper.list(path, new SardineCallBack<List<DavResource>>() {
            @Override
            public void getResult(List<DavResource> list) {
                adapter.setNewInstance(FileInfoUtils.map(list));
            }
        });
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
