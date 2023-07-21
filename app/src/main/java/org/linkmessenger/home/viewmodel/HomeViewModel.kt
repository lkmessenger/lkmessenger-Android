package org.linkmessenger.home.viewmodel

import android.content.SharedPreferences
import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import com.microsoft.appcenter.analytics.Analytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.data.local.entity.MyPost
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.data.local.entity.SubsPost
import org.linkmessenger.notifyObserver
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.request.models.NotificationCountResponse

class HomeViewModel(private val postRepository: PostRepository, private val pref: SharedPreferences):BaseViewModel() {
    val posts: MutableLiveData<ArrayList<Post>> = MutableLiveData()
    val emptyState: MutableLiveData<Boolean> = MutableLiveData()
    val notificationsCount:MutableLiveData<NotificationCountResponse?> = MutableLiveData()

    var limit = 20
    var lastPostId=0L
    var forceOnline = true
    var isNotificationChecked = false

    var isRemovefirst = true

    private lateinit var state: Parcelable
    fun saveRecyclerViewState(parcelable: Parcelable) { state = parcelable }
    fun restoreRecyclerViewState() : Parcelable = state
    fun stateInitialized() : Boolean = ::state.isInitialized

    init {
        isRemovefirst = true
        forceOnline = true
//        lastPostId = 0
    }
    fun loadPosts(){
        if(lastPostId!=-1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)
                    val res = postRepository.getSubscriptionPosts(limit, lastPostId, forceOnline)
                    if(res.isNotEmpty()){
                        emptyState.postValue(false)
                        if(isRemovefirst){
                            isRemovefirst = false
                            posts.postValue(res as ArrayList<Post>)
                        }else{
                            val loadedPosts = res.toMutableList().apply {
                                removeAt(0)
                            }
                            posts.postValue(loadedPosts as ArrayList<Post>)
                        }
                    }else{
                        emptyState.postValue(true)
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
    fun loadNotificationsCount(){
        val hourLimit = 3 * 60 * 60

        val oldTime: Long = pref.getLong("notification_check_time", 0)
        val currentTime: Long = System.currentTimeMillis() / 1000

//        if(currentTime - oldTime > hourLimit && !isNotificationChecked){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val res = postRepository.getNotificationsCount()
                    if(res.data != null){
                        notificationsCount.postValue(res.data as NotificationCountResponse)
                    }

                    pref.edit().putLong("notification_check_time", currentTime).apply()

                    isNotificationChecked = true
                }catch (e:Exception){
                    error.postValue(e)
                }
            }
//        }
    }

    fun onDestroy(oldPosts: ArrayList<Post>){
        isNotificationChecked = false
        notificationsCount.value = null
        posts.postValue(oldPosts)
    }

    fun refresh() {
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.resetPosts(SubsPost::class.java)
        }
        forceOnline = true
        lastPostId = 0
        loadPosts()
    }
    fun deleteNotificationsCount(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = postRepository.deleteNotificationCount()
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:Exception){
                error.postValue(e)
            }
        }
    }
}