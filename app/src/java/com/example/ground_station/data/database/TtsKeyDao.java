package java.com.example.ground_station.data.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.com.example.ground_station.data.database.model.TtsKeyModel;
import java.util.List;

@Dao
public interface TtsKeyDao {

    @Query("SELECT * FROM TTS WHERE appId = :appId")
    LiveData<List<TtsKeyModel>> getTtsKeysByAppId(String appId);

    @Update
    void updateTtsKey(TtsKeyModel ttsKeyModel);

    @Delete
    void deleteTtsKey(TtsKeyModel ttsKeyModel);

    @Insert
    void insertTtsKey(TtsKeyModel ttsKeyModel);
}
