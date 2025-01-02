package java.com.example.ground_station.presentation.floating.adapter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_audio, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioModel audioModel = getItem(position);
        holder.bind(audioModel, position == currentPlayingPosition);
        holder.itemView.isFocusableInTouchMode();
    }

    public void updatePlayingPosition(int newPlayingPosition) {
        if (getItemCount() == 0) {
            return;
        }

        if (currentPlayingPosition >= getItemCount()) {
            currentPlayingPosition = getItemCount() - 1;
        }
        if (currentPlayingPosition != RecyclerView.NO_POSITION) {
            getItem(currentPlayingPosition).setPlaying(false);
            notifyItemChanged(currentPlayingPosition);
        }
        currentPlayingPosition = newPlayingPosition;
        getItem(newPlayingPosition).setPlaying(true);
        notifyItemChanged(newPlayingPosition);
    }

    public int getCurrentPlayingPosition() {
        return currentPlayingPosition;
    }

    public void togglePlayPause(int position) {
        AudioModel audioModel = getItem(position);
        audioModel.setPlaying(!audioModel.isPlaying());
        notifyItemChanged(position);
    }

    public void playNextAudio() {
        if (getItemCount() == 0) {
            return;
        }

        if (currentPosition >= getItemCount() - 1) {
            // 到了最后一个时重置到第一个位置
            currentPosition = 0;
        } else {
            // 否则播放下一个
            currentPosition++;
        }
        ItemClickHandle(currentPosition);
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

    public AudioModel getCurrentItem(int position) {
        return getItem(position);
    }

    class AudioViewHolder extends RecyclerView.ViewHolder {
        private final TextView audioContent;
        private final ImageView playBtn;
        private final View playImg;
//        private final ImageView deleteBtn;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            audioContent = itemView.findViewById(R.id.audio_content);
            playBtn = itemView.findViewById(R.id.play_btn);
            playImg = itemView.findViewById(R.id.audio_play_img);

            itemView.setOnClickListener(view -> {
//                if (connectStateListener != null && !connectStateListener.isConnectedSocket()) {
//                    ToastUtils.showShort("未成功连接");
//                    return;
//                }
                int position = getAdapterPosition();
                currentPosition = position;

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
        }

        public void bind(AudioModel audioModel, boolean isSelected) {
            audioContent.setText(audioModel.getShowName());
            playBtn.setImageResource(audioModel.isPlaying() ? R.drawable.ic_item_pause : R.drawable.ic_item_play);
            itemView.setSelected(isSelected);
            ViewCompat.setBackgroundTintList(playImg, ColorStateList.valueOf(isSelected ? Color.parseColor("#F99E51") : Color.parseColor("#FFFFFF")));
            audioContent.setTextColor(isSelected ? 0xFFF99E51 : 0xFFE2E6F2);
            // Update play button state if necessary
//            playBtn.setImageResource(audioModel.isPlaying() ? R.drawable.ic_pause_circle : R.drawable.ic_play_circle);
//            playBtn.setVisibility(audioModel.selected && type == CommonConstants.TYPE_BP ? View.GONE : View.VISIBLE);
//            deleteBtn.setVisibility(audioModel.selected ? View.GONE : View.VISIBLE);
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

