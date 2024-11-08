package java.com.example.ground_station.presentation.fun.file;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.example.ground_station.R;
import com.thegrizzlylabs.sardineandroid.DavResource;

import java.io.File;
import java.util.List;

public class SimpleAdapter extends BaseQuickAdapter<FileInfo, SimpleAdapter.FileViewHolder> {

    private final View headerView;

    public SimpleAdapter() {
        super(R.layout.listitem_file);
        headerView = LayoutInflater.from(Utils.getApp()).inflate(R.layout.listitem_file, null);
        ((TextView) headerView.findViewById(R.id.tv0)).setText("返回上一层");
        addHeaderView(headerView);
    }

    @Override
    protected void convert(FileViewHolder holder, FileInfo item) {
        DavResource info = item.info;
        holder.tv0.setText("文件名：" + info.getName());

        boolean isFile = checkFile(info);
        holder.downLoadBtn.setVisibility(isFile ? View.VISIBLE : View.GONE);

        boolean hasApkFile = FileHelper.checkApk(item);
        holder.downLoadBtn.setText(hasApkFile ? "安装" : "下载");
        holder.downLoadBtn.setTextColor(Color.parseColor(hasApkFile ? "#ffaa99" : "#999999"));
        holder.resetDownLoadBtn.setVisibility(hasApkFile ? View.VISIBLE : View.GONE);
        holder.resetDownLoadBtn.setOnClickListener(view -> {
            File file = item.loadFile;
            if (file != null && file.exists()) {
                file.delete();
            }
            item.loadFile = null;
            holder.resetDownLoadBtn.setVisibility(View.GONE);
            holder.downLoadBtn.setTextColor(Color.parseColor("#999999"));
            getOnItemChildClickListener().onItemChildClick(this, holder.downLoadBtn, holder.getLayoutPosition());
        });
        holder.downLoadBtn.setOnClickListener(view -> {
            getOnItemChildClickListener().onItemChildClick(this, holder.downLoadBtn, holder.getLayoutPosition());
        });
    }

    @Override
    protected void convert(@NonNull FileViewHolder holder, FileInfo item, @NonNull List<?> payloads) {
        super.convert(holder, item, payloads);
        if (payloads != null && payloads.size() > 0) {
            if (item.progress > 0) {
                holder.downLoadBtn.setText(String.valueOf(item.progress) + "%");
                if (item.progress >= 100) {
                    holder.resetDownLoadBtn.setVisibility(View.VISIBLE);
                    boolean hasApkFile = FileHelper.checkApk(item);
                    if (hasApkFile) {
                        holder.downLoadBtn.setText("安装");
                        holder.downLoadBtn.setTextColor(Color.parseColor("#ffaa99"));
                    }
                }
            }
        }
    }

    private boolean checkFile(DavResource item) {
        return item.getName().contains(".");
    }

    public void setHeaderViewClickListener(View.OnClickListener listener) {
        headerView.setOnClickListener(listener);
    }

    public void setGoParentVisiblity(boolean b) {
        headerView.setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public static class FileViewHolder extends BaseViewHolder {

        private final TextView tv0;
        private final Button downLoadBtn;
        private final Button resetDownLoadBtn;

        public FileViewHolder(View view) {
            super(view);
            resetDownLoadBtn = view.findViewById(R.id.resetDownLoadBtn);
            downLoadBtn = view.findViewById(R.id.downLoadBtn);
            tv0 = view.findViewById(R.id.tv0);
        }
    }
}
