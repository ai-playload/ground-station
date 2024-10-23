package java.com.example.ground_station.domain.usecase;


import java.com.example.ground_station.data.database.model.TtsKeyModel;
import java.com.example.ground_station.domain.repository.TtsKeyRepository;
import java.com.example.ground_station.domain.usecase.listener.OnOperationCompleteListener;

public class DeleteTtsKeyUseCase {
    private final TtsKeyRepository ttsKeyRepository;

    public DeleteTtsKeyUseCase(TtsKeyRepository ttsKeyRepository) {
        this.ttsKeyRepository = ttsKeyRepository;
    }

    public void execute(TtsKeyModel ttsKeyModel, OnOperationCompleteListener listener) {
        try {
            ttsKeyRepository.deleteTtsKey(ttsKeyModel);
            listener.onOperationComplete(true);
        } catch (Exception e) {
            e.printStackTrace();
            listener.onOperationComplete(false);
        }
    }
}
