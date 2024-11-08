package java.com.example.ground_station.presentation.fun.file;

import com.thegrizzlylabs.sardineandroid.DavResource;

import java.io.File;

public class FileInfo {

    public DavResource info;

    public float progress;

    public File loadFile;

    public FileInfo(DavResource davResource) {
        this.info = davResource;
    }

    public boolean hasLoadFile() {
        return loadFile != null && loadFile.exists() && loadFile.isFile() && loadFile.length() > 0;
    }
}
