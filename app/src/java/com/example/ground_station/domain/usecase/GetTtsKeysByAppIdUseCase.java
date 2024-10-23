package java.com.example.ground_station.domain.usecase;

import androidx.lifecycle.LiveData;

import java.com.example.ground_station.data.database.model.TtsKeyModel;
import java.com.example.ground_station.domain.repository.TtsKeyRepository;
import java.util.List;


public class GetTtsKeysByAppIdUseCase {
    private final TtsKeyRepository ttsKeyRepository;

    public GetTtsKeysByAppIdUseCase(TtsKeyRepository ttsKeyRepository) {
        this.ttsKeyRepository = ttsKeyRepository;
    }

    public LiveData<List<TtsKeyModel>> execute(String appId) {
        return ttsKeyRepository.getTtsKeysByAppId(appId);
    }
}
