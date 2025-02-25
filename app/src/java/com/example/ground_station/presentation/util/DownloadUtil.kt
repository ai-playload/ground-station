package java.com.example.ground_station.presentation.util

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.Utils
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.com.example.ground_station.presentation.floating.autdio.AudioFormatBean
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files

open class DownloadUtil {

    private var okHttpClient: OkHttpClient? = null

    /**
     * @param url          下载连接
     * @param destFileDir  下载的文件储存目录
     * @param destFileName 下载文件名称
     * @param listener     下载监听
     */
    fun download(
        info: AudioFormatBean,
        listener: OnDownloadListener
    ) {
        var destFileName: String = info.filename;
        var url = " http://81.70.35.199:5100/piper";
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient()
        }

        var destFileDir: String = Utils.getApp().cacheDir.absolutePath + "/tempAudio";
        var json = GsonUtils.toJson(info);
//        json =
//            "{\"text\":\"我们是中华中民共和国公民\",\"filename\":\"t31.mp3\",\"num\":\"1\",\"DB\":\"20\"}"

        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()

        val body: RequestBody = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .addHeader("Content-Type", "application/json")
            .build()
        okHttpClient!!.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 下载失败监听回调
                listener.onDownloadFailed(e)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                var code = response.code;
                if (code !== 200) {
                    var vs:String = "访问文字转语音接口失败，code: "+ code;
                    var e = RuntimeException(vs);
                    listener.onDownloadFailed(e)
                } else if (code === 200 && response.body != null) {
                    var inputStream: InputStream? = null
                    val buf = ByteArray(2048)
                    var len = 0
                    var fos: FileOutputStream? = null
                    // 储存下载文件的目录
                    val dir = File(destFileDir)
                    if (!dir.exists()) {
                        dir.mkdirs()
                    }
                    val file = File(dir, destFileName)
                    try {
                        inputStream = response.body!!.byteStream()
                        val total: Long = response.body!!.contentLength()
                        fos = FileOutputStream(file)
                        var sum: Long = 0
                        while (inputStream.read(buf).also { len = it } != -1) {
                            fos.write(buf, 0, len)
                            sum += len.toLong()
                            val progress = (sum * 1.0f / total * 100).toInt()
                            // 下载中更新进度条
                            listener.onDownloading(progress)
                        }
                        fos.flush()
                        // 下载完成
                        listener.onDownloadSuccess(file)
                    } catch (e: Exception) {
                        listener.onDownloadFailed(e)
                    } finally {
                        try {
                            inputStream?.close()
                        } catch (e: IOException) {
                            listener.onDownloadFailed(e)
                        }
                        try {
                            fos?.close()
                        } catch (e: IOException) {
                            listener.onDownloadFailed(e)
                        }
                    }
                } else {
                    listener.onDownloadFailed(IOException("接口失败"))
                }
            }
        })
    }

    interface OnDownloadListener {
        /**
         * @param file 下载成功后的文件
         */
        fun onDownloadSuccess(file: File?)

        /**
         * @param progress 下载进度
         */
        fun onDownloading(progress: Int)

        /**
         * @param e 下载异常信息
         */
        fun onDownloadFailed(e: Exception?)
    }


    /**
     * 将视频保存到系统相册
     */
    fun saveVideoToAlbum(context: Context, videoFile: String?): Boolean {
        if (videoFile == null || videoFile == "") {
            return false
        }
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            saveVideoToAlbumBeforeQ(context, videoFile)
        } else {
            saveVideoToAlbumAfterQ(context, videoFile)
        }
    }

    private fun saveVideoToAlbumAfterQ(context: Context, videoFile: String): Boolean {
        return try {
            val contentResolver = context.contentResolver
            val tempFile = File(videoFile)
            val contentValues = getVideoContentValues(context, tempFile, System.currentTimeMillis())
            val uri =
                contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)
            copyFileAfterQ(context, contentResolver, tempFile, uri)
            contentValues.clear()
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
            context.contentResolver.update(uri!!, contentValues, null, null)
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri))
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun saveVideoToAlbumBeforeQ(context: Context, videoFile: String): Boolean {
        val picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val tempFile = File(videoFile)
        val destFile = File(picDir, context.packageName + File.separator + tempFile.name)
        var ins: FileInputStream? = null
        var ous: BufferedOutputStream? = null
        return try {
            ins = FileInputStream(tempFile)
            ous = BufferedOutputStream(FileOutputStream(destFile))
            var nread = 0L
            val buf = ByteArray(1024)
            var n: Int
            while (ins.read(buf).also { n = it } > 0) {
                ous.write(buf, 0, n)
                nread += n.toLong()
            }
            MediaScannerConnection.scanFile(
                context, arrayOf(destFile.absolutePath), arrayOf("video/*")
            ) { path: String?, uri: Uri? -> }
            true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            false
        } finally {
            try {
                ins?.close()
                ous?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    @Throws(IOException::class)
    private fun copyFileAfterQ(
        context: Context,
        localContentResolver: ContentResolver,
        tempFile: File,
        localUri: Uri?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            context.applicationInfo.targetSdkVersion >= Build.VERSION_CODES.Q
        ) {
            //拷贝文件到相册的uri,android10及以上得这么干，否则不会显示。可以参考ScreenMediaRecorder的save方法
            val os = localContentResolver.openOutputStream(localUri!!)
            Files.copy(tempFile.toPath(), os)
            os!!.close()
            tempFile.delete()
        }
    }


    /**
     * 获取视频的contentValue
     */
    private fun getVideoContentValues(
        context: Context,
        paramFile: File,
        timestamp: Long
    ): ContentValues {
        val localContentValues = ContentValues()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            localContentValues.put(
                MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_DCIM
                        + File.separator + context.packageName
            )
        }
        localContentValues.put(MediaStore.Video.Media.TITLE, paramFile.name)
        localContentValues.put(MediaStore.Video.Media.DISPLAY_NAME, paramFile.name)
        localContentValues.put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        localContentValues.put(MediaStore.Video.Media.DATE_TAKEN, timestamp)
        localContentValues.put(MediaStore.Video.Media.DATE_MODIFIED, timestamp)
        localContentValues.put(MediaStore.Video.Media.DATE_ADDED, timestamp)
        localContentValues.put(MediaStore.Video.Media.SIZE, paramFile.length())
        return localContentValues
    }
}