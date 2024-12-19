package java.com.example.ground_station.presentation.fun.file;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FilePathUtils {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // Handle non-primary volumes (such as SD cards, USB storage, etc.)
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri)) {
                return uri.getLastPathSegment();
            }

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    // 获取数据列的方法
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } catch (Exception e) {
            return null;
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    // 判断是否为 ExternalStorageProvider
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    // 判断是否为 DownloadsProvider
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    // 判断是否为 MediaProvider
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    // 判断是否为 Google Photos
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static void getInput(Uri uri) {
    }

    public static InputStream getFilePathForN(Uri uri, Context context) {
        Uri returnUri = uri;
        Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
        /*
         * Get the column indexes of the data in the Cursor,
         *     * move to the first row in the Cursor, get the data,
         *     * and display it.
         * */
//        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
//        returnCursor.moveToFirst();
//        String name = (returnCursor.getString(nameIndex));
//        String size = (Long.toString(returnCursor.getLong(sizeIndex)));
//        File file = new File(context.getFilesDir(), name);
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            return inputStream;
//            FileOutputStream outputStream = new FileOutputStream(file);
//            int read = 0;
//            int maxBufferSize = 1 * 1024 * 1024;
//            int bytesAvailable = inputStream.available();
//
//            //int bufferSize = 1024;
//            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
//
//            final byte[] buffers = new byte[bufferSize];
//            while ((read = inputStream.read(buffers)) != -1) {
//                outputStream.write(buffers, 0, read);
//            }
//            Log.e("File Size", "Size " + file.length());
//            inputStream.close();
//            outputStream.close();
//            Log.e("File Path", "Path " + file.getPath());
//            Log.e("File Size", "Size " + file.length());
        } catch (Exception e) {
            Log.e("Exception", e.getMessage());
        }
//        return file.getPath();

        return null;
    }

    public static byte[] toByteArray(InputStream input)
            throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static int copy(InputStream input, OutputStream output)
            throws IOException {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    public static long copyLarge(InputStream input, OutputStream output)
            throws IOException {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        input.close();
        output.close();
        return count;
    }


    public static void installApk(File file) {


        Application context = Utils.getApp();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true); //表明不是未知来源
            Uri uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
            context.startActivity(intent);

        } else {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE, true); //表明不是未知来源
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public static String copyAssetGetFilePath(Context context, String fileName) {
        try {
            File cacheDir = context.getCacheDir();
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            File outFile = new File(cacheDir, fileName);
            if (!outFile.exists()) {
                boolean res = outFile.createNewFile();
                if (!res) {
                    return null;
                }
            } else {
                if (outFile.length() > 10) {//表示已经写入一次
                    return outFile.getPath();
                }
            }
            InputStream is = context.getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(outFile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return outFile.getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void openAssignFolder(String dir)//openAssignFolder("CameraPhotos");
    {
        dir = "kds%2fcrash_log";
        Uri uri =
                Uri.parse("content://com.android.externalstorage.documents/document/primary:" + dir);
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityUtils.getTopActivity().startActivity(intent);
//        ActivityUtils.getTopActivity().startActivityForResult(intent, 1);
    }

    public static void openAssignFolder2(String crashSdPath) {
        //调用系统文件管理器打开指定路径目录
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(Uri.fromFile(new File(crashSdPath).getParentFile()), "file/*.txt");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        ActivityUtils.getTopActivity().startActivity(intent);
//        startActivityForResult(intent, REQUEST_CHOOSEFILE)
    }
}
