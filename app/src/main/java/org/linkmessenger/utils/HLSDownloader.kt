package org.linkmessenger.utils

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.ResponseBody
import org.linkmessenger.posts.repository.DownloadState
import java.io.File
import java.io.FileOutputStream

class HLSDownloader {
    private val client = OkHttpClient()
    fun downloadAndSave(hlsUrl: String, outputPath: String, id:Long,  callback: (DownloadState) -> Unit) {
        val outputFile = File(outputPath)
        val hlsManifest = fetchHLSManifest(hlsUrl)
        val segmentUrls = parseHLSManifest(hlsManifest)
        downloadAndMergeSegments(hlsUrl, segmentUrls, outputFile,id, callback)
        println("Video downloaded and saved as ${outputFile.absolutePath}")
    }

    private fun fetchHLSManifest(hlsUrl: String): String {
        val request = Request.Builder()
            .url(hlsUrl)
            .build()
        val response = client.newCall(request).execute()
        val hlsManifest = response.body()?.string()
        response.close()
        return hlsManifest.orEmpty()
    }

    private fun parseHLSManifest(manifestContent: String): List<String> {
        val urls = mutableListOf<String>()
        manifestContent.lines().forEach { line ->
            if (!line.startsWith("#")) {
                urls.add(line.trim())
            }
        }
        return urls
    }

    private fun downloadAndMergeSegments(
        hlsUrl: String,
        segmentUrls: List<String>,
        outputFile: File,
        id:Long,
        callback:(DownloadState)->Unit
    ) {
        val url = hlsUrl.substringBeforeLast('/')
        val outputStream = FileOutputStream(outputFile)
        for ((index, segmentUrl) in segmentUrls.withIndex()) {
            val request = Request.Builder()
                .url("$url/$segmentUrl")
                .build()
            val response = client.newCall(request).execute()
            val responseBody: ResponseBody? = response.body()
            responseBody?.byteStream()?.copyTo(outputStream)
            response.close()

            val p = ((index+1).toFloat()/segmentUrls.size)*100
            callback.invoke(DownloadState(id,1, p.toInt(), null))
        }
        outputStream.close()
    }
}
