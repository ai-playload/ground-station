package java.com.example.ground_station.domain.usecase;

import java.com.example.ground_station.data.database.model.TtsKeyModel;
import java.com.example.ground_station.domain.repository.TtsKeyRepository;
import java.com.example.ground_station.domain.usecase.listener.OnOperationCompleteListener;

public class UpdateDiaryUseCase {
    private final TtsKeyRepository ttsKeyRepository;

    public UpdateDiaryUseCase(TtsKeyRepository ttsKeyRepository) {
        this.ttsKeyRepository = ttsKeyRepository;
    }

    public void execute(TtsKeyModel ttsKeyModel, OnOperationCompleteListener listener) {
        try {
            ttsKeyRepository.updateTtsKey(ttsKeyModel);
            listener.onOperationComplete(true);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onOperationComplete(false);
        }
    }
}
