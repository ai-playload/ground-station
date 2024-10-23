package java.com.example.ground_station.data.database.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "TTS")
public class TtsKeyModel implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String appId;
    public String apiSecret;
    public String apiKey;
}
