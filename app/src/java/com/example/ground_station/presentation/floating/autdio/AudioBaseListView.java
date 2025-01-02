package java.com.example.ground_station.presentation.floating.autdio;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ground_station.R;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.CommonConstants;
import java.com.example.ground_station.data.service.GroundStationService;
import java.com.example.ground_station.data.socket.SocketConstant;
import java.com.example.ground_station.presentation.floating.BaseFloatingHelper;
import java.com.example.ground_station.presentation.floating.adapter.AudioAdapter;
import java.com.example.ground_station.presentation.listener.ConnectStateListener;
import java.util.ArrayList;
import java.util.List;

public class AudioBaseListView extends LinearLayout implements ConnectStateListener {

    protected AudioAdapter adapter;
    BaseFloatingHelper helper;
    GroundStationService groundStationService;
    protected int bfFlag = PlayerStates.normal;
    protected List<AudioModel> list = new ArrayList<>();
    protected int type = CommonConstants.TYPE_LOAD;
    protected AudioSelectedListView selectedListView;
    protected RadioGroup btnParent;

    public AudioBaseListView(Context context) {
        this(context, null);
    }

    public AudioBaseListView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioBaseListView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void resetData() {
        for (AudioModel audioModel : list) {
            audioModel.selected = false;
            audioModel.setPlaying(false);
        }
    }

    protected void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.floating_audio_file_list, this);
        View view = this;
        RecyclerView recyclerView = view.findViewById(R.id.recyclerview);
        selectedListView = view.findViewById(R.id.selectedParent);
        selectedListView.setVisibility(View.GONE);

        initRecyclerView(recyclerView);
        btnParent = (RadioGroup) view.findViewById(R.id.nt1);
        btnParent.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                int tag = Integer.parseInt(((String) radioGroup.findViewById(i).getTag().toString()));
                bfFlag = tag;
                resetData();
                if (PlayerStates.xh == tag) {
                    selectedListView.setVisibility(VISIBLE);
                    selectedListView.submitList(list);
                } else {
                    selectedListView.setVisibility(GONE);
                    //先暂停
                    int p = adapter.currentPlayingPosition;
                    p = Math.max(p, 0);
                    if (type == CommonConstants.TYPE_LOAD) {
                        send(SocketConstant.STREAMER, SocketConstant.PM.PLAY_BUNCH_STOP);
                    } else {
                        send(SocketConstant.PLAY_REMOTE_AUDIO_BY_NAME, p, SocketConstant.PM.PLAY_BUNCH_STOP);
                    }

                    if (PlayerStates.normal == tag) {

                    } else if (PlayerStates.pause == tag) {

                    }
                    adapter.submitList(list);
                    adapter.notifyDataSetChanged();
                }
            }
        });
    }

    public void initRecyclerView(RecyclerView recyclerView) {
        adapter = new AudioAdapter(this, type);
        recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
        recyclerView.setAdapter(adapter);
    }

    public boolean isConnectedSocket() {
//        return groundStationService != null && groundStationService.isConnectedSocket();
        return  false;
    }

    protected void send(byte msgId2, int... payload) {
        groundStationService.sendInstruct(msgId2, payload);
    }

    public void setObj(BaseFloatingHelper helper) {
        this.helper = helper;
    }

    public void setObj(GroundStationService groundStationService) {
        this.groundStationService = groundStationService;
    }

    public void submitList(List<AudioModel> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        this.list = list;
        adapter.submitList(list);
        adapter.notifyDataSetChanged();
    }

    public AudioAdapter getAdapter() {
        return adapter;
    }

    public void setType(int type) {
        this.type = type;
    }
}
