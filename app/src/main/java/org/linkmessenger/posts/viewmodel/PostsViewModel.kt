package org.linkmessenger.posts.viewmodel

import android.content.Context
import android.content.SharedPreferences
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.extensions.getHashtags
import org.linkmessenger.isImageExistsInMediaPictures
import org.linkmessenger.isVideoExistsInMediaVideos
import org.linkmessenger.notifyObserver
import org.linkmessenger.percent
import org.linkmessenger.posts.repository.DownloadState
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.posts.repository.VideoForPost
import org.linkmessenger.posts.repository.VideoRepository
import org.linkmessenger.request.models.*
import org.linkmessenger.utils.MediaEditor
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.util.MediaUtil
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class PostsViewModel(private val repository: PostRepository, private val videoRepository: VideoRepository, private val analyticsPrefs:SharedPreferences): ViewModel() {
    private val TAG = this::class.java.toString()
    val error:MutableLiveData<Exception?> = MutableLiveData()
    private val mediaEditor:MediaEditor = MediaEditor()
    val orientationType:MutableLiveData<Int> = MutableLiveData(0)
    val allowDownload:MutableLiveData<Boolean> = MutableLiveData()
    val medias: MutableLiveData<ArrayList<Media>> by lazy {
        MutableLiveData<ArrayList<Media>>()
    }
    val loading:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val description:MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val commentAvailable:MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }
    val downloadState:MutableLiveData<DownloadState?> = MutableLiveData()

    init {
        loading.postValue(false)
        commentAvailable.postValue(true)
        description.postValue(null)
        medias.value = arrayListOf()
        allowDownload.postValue(true)
    }
    fun removeMedia(media: Media){
        medias.value?.remove(media)
        CoroutineScope(Dispatchers.Main).launch {
            medias.notifyObserver()
        }
    }
    fun getByIndex(index:Int): Media? {
        return medias.value?.get(index)
    }
    fun postMedias(items:ArrayList<Media>){
        val v = items.find { MediaUtil.isVideo(it.mimeType) }
        if(v!=null){
            medias.postValue(arrayListOf(v))
        }else{
            medias.postValue(ArrayList(items.filter {!MediaUtil.isVideo(it.mimeType) }))
        }
    }
    fun clearMedias(){
        medias.postValue(arrayListOf())
    }
    fun setDescription(text:String?){
        description.value = text
    }
    fun setAllowDownload(value: Boolean){
        allowDownload.postValue(value)
    }
    fun setCommentAvailability(value: Boolean){
        commentAvailable.postValue(value)
    }
    fun addPostView(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val viewDate = analyticsPrefs.getLong("vd_$postId", 0)
                val now = Date().time
                if(now - viewDate > 1000*60*60){
                    val res = repository.addPostView(AddPostViewParams(post_id = postId))
                    if(!res.success) throw FromServerException(res.error?.message)
                    analyticsPrefs.edit().putLong("vd_$postId", now).apply()
                }
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
            }
        }
    }
    private fun publishImages(callback: (event: AddPostEvent) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            val mediaParams = ArrayList<AddPostMediaParams>()
            var p = 1
            medias.value?.forEach { _ ->
                val mediaSizes = when (orientationType.value) {
                    0 -> {
                        arrayListOf(
                            MediaSizeParams(1080,1080),
                            MediaSizeParams(720,720),
                            MediaSizeParams(480,480),
                            MediaSizeParams(80,80),
                        )
                    }
                    1 -> {
                        arrayListOf(
                            MediaSizeParams(1080,608),
                            MediaSizeParams(720,405),
                            MediaSizeParams(480,270),
                            MediaSizeParams(80,45),
                        )
                    }
                    else -> {
                        arrayListOf(
                            MediaSizeParams(1080,1350),
                            MediaSizeParams(720,900),
                            MediaSizeParams(480,600),
                            MediaSizeParams(80,100),
                        )
                    }
                }
                val mediaItem = AddPostMediaParams(
                    ext = "jpeg",
                    position = p,
                    type = 1,
                    orientation = orientationType.value?.plus(1) ?: 1,
                    sizes = mediaSizes
                )
                mediaParams.add(mediaItem)
                p++
            }
            val medias = AddPostMedias(photos = mediaParams, video = null)
            val params = AddPostParams(
                hashtags = description.value?.getHashtags(),
                description = description.value,
                media = medias,
                commentAvailable = commentAvailable.value ?: true,
                type = 1,
                allow_download = allowDownload.value ?:true
            )
            try {
                callback.invoke(AddPostEvent(AddPostEventType.Check, null, null))
                val response = repository.addPost(params)
                if(!response.success){
                    throw java.lang.Exception(response.error?.message)
                }
                val setMediaParams = uploadPostMedias(response.data!!, orientationType.value?.plus(1) ?: 1, callback)
                val setParams = SetPostParams(
                    post_id = response.data.post_id,
                    media = SetPostMedia(photos = setMediaParams, video = null),
                    type = 1
                )
                val res = repository.setPost(setParams)
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                callback.invoke(AddPostEvent(AddPostEventType.Complete, null, null))
            }catch (e:java.lang.Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                callback.invoke(AddPostEvent(AddPostEventType.Error, e.localizedMessage, null))
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }
    private fun publishVideo(v:Media, callback: (event: AddPostEvent) -> Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if(orientationType.value==null) {
                    callback.invoke(AddPostEvent(AddPostEventType.Error, "Invalid orientation", null))
                    return@launch
                }
                callback.invoke(AddPostEvent(AddPostEventType.Compress, null, null))
                val res = videoRepository.prepare(v, orientationType.value!!)

                val videoParams = AddPostVideoParams(
                    orientation = orientationType.value?.plus(1) ?: 1,
                    preloadPhotoFilename = "${res.name}.jpg",
                    playlistFilename = "${res.name}.m3u8",
                    segmentFilenames = res.tss.map { it.key }.toList()
                )
                val medias = AddPostMedias(photos = null, video = videoParams)
                val params = AddPostParams(
                    hashtags = description.value?.getHashtags(),
                    description = description.value,
                    media = medias,
                    commentAvailable = commentAvailable.value ?: true,
                    type = 2,
                    allow_download = allowDownload.value ?:true
                )
                callback.invoke(AddPostEvent(AddPostEventType.Check, null, null))
                val response = repository.addPost(params)
                if(!response.success){
                    throw java.lang.Exception(response.error?.message)
                }

                val setVideoParam = uploadPostVideo(response.data!!, res, orientationType.value?.plus(1) ?: 1, callback)
                val setParams = SetPostParams(
                    post_id = response.data.post_id,
                    media = SetPostMedia(photos = null, video = setVideoParam),
                    type = 2
                )
                val response1 = repository.setPost(setParams)
                if(!response1.success){
                    throw java.lang.Exception(response1.error?.message)
                }

                callback.invoke(AddPostEvent(AddPostEventType.Complete, null, null))
            }catch (e:Exception){
                repository.addLog(AddLogParams("publishVideo", e.stackTraceToString()))
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                callback.invoke(AddPostEvent(AddPostEventType.Error, e.localizedMessage, null))
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }
    fun publishPost(callback: (event: AddPostEvent) -> Unit){
        loading.postValue(true)
        val v = medias.value?.find { it1 -> MediaUtil.isVideo(it1.mimeType) }
        if(v!=null){
            publishVideo(v, callback)
        }else{
            publishImages(callback)
        }
    }
    private fun uploadPostVideo(addPostResponse: AddPostResponse, videoForPost: VideoForPost, o: Int, callback: (event: AddPostEvent) -> Unit):SetVideoParam{
        val file1 = File(videoForPost.playListPath)
        val res1 = repository.uploadVideo(addPostResponse.media!!.video!!.playlist.pre_signed_url, file1)
        if(res1!=null){
            throw java.lang.Exception(res1)
        }


        val p = when(addPostResponse.media.video!!.orientation){
            1->{
                Pair(1080,1080)
            }
            2->{
                Pair(1080,608)
            }
            else->{
                Pair(1080,1350)
            }
        }
        if(!FirebaseRemoteConfig.getInstance().getBoolean("old_image_type")){
            val newImage = videoRepository.prepareImage(videoForPost.placeholderPath, p.first, p.second)
            val res2 = repository.uploadMedia(addPostResponse.media.video.preload_photo.pre_signed_url, newImage)

            if(res2!=null){
                throw java.lang.Exception(res2)
            }
        }else{
            val file2 = File(videoForPost.placeholderPath)
            val b = mediaEditor.createSizedBitmap(file2.toUri(), p.first, p.second)
            val res2 = repository.uploadMedia(addPostResponse.media.video.preload_photo.pre_signed_url, b)
            if(res2!=null){
                throw java.lang.Exception(res2)
            }
        }




        callback.invoke(AddPostEvent(AddPostEventType.Uploading, null, 10))
        val total = addPostResponse.media.video.upload_urls.size
        for ((index, url) in addPostResponse.media.video.upload_urls.withIndex()){
            val file = File(videoForPost.tss[url.filename.replace("${addPostResponse.post_id}/","")]!!)
            val res = repository.uploadVideo(url.pre_signed_url, file)
            if(res!=null){
                throw java.lang.Exception(res)
            }
            callback.invoke(AddPostEvent(AddPostEventType.Uploading, null, total.percent(index+1)))
        }
        return SetVideoParam(addPostResponse.media.video.playlist.filename, addPostResponse.media.video.preload_photo.filename, o)
    }
    private fun uploadPostMedias(addPostResponse: AddPostResponse, o: Int, callback: (event: AddPostEvent) -> Unit):ArrayList<SetMediaParams>{
        val setMediaParams = ArrayList<SetMediaParams>()
        callback.invoke(AddPostEvent(AddPostEventType.Uploading, null, 10))
        val total = addPostResponse.media!!.photos!!.size
        for ((index, it) in addPostResponse.media.photos!!.withIndex()) {
            val media = medias.value!![it.position-1]
            for (url in it.Urls) {
                if(!FirebaseRemoteConfig.getInstance().getBoolean("old_image_type")){
                    val file = videoRepository.prepareImage(media, url.size.width, url.size.height)
                    val res = repository.uploadMedia(url.pre_signed_url, file)
                    if(res!=null){
                        throw java.lang.Exception(res)
                    }
                }else{
                    val data = mediaEditor.createSizedBitmap(media.uri, url.size.width, url.size.height)
                    val res = repository.uploadMedia(url.pre_signed_url, data)
                    if(res!=null){
                        throw java.lang.Exception(res)
                    }
                }
            }
            setMediaParams.add(SetMediaParams(it.filename, 1, it.position, o))
            callback.invoke(AddPostEvent(AddPostEventType.Uploading, null, total.percent(index+1)))
        }
        return setMediaParams
    }

    fun changeOrientation(){
        var tmp = orientationType.value ?:0
        tmp++
        if(tmp>2){
            tmp = 0
        }
        orientationType.postValue(tmp)
    }
    fun like(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.likePost(postId)
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }
    fun unLike(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.unLikePost(postId)
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }
    fun addPostToCollection(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.addPostToCollections(postId)
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }
    fun deletePostFromCollection(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.deletePostFromCollection(postId)
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }
    fun downloadVideo(context: Context, data: Post?):Boolean {
        if(downloadState.value==null){
            if(data==null || data.medias.isNullOrEmpty()) return true
            if (data.medias!![0]!!.type==2){
                if(!context.isVideoExistsInMediaVideos(data.id.toString())){
                    downloadState.postValue(DownloadState(data.id, 1, 0, null))
                    videoRepository.downloadVideo(context, data.id.toString(), data.medias!![0]!!.url, data.id){
                        downloadState.postValue(it)
                        if (it.state==2){
                            android.os.Handler(Looper.getMainLooper()).postDelayed({
                                downloadState.postValue(null)
                            },1000)
                        }
                    }
                }else{
                    downloadState.postValue(DownloadState(data.id, 3, 0, ""))
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        downloadState.postValue(null)
                    },1000)
                }
            }else if(data.medias!![0]!!.type==1){
                if(!context.isImageExistsInMediaPictures(data.medias!![0]!!.id.toString())){
                    downloadState.postValue(DownloadState(data.id, 1, 0, null))
                    videoRepository.downloadPhotos(context, data.medias!!, data.id){
                        downloadState.postValue(it)
                        if (it.state==2){
                            android.os.Handler(Looper.getMainLooper()).postDelayed({
                                downloadState.postValue(null)
                            },1000)
                        }
                    }
                }else{
                    downloadState.postValue(DownloadState(data.id, 3, 0, ""))
                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        downloadState.postValue(null)
                    },1000)
                }
            }
            return true
        }else{
            return false
        }
    }
}
data class AddPostEvent(
    val type:AddPostEventType,
    val error:String?,
    val progress:Int?
)
enum class AddPostEventType{
    Check, Compress, Uploading, Complete, Error
}