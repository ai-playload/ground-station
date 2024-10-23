package java.com.example.ground_station.presentation.floating.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.com.example.ground_station.presentation.floating.fragment.LocalAudioFragment;
import java.com.example.ground_station.presentation.floating.fragment.RemoteAudioFragment;

public class AudioPagerAdapter extends FragmentStateAdapter {

    public AudioPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new RemoteAudioFragment();
            case 1:
                return new RemoteAudioFragment();
            default:
                return new RemoteAudioFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 2; // 本地音频和远程音频
    }
}
