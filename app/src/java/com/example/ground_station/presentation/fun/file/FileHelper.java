package java.com.example.ground_station.presentation.fun.file;

import android.util.Log;

import com.blankj.utilcode.util.ThreadUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class FileHelper {

    public static WriteFileBuilder createWriteBuilder() {
        return new WriteFileBuilder();
    }

    public static boolean checkApk(FileInfo loadFile) {
        File file = loadFile.loadFile;
        return file != null && file.exists() && file.getName().endsWith(".apk") && file.length() > 0;
    }

    public static class WriteFileBuilder {
        private FileLoadCallBack callBack;
        private InputStream inputStream;
        private File outFile;
        private long fileSize;
        private boolean cover;

        public WriteFileBuilder setInputStream(InputStream inputStream) {
            this.inputStream = inputStream;
            return this;
        }

        public WriteFileBuilder setFileSize(long fileSize) {
            this.fileSize = fileSize;
            return this;
        }

        public WriteFileBuilder setOutFile(File outFile) {
            this.outFile = outFile;
            return this;
        }

        public WriteFileBuilder setCover(boolean cover) {
            this.cover = cover;
            return this;
        }

        public WriteFileBuilder setCallBack(FileLoadCallBack callBack) {
            this.callBack = callBack;
            return this;
        }

        public void build() {
            writeFile(inputStream, outFile);
        }

        private boolean writeFile(InputStream is, File outFile) {
            FileOutputStream fos = null;
            try {
                if (outFile.exists()) {
                    outFile.delete();
                } else if (!outFile.getParentFile().exists()) {
                    outFile.getParentFile().mkdirs();
                }

                fos = new FileOutputStream(outFile);
                byte[] b = new byte[1024];
                long loadSize = 0;
                float progress = 0;
                int len = 0;
                while ((len = is.read(b)) != -1) {
                    fos.write(b, 0, len);// 写入数据
                    loadSize += len;
                    float rate = (loadSize * 100f / fileSize);

                    BigDecimal bigDecimal = new BigDecimal(rate).setScale(1, RoundingMode.HALF_UP);
                    float v = bigDecimal.floatValue();
                    if (v != progress) {
                        progress = v;

                        float finalProgress = progress;
                        ThreadUtils.getMainHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Log.d("FileHlper", "进度：" + finalProgress);
                                callBack.progress(finalProgress);
                            }
                        });
                    }
                }
                return true;
            } catch (Exception e) {
            } finally {
                try {
                    is.close();
                    fos.close();// 保存数据

                    ThreadUtils.getMainHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            callBack.getResult(outFile);
                        }
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return false;
        }
    }
}
