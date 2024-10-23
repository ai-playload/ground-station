package java.com.example.ground_station.domain.repository;


import androidx.lifecycle.LiveData;

import java.com.example.ground_station.data.database.model.TtsKeyModel;
import java.util.List;
import java.util.concurrent.Future;

public interface TtsKeyRepository {
    LiveData<List<TtsKeyModel>> getTtsKeysByAppId(String date);

    Future<?> updateTtsKey(TtsKeyModel diaryModel);

    Future<?> deleteTtsKey(TtsKeyModel diaryModel);

    Future<?> insertTtsKey(TtsKeyModel diaryModel);
}
