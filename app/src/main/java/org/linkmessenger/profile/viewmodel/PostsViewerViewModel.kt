package org.linkmessenger.profile.viewmodel

import android.content.Context
import android.os.Looper
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.data.local.entity.MyPost
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.data.local.entity.mappers.PostDataToPostMapper
import org.linkmessenger.isImageExistsInMediaPictures
import org.linkmessenger.isVideoExistsInMediaVideos
import org.linkmessenger.posts.repository.DownloadState
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.posts.repository.VideoRepository

class PostsViewerViewModel(private val postRepository: PostRepository, private val videoRepository: VideoRepository): BaseViewModel() {
    val posts: MutableLiveData<ArrayList<Post>> = MutableLiveData()
    var limit = 20
    var lastPostId=0L
    var page = 1
    var isFirst = true
    var isRemovefirst = true
    init {
        isFirst = true
        isRemovefirst = true
        lastPostId = 0
    }
    fun loadMyPosts(){
        if(lastPostId!=-1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val res = postRepository.getMyPosts(limit, lastPostId, false)
                    if(res.isNotEmpty()){
                        posts.postValue(res as ArrayList<Post>)
                    }
                    lastPostId = if(res.isNotEmpty()){
                        if(res.size<limit){
                            -1
                        }else{
                            res.minByOrNull { p -> p.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    loading.postValue(false)
                }
            }
        }else{
            loading.postValue(false)
        }
    }
    fun deletePost(postId:Long, callback:()->Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                postRepository.deletePost(postId)
                CoroutineScope(Dispatchers.Main).launch {
                    callback.invoke()
                }

            }catch (e:Exception){
                error.postValue(e)
            }
        }
    }
    fun loadMyPostsToId(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = postRepository.getMyPostsToId(postId)
                if(res.isNotEmpty()){
                    posts.postValue(res as ArrayList<Post>?)
                }
                lastPostId = if(res.isNotEmpty()){
                    res.minByOrNull { p -> p.id }?.id ?:0
                }else{
                    0
                }
            }catch (e:Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }
    fun loadUserPostsToId(userId:Int, postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = postRepository.getUserPostsToId(userId, postId)
                if(res.isNotEmpty()){
                    posts.postValue(res as ArrayList<Post>?)
                }
                lastPostId = if(res.isNotEmpty()){
                    res.minByOrNull { p -> p.id }?.id ?:0
                }else{
                    0
                }
            }catch (e:Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun loadSharePosts() {
        if(lastPostId!=-1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val res = postRepository.getRecommendationPosts(limit, lastPostId, false)
                    if(res.isNotEmpty()){
                        if(isRemovefirst){
                            isRemovefirst = false
                            posts.postValue(res as ArrayList<Post>)
                        }else{
                            val loadedPosts = res.toMutableList().apply {
                                removeAt(0)
                            }
                            posts.postValue(loadedPosts as ArrayList<Post>)
                        }
                    }
                    lastPostId = if(res.isNotEmpty()){
                        if(res.size<limit){
                            -1
                        }else{
                            res.minByOrNull { p -> p.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    loading.postValue(false)
                }
            }
        }else{
            loading.postValue(false)
        }
    }
    fun loadUserPosts(userId:Int) {
        if(lastPostId!=-1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val res = postRepository.getUserPosts(userId, limit, lastPostId, false)
                    if(res.isNotEmpty()){
                        posts.postValue(res as ArrayList<Post>)
                    }
                    lastPostId = if(res.isNotEmpty()){
                        if(res.size<limit){
                            -1
                        }else{
                            res.minByOrNull { p -> p.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    loading.postValue(false)
                }
            }
        }else{
            loading.postValue(false)
        }
    }
    fun loadCollections(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)

                val res = postRepository.getCollections(page, limit)
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }

                if(res.data?.posts?.isNotEmpty() == true){
                    val response = res.data.posts!!.map {p-> PostDataToPostMapper(false, MyPost::class.java).transform(p) }
                    posts.postValue(response as ArrayList<Post>)
                    loading.postValue(false)
                    page++
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }



}