package java.com.example.ground_station.presentation.floating.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ground_station.R;

import java.com.example.ground_station.data.model.AudioModel;


public class AudioSelectedAdapter extends ListAdapter<AudioModel, AudioSelectedAdapter.AudioViewHolder> {

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(AudioModel audioModel, boolean isPlaying, boolean isPlayingPosition);
    }

    public AudioSelectedAdapter(OnItemClickListener onItemClickListener) {
        super(DIFF_CALLBACK);
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_selected, parent, false);
        return new AudioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder holder, int position) {
        AudioModel audioModel = getItem(position);
        holder.bind(audioModel);
        holder.itemView.isFocusableInTouchMode();
    }


    class AudioViewHolder extends RecyclerView.ViewHolder {
        private final TextView audioContent;
        private final CheckBox playBtn;

        public AudioViewHolder(@NonNull View itemView) {
            super(itemView);
            audioContent = itemView.findViewById(R.id.audio_content);
            playBtn = itemView.findViewById(R.id.check_box);


            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    AudioModel info = getItem(position);
                    info.selected = true;
                    if (onItemClickListener != null) {
                        onItemClickListener.onItemClick(info, getItem(position).isPlaying(), true);
                    }
                }
            });
        }

        public void bind(AudioModel audioModel) {
            audioContent.setText(audioModel.getAudioFileName());
            boolean isSelected = audioModel.selected;
            playBtn.setOnCheckedChangeListener(null);
            playBtn.setChecked(isSelected);
            playBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    audioModel.selected = b;
                }
            });
        }
    }


    public void runItemClick(int position, boolean start) {
        AudioModel audioModel = getItem(position);
        audioModel.setPlaying(start);
        notifyItemChanged(position);
        // Toggle play/pause
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(getItem(position), start, true);
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
}

