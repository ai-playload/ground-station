package java.com.example.ground_station.presentation.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GsonParser {
    public List<String> parseAudioFileList(String jsonArrayString) {
        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(jsonArrayString, listType);
    }
}
