package java.com.example.ground_station.presentation.floating.autdio;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ground_station.R;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.presentation.floating.adapter.AudioSelectedAdapter;
import java.util.ArrayList;
import java.util.List;

public class AudioSelectedListView extends LinearLayout {

    private AudioSelectedAdapter adapter;
    private AudioDataProvider provider;

    public AudioSelectedListView(Context context) {
        this(context, null);
    }

    public AudioSelectedListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioSelectedListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.floating_audio_file_select, this);
        View view = this;
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        initRecyclerView(recyclerView);
        view.findViewById(R.id.confirm).setOnClickListener(view1 -> {
            confirmClickEvent();
        });
    }

    public void setSelectedDataCallback(AudioDataProvider provider) {
        this.provider = provider;
    }

    private void confirmClickEvent() {
        this.setVisibility(View.GONE);
        if (provider != null) {
            List list = new ArrayList<AudioModel>();
            for (AudioModel model : adapter.getCurrentList()) {
                if (model.selected) {
                    list.add(model);
                }
            }
            provider.provider(list);
        }
    }

    public void initRecyclerView(RecyclerView recyclerView) {
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        adapter = new AudioSelectedAdapter(new AudioSelectedAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(AudioModel audioModel, boolean isPlaying, boolean isPlayingPosition) {

            }
        });
        recyclerView.setAdapter(adapter);
    }

    public void submitList(List<AudioModel> list) {
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }

    public AudioSelectedAdapter getAdapter() {
        return adapter;
    }
}
