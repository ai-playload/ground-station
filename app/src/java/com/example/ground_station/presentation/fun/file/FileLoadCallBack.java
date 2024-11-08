package java.com.example.ground_station.presentation.fun.file;

import java.io.File;

public interface FileLoadCallBack extends SardineCallBack<File> {

    void progress(float progress);
}
