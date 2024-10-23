package java.com.example.ground_station.data.repository;

import androidx.lifecycle.LiveData;

import java.com.example.ground_station.data.database.AppDatabase;
import java.com.example.ground_station.data.database.TtsKeyDao;
import java.com.example.ground_station.data.database.model.TtsKeyModel;
import java.com.example.ground_station.domain.repository.TtsKeyRepository;
import java.util.List;
import java.util.concurrent.Future;

public class TtsKeyRepositoryImpl implements TtsKeyRepository {
    private final TtsKeyDao ttsKeyDao;

    public TtsKeyRepositoryImpl(TtsKeyDao ttsKeyDao) {
        this.ttsKeyDao = ttsKeyDao;
    }

    @Override
    public LiveData<List<TtsKeyModel>> getTtsKeysByAppId(String appId) {
        return ttsKeyDao.getTtsKeysByAppId(appId);
    }

    @Override
    public Future<?> updateTtsKey(TtsKeyModel diaryModel) {
        return AppDatabase.databaseWriteExecutor.submit(() -> ttsKeyDao.updateTtsKey(diaryModel));
    }

    @Override
    public Future<?> deleteTtsKey(TtsKeyModel diaryModel) {
        return AppDatabase.databaseWriteExecutor.submit(() -> ttsKeyDao.deleteTtsKey(diaryModel));
    }

    @Override
    public Future<?> insertTtsKey(TtsKeyModel diaryModel) {
        return AppDatabase.databaseWriteExecutor.submit(() -> ttsKeyDao.insertTtsKey(diaryModel));
    }
}
