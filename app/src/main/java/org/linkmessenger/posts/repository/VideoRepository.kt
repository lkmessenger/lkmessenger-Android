package org.linkmessenger.posts.repository

import android.content.Context
import org.thoughtcrime.securesms.mediasend.Media
import java.io.File


interface VideoRepository {
    fun compressVideo(media: Media, newFilePath:String, orientation:Int)
    fun toHls(pathToVideo:String, newFilePath: String)
    fun generatePlaceholder(pathToVideo:String, newFilePath: String)
    fun prepare(media: Media, orientation:Int):VideoForPost
    fun prepareImage(media: Media, width:Int, height:Int): File
    fun prepareImage(inputPath: String, width:Int, height:Int): File
    fun downloadVideo(context: Context, name:String, url:String, id:Long, callback:(DownloadState)->Unit)
    fun downloadPhotos(context: Context, medias:List<org.linkmessenger.data.local.entity.Media>, id:Long, callback:(DownloadState)->Unit)
}