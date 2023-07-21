package org.linkmessenger.posts.repository


import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.arthenica.ffmpegkit.ReturnCode
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.microsoft.appcenter.analytics.Analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.getWatermarkIcon
import org.linkmessenger.request.models.AddLogParams
import org.linkmessenger.utils.HLSDownloader
import org.linkmessenger.utils.ImagesDownloader
import org.linkmessenger.utils.ResourcesUtil
import org.signal.core.util.ResourceUtil.getResources
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.util.MediaUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
import java.util.UUID


class VideoRepositoryImpl(val context: Context, private val postRepository: PostRepository):VideoRepository {
    override fun compressVideo(media: Media, newFilePath: String, orientation:Int) {
        val inputPath = FFmpegKitConfig.getSafParameterForRead(context, media.uri)

        val session = if(FirebaseRemoteConfig.getInstance().getBoolean("new_video_type")){
            FFmpegKit.execute("-i \"$inputPath\" -ss 00:00:00 -t 00:03:00 -vf scale=\"480:trunc(ow/a/2)*2\" -c:v libx264 -crf 26 -preset fast -tune film -y \"$newFilePath\"")
        }else{
            FFmpegKit.execute("-i \"$inputPath\" -ss 00:00:00 -t 00:03:00 -async 1  -vf \"scale='if(gt(iw,ih),480,trunc(oh*a/2)*2)':'if(gt(iw,ih),trunc(ow/a/2)*2,480)'\" -c:v h264 -crf 25 -y \"$newFilePath\"")
        }

        if (ReturnCode.isSuccess(session.returnCode)) {
//            postRepository.addLog(AddLogParams("compressVideo", "success"))
            // SUCCESS
        } else if (ReturnCode.isCancel(session.returnCode)) {
            postRepository.addLog(AddLogParams("compressVideo", "canceled"))
            // CANCEL
        } else {
            postRepository.addLog(AddLogParams("compressVideo", "error:${session.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "compressVideo"
            properties["message"] = session.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session.logsAsString)
        }
    }

    override fun toHls(pathToVideo: String, newFilePath: String) {

        val session = if(FirebaseRemoteConfig.getInstance().getBoolean("min_hls")){
            FFmpegKit.execute("-i \"$pathToVideo\" -codec: copy -start_number 0 -hls_time 5 -hls_playlist_type vod -hls_list_size 0 -f hls -y \"$newFilePath\"")
        }else{
            FFmpegKit.execute("-i \"$pathToVideo\" -codec: copy -start_number 0 -hls_time 10 -hls_playlist_type vod -hls_list_size 0 -f hls -y \"$newFilePath\"")
        }
        if (ReturnCode.isSuccess(session.returnCode)) {
//            postRepository.addLog(AddLogParams("toHls", "success"))
            // SUCCESS
        } else if (ReturnCode.isCancel(session.returnCode)) {
            postRepository.addLog(AddLogParams("toHls", "canceled"))
            // CANCEL
        } else {
            postRepository.addLog(AddLogParams("toHls", "error:${session.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "toHls"
            properties["message"] = session.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session.logsAsString)
        }
    }

    override fun generatePlaceholder(pathToVideo: String, newFilePath: String) {

        val session = if(FirebaseRemoteConfig.getInstance().getBoolean("new_video_type")){
            FFmpegKit.execute("-ss 1 -i \"$pathToVideo\" -qscale:v 10 -frames:v 1 \"$newFilePath\"")
        }else{
            FFmpegKit.execute("-i \"$pathToVideo\" -ss 00:00:01 -vframes 1 \"$newFilePath\"")
        }
        if (ReturnCode.isSuccess(session.returnCode)) {
//            postRepository.addLog(AddLogParams("generatePlaceholder", "success"))
            // SUCCESS
        } else if (ReturnCode.isCancel(session.returnCode)) {
            postRepository.addLog(AddLogParams("generatePlaceholder", "canceled"))
            // CANCEL
        } else {
            postRepository.addLog(AddLogParams("generatePlaceholder", "error:${session.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "generatePlaceholder"
            properties["message"] = session.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session.logsAsString)
        }
    }

    override fun prepare(media: Media, orientation: Int): VideoForPost {
        val directory = File(context.cacheDir, "video_post")
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        directory.mkdirs()
        val croppedPath = "${directory.path}/cropped.mp4"
        compressVideo(media, croppedPath, orientation)

        val name = UUID.randomUUID().toString()

        val playListPath = "${directory.path}/$name.m3u8"
        val placeholderPath = "${directory.path}/$name.jpg"
        toHls(croppedPath, playListPath)

        generatePlaceholder(croppedPath, placeholderPath)

        val files = directory.listFiles() ?: throw Exception("files null")

        val tss = mutableMapOf<String, String>()
        for (file in files) {
            val ext = MediaUtil.getExtension(context, file.toUri())
            if (ext == "ts") {
                tss[file.name] = file.path
            }
        }
        return VideoForPost(name, playListPath, placeholderPath, tss)
    }

    override fun prepareImage(media: Media, width:Int, height:Int): File {
        val directory = File(context.cacheDir, "image_post")
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        directory.mkdirs()
        val scaledPath = "${directory.path}/scaled.jpg"
        val croppedPath = "${directory.path}/cropped.jpg"

        val sourceWidth = media.width
        val sourceHeight = media.height

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        val xScale = width.toFloat() / sourceWidth
        val yScale = height.toFloat() / sourceHeight
        val scale = Math.max(xScale, yScale)

        // Now get the size of the source bitmap when scaled
        val scaledWidth = (scale * sourceWidth)+1
        val scaledHeight = (scale * sourceHeight)+1


        val forceOriginalAspectRatio = if(scaledWidth>sourceWidth || scaledHeight>sourceHeight){
            "increase"
        }else{
            "decrease"
        }

        val inputPath = FFmpegKitConfig.getSafParameterForRead(context, media.uri)
        val session = FFmpegKit.execute("-y -i \"$inputPath\" -filter:v \"scale=w=$scaledWidth:h=$scaledHeight:force_original_aspect_ratio=$forceOriginalAspectRatio\" \"$scaledPath\"")
        if (ReturnCode.isSuccess(session.returnCode)) {

        } else if (ReturnCode.isCancel(session.returnCode)) {
            postRepository.addLog(AddLogParams("prepareImage", "canceled"))
            throw Exception("canceled")
        } else {
            postRepository.addLog(AddLogParams("prepareImage", "error:${session.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "prepareImage"
            properties["message"] = session.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session.logsAsString)
        }

        val session1 = FFmpegKit.execute("-y -i \"$scaledPath\" -filter:v \"crop=w=$width:h=$height\" -q:v 10 \"$croppedPath\"")
        if (ReturnCode.isSuccess(session1.returnCode)) {

        } else if (ReturnCode.isCancel(session1.returnCode)) {
            postRepository.addLog(AddLogParams("prepareImage2", "canceled"))
            throw Exception("canceled")
        } else {
            postRepository.addLog(AddLogParams("prepareImage2", "error:${session1.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "prepareImage"
            properties["message"] = session1.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session1.logsAsString)
        }

        return File(croppedPath)
    }
    override fun prepareImage(inputPath: String, width:Int, height:Int): File {
        val directory = File(context.cacheDir, "image_post")
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        directory.mkdirs()
        val scaledPath = "${directory.path}/scaled.jpg"
        val croppedPath = "${directory.path}/cropped.jpg"

        val mediaInformation = FFprobeKit.getMediaInformation(inputPath)

        val sourceWidth = mediaInformation.mediaInformation.streams[0].width
        val sourceHeight = mediaInformation.mediaInformation.streams[0].height

        // Compute the scaling factors to fit the new height and width, respectively.
        // To cover the final image, the final scaling will be the bigger
        // of these two.
        val xScale = width.toFloat() / sourceWidth
        val yScale = height.toFloat() / sourceHeight
        val scale = Math.max(xScale, yScale)

        // Now get the size of the source bitmap when scaled
        val scaledWidth = (scale * sourceWidth)+1
        val scaledHeight = (scale * sourceHeight)+1


        val forceOriginalAspectRatio = if(scaledWidth>sourceWidth || scaledHeight>sourceHeight){
            "increase"
        }else{
            "decrease"
        }

        val session = FFmpegKit.execute("-y -i \"$inputPath\" -filter:v \"scale=w=$scaledWidth:h=$scaledHeight:force_original_aspect_ratio=$forceOriginalAspectRatio\" \"$scaledPath\"")
        if (ReturnCode.isSuccess(session.returnCode)) {

        } else if (ReturnCode.isCancel(session.returnCode)) {
            postRepository.addLog(AddLogParams("prepareImage", "canceled"))
            throw Exception("canceled")
        } else {
            postRepository.addLog(AddLogParams("prepareImage", "error:${session.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "prepareImage"
            properties["message"] = session.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session.logsAsString)
        }

        val session1 = FFmpegKit.execute("-y -i \"$scaledPath\" -filter:v \"crop=w=$width:h=$height\" -q:v 10 \"$croppedPath\"")
        if (ReturnCode.isSuccess(session1.returnCode)) {

        } else if (ReturnCode.isCancel(session1.returnCode)) {
            postRepository.addLog(AddLogParams("prepareImage2", "canceled"))
            throw Exception("canceled")
        } else {
            postRepository.addLog(AddLogParams("prepareImage2", "error:${session1.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "prepareImage"
            properties["message"] = session1.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session1.logsAsString)
        }

        return File(croppedPath)
    }

    override fun downloadVideo(context: Context, name:String, url: String, id:Long, callback:(DownloadState)->Unit) {
        val directory = File(context.cacheDir, "video_download")
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        directory.mkdirs()
        val tmpPath = "${directory.path}/tmp.mp4"
        val tmp2Path = "${directory.path}/tmp2.mp4"

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val hlsDownloader = HLSDownloader()
                hlsDownloader.downloadAndSave(url, tmpPath, id, callback)

                addWatermark(context, tmpPath, tmp2Path)

                saveVideoToMedia(context, name, tmp2Path)
                callback.invoke(DownloadState(id, 2, 100, null))
            }catch (e:Exception){
                callback.invoke(DownloadState(id, -1, 0, e.localizedMessage?:"error"))
            }
        }
    }

    override fun downloadPhotos(
        context: Context,
        medias: List<org.linkmessenger.data.local.entity.Media>,
        id: Long,
        callback: (DownloadState) -> Unit
    ) {
        val directory = File(context.cacheDir, "photo_download")
        if (directory.exists()) {
            directory.deleteRecursively()
        }
        directory.mkdirs()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imagesDownloader = ImagesDownloader()
                imagesDownloader.download(context, medias, directory.path, id, callback)
                callback.invoke(DownloadState(id, 2, 100, null))
            }catch (e:Exception){
                callback.invoke(DownloadState(id, -1, 0, e.localizedMessage?:"error"))
            }
        }
    }

    private fun addWatermark(context: Context, inputPath:String, outputPath:String){
        val watermarkPath = context.getWatermarkIcon()

        val session = FFmpegKit.execute("-i \"$inputPath\" -i \"$watermarkPath\" -filter_complex \"[1:v]scale=50:50 [ovrl], [0:v][ovrl]overlay=W-w-30:H-h-30\" \"$outputPath\"")
        if (ReturnCode.isSuccess(session.returnCode)) {

        } else if (ReturnCode.isCancel(session.returnCode)) {
            postRepository.addLog(AddLogParams("prepareImage", "canceled"))
            throw Exception("canceled")
        } else {
            postRepository.addLog(AddLogParams("prepareImage", "error:${session.logsAsString}"))
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "prepareImage"
            properties["message"] = session.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session.logsAsString)
        }
    }


    private fun saveVideoToMedia(context: Context, name:String, videoPath:String) {
        val videoFile = File(videoPath)

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES)
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, values)

        try {
            uri?.let { targetUri ->
                resolver.openOutputStream(targetUri)?.use { outputStream ->
                    val inputStream = FileInputStream(videoFile)
                    inputStream.copyTo(outputStream)
                    inputStream.close()
                    outputStream.close()
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getRealPathFromURI(context: Context, contentUri: Uri): String? {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = context.contentResolver.query(contentUri, proj, null, null, null)
            val column_index: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor?.moveToFirst()
            column_index?.let { cursor?.getString(it) }
        } finally {
            cursor?.close()
        }
    }
}
data class VideoForPost(
    val name:String,
    val playListPath:String,
    val placeholderPath:String,
    val tss:MutableMap<String, String>
)
data class DownloadState(
    val id:Long,
    val state:Int,
    val percent:Int,
    val error:String?
)