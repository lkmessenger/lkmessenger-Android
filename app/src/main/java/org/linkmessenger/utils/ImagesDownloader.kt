package org.linkmessenger.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import com.microsoft.appcenter.analytics.Analytics
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.data.local.entity.Media
import org.linkmessenger.data.local.entity.getSmallPhoto
import org.linkmessenger.getWatermarkIcon
import org.linkmessenger.posts.repository.DownloadState
import org.linkmessenger.request.models.AddLogParams
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

class ImagesDownloader {
    private val client = OkHttpClient()
    fun download(context: Context, medias:List<Media>, outputPath: String, id:Long,  callback: (DownloadState) -> Unit){
        for ((index, media) in medias.withIndex()) {
            val outputFile = File("$outputPath/${media.id}.jpeg")
            val outputStream = FileOutputStream(outputFile)
            val request = Request.Builder()
                .url(media.getSmallPhoto(PostViewType.List))
                .build()
            val response = client.newCall(request).execute()
            val responseBody: ResponseBody? = response.body()
            responseBody?.byteStream()?.copyTo(outputStream)
            response.close()
            outputStream.close()

            addWatermark(context, "$outputPath/${media.id}.jpeg", "$outputPath/tmp_${media.id}.jpeg")
            saveToMedia(context, media.id.toString(), "$outputPath/tmp_${media.id}.jpeg")

            val p = ((index+1).toFloat()/medias.size)*100
            callback.invoke(DownloadState(id,1, p.toInt(), null))
        }
    }
    private fun addWatermark(context:Context, inputPath:String, outputPath: String){
        val watermarkPath = context.getWatermarkIcon()

        val session = FFmpegKit.execute("-i \"$inputPath\" -i \"$watermarkPath\" -filter_complex \"overlay=W-w-30:H-h-30\" \"$outputPath\"")
        if (ReturnCode.isSuccess(session.returnCode)) {

        } else if (ReturnCode.isCancel(session.returnCode)) {
            throw Exception("canceled")
        } else {
            val properties: MutableMap<String, String> = HashMap()
            properties["tag"] = "prepareImage"
            properties["message"] = session.logsAsString
            Analytics.trackEvent("Error", properties)
            throw Exception(session.logsAsString)
        }
    }
    private fun saveToMedia(context: Context, name:String, photoPath:String) {
        val photoFile = File(photoPath)

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val resolver = context.contentResolver
        val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val uri = resolver.insert(collection, values)

        uri?.let { targetUri ->
            resolver.openOutputStream(targetUri)?.use { outputStream ->
                val inputStream = FileInputStream(photoFile)
                inputStream.copyTo(outputStream)
                inputStream.close()
                outputStream.close()
            }
        }
    }
}