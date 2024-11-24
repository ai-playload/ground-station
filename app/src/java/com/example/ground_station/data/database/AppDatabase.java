package java.com.example.ground_station.data.database;


import android.content.Context;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import java.com.example.ground_station.data.database.model.TtsKeyModel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


@Database(entities = {TtsKeyModel.class}, version = 1)
public abstract
class AppDatabase extends RoomDatabase {

    public abstract TtsKeyDao ttsKeyDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "ttskey_database")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
