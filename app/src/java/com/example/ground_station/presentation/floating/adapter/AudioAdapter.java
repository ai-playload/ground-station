package java.com.example.ground_station.presentation.floating.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ToastUtils;
import com.example.ground_station.R;

import java.com.example.ground_station.data.model.AudioModel;
import java.com.example.ground_station.data.model.CommonConstants;
import java.com.example.ground_station.presentation.listener.ConnectStateListener;

public class AudioAdapter extends ListAdapter<AudioModel, AudioAdapter.AudioViewHolder> {

    private OnItemClickListener onItemClickListener;
    public int currentPlayingPosition = RecyclerView.NO_POSITION;
    private int currentPosition = -1;
    protected ConnectStateListener connectStateListener;
    private int type;

    public void setParentViewType(int type) {
        this.type = type;
    }

    public interface OnItemClickListener {
        void onItemClick(AudioModel audioModel, boolean isPlaying, boolean isPlayingPosition, int position);
    }

    private OnItemDeleteListener onItemDeleteListener;

    public AudioAdapter(ConnectStateListener listener, int parentType) {
        super(DIFF_CALLBACK);
        setConnectStateListener(listener);
        setParentViewType(parentType);
    }

    public AudioAdapter(OnItemClickListener onItemClickListener) {
        super(DIFF_CALLBACK);
        this.onItemClickListener = onItemClickListener;
    }

    public void setConnectStateListener(ConnectStateListener connectStateListener) {
        this.connectStateListener = connectStateListener;
    }

    public void setOnItemDeleteListener(OnItemDeleteListener onItemDeleteListener) {
        this.onItemDeleteListener = onItemDeleteListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioModel audioModel = getItem(position);
        holder.bind(audioModel);
        holder.itemView.isFocusableInTouchMode();
    }

    public void updatePlayingPosition(int newPlayingPosition) {
        if (currentPlayingPosition != RecyclerView.NO_POSITION) {
            getItem(currentPlayingPosition).setPlaying(false);
            notifyItemChanged(currentPlayingPosition);
        }
        currentPlayingPosition = newPlayingPosition;
        getItem(newPlayingPosition).setPlaying(true);
        notifyItemChanged(newPlayingPosition);
    }

    public void togglePlayPause(int position) {
        AudioModel audioModel = getItem(position);
        audioModel.setPlaying(!audioModel.isPlaying());
        notifyItemChanged(position);
    }

    public void playNextAudio() {
        if (currentPosition <= getItemCount() - 1) {
            currentPosition++;

            ItemClickHandle(currentPosition);
        }
    }

    public void playLoopAudio() {
        ItemClickHandle(currentPosition);
    }

    private void ItemClickHandle(int position) {
        updatePlayingPosition(position);

        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(getItem(position), false, false, currentPosition);
        }
    }

    public void other(int position) {
        updatePlayingPosition(position);
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(getItem(position), false, false, position);
        }
    }

    public void pause() {
        if (currentPlayingPosition >= 0 && currentPlayingPosition < getCurrentList().size()) {
            AudioModel audioModel = getItem(currentPlayingPosition);
            if (audioModel.isPlaying()) {
                audioModel.setPlaying(false);
                notifyDataSetChanged();
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(audioModel, false, true, currentPlayingPosition);
                }
            }
        }
    }

    public void stopPlay(int fileIndex) {
        AudioModel audioModel = getItem(fileIndex);
        audioModel.setPlaying(false);
        currentPlayingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        private final TextView audioContent;
        private final ImageView playBtn;
        private final ImageView deleteBtn;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            audioContent = itemView.findViewById(R.id.audio_content);
            playBtn = itemView.findViewById(R.id.play_btn);
            deleteBtn = itemView.findViewById(R.id.delete_btn);

            itemView.setOnClickListener(view -> {
                if (connectStateListener != null && !connectStateListener.isConnectedSocket()) {
                    ToastUtils.showShort("未成功连接");
                    return;
                }
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    if (currentPlayingPosition == position) {
                        togglePlayPause(position);

                        // Toggle play/pause
                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(getItem(position), getItem(position).isPlaying(), true, position);
                        }
                    } else {
                        updatePlayingPosition(position);

                        if (onItemClickListener != null) {
                            onItemClickListener.onItemClick(getItem(position), false, false, position);
                        }
                    }
                }
            });

            deleteBtn.setOnClickListener(view -> {
                if (onItemDeleteListener != null) {
                    int position = getAdapterPosition();
                    onItemDeleteListener.onItemDelete(getItem(position), position);
                }
            });
        }

        public void bind(AudioModel audioModel) {
            audioContent.setText(audioModel.getAudioFileName());
            // Update play button state if necessary
            playBtn.setImageResource(audioModel.isPlaying() ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
            playBtn.setVisibility(audioModel.selected && type == CommonConstants.TYPE_BP ? View.GONE : View.VISIBLE);
            deleteBtn.setVisibility(audioModel.selected ? View.GONE : View.VISIBLE);
        }
    }

    private static final DiffUtil.ItemCallback<AudioModel> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AudioModel>() {
                @Override
                public boolean areItemsTheSame(@NonNull AudioModel oldItem, @NonNull AudioModel newItem) {
                    return oldItem.getAudioFilePath().equals(newItem.getAudioFilePath());
                }

                @SuppressLint("DiffUtilEquals")
                @Override
                public boolean areContentsTheSame(@NonNull AudioModel oldItem, @NonNull AudioModel newItem) {
                    return oldItem.equals(newItem);
                }
            };


    public interface OnItemDeleteListener {
        void onItemDelete(AudioModel audioModel, int position);
    }

}

