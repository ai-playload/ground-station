package java.com.example.ground_station.presentation.util;

import android.content.Context;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AssetCopierUtil {

    public interface CopyCallback {
        void onCopySuccess();
        void onCopyFailure(Exception e);
    }

    public static void copyAssetsToSDCard(Context context, String assetDir, String outDir, CopyCallback callback) {
        File outFileDir = new File(outDir);

        // 如果目标文件夹存在并且包含aisound和xtts文件夹，则不进行复制操作
        if (outFileDir.exists()) {
            File aisoundDir = new File(outDir + "/aisound");
            File xttsDir = new File(outDir + "/xtts");
            if (aisoundDir.exists() && xttsDir.exists()) {
                callback.onCopySuccess();
                return;
            }
        }

        String[] files;
        try {
            files = context.getAssets().list(assetDir);
            if (files == null || files.length == 0) return;

            if (!outFileDir.exists()) {
                outFileDir.mkdirs();
            }

            CopyTask copyTask = new CopyTask(context, assetDir, outDir, files.length, callback);
            for (String fileName : files) {
                String assetFilePath = assetDir + "/" + fileName;
                String outFilePath = outDir + "/" + fileName;

                if (context.getAssets().list(assetFilePath).length == 0) {
                    copyTask.copyFile(assetFilePath, outFilePath);
                } else {
                    copyAssetsToSDCard(context, assetFilePath, outFilePath, copyTask);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            callback.onCopyFailure(e);
        }
    }
    public static boolean shouldSkipCopy(String outDir) {
        File outFileDir = new File(outDir);
        if (outFileDir.exists()) {
            File aisoundDir = new File(outDir + "/aisound");
            File xttsDir = new File(outDir + "/xtts");
            return aisoundDir.exists() && xttsDir.exists();
        }
        return false;
    }

    private static void copyFile(Context context, String assetFilePath, String outFilePath) {
        try {
            InputStream in = context.getAssets().open(assetFilePath);
            OutputStream out = new FileOutputStream(outFilePath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            in.close();
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class CopyTask implements CopyCallback {
        private final Context context;
        private final String assetDir;
        private final String outDir;
        private int fileCount;
        private final CopyCallback callback;

        public CopyTask(Context context, String assetDir, String outDir, int fileCount, CopyCallback callback) {
            this.context = context;
            this.assetDir = assetDir;
            this.outDir = outDir;
            this.fileCount = fileCount;
            this.callback = callback;
        }

        public void copyFile(String assetFilePath, String outFilePath) throws IOException {
            try (InputStream in = context.getAssets().open(assetFilePath);
                 OutputStream out = new FileOutputStream(outFilePath)) {

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                out.flush();
                onCopySuccess();
            } catch (IOException e) {
                onCopyFailure(e);
            }
        }

        @Override
        public void onCopySuccess() {
            fileCount--;
            if (fileCount == 0) {
                callback.onCopySuccess();
            }
        }

        @Override
        public void onCopyFailure(Exception e) {
            callback.onCopyFailure(e);
        }
    }
}
