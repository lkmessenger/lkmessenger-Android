package org.linkmessenger.profile.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.data.local.entity.MyPost
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.data.local.entity.UserPost
import org.linkmessenger.notifyObserver
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.request.models.PrivacySettingsParams
import org.linkmessenger.request.models.SubscribeParams
import org.linkmessenger.request.models.TrendingParams

class ProfileViewModel(val repository: ProfileRepository, private val postRepository: PostRepository):BaseViewModel() {
    val posts:MutableLiveData<ArrayList<Post>> = MutableLiveData()
    val profileData:MutableLiveData<ProfileData?> = MutableLiveData()
    val subscribeLoading:MutableLiveData<Boolean> = MutableLiveData()
    val recommendations: MutableLiveData<ArrayList<ProfileData>?> = MutableLiveData()
//    val userFromUid: MutableLiveData<ProfileData?> = MutableLiveData()
    val blockedUsers: MutableLiveData<ArrayList<ProfileData>?> = MutableLiveData()

    val emptyState: MutableLiveData<Boolean> = MutableLiveData()

    var limit = 20
    var lastPostId=0L
    var forceOnline = true

    init {
        emptyState.postValue(false)
    }

    fun sendRequestForMessage(userId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.requestToSendMessage(userId)
                if(res.success){
                    profileData.value?.isChatRequested = true
                }
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
            }
        }
    }
    fun sendRequestForMessageAnswer(id:Long, status:Int, callback:(success:Boolean)->Unit){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = if(status==1){
                    repository.allowMessage(id)
                }else{
                    repository.denyMessage(id)
                }
                callback.invoke(res.success)
                return@launch
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                callback.invoke(false)
                return@launch
            }
        }
    }
    fun loadProfile(id:Int?=null, username:String?=null, uuid: String? = null){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = if(id!=null){
                    repository.getAntherProfile(id)
                }else if(!username.isNullOrEmpty()){
                    repository.getAntherProfileByUsername(username)
                } else if(!uuid.isNullOrEmpty()){
                    repository.getUserByUuid(uuid)
                } else{
                    throw Exception("id or username empty")
                }
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                res.data?.let {
                    CoroutineScope(Dispatchers.IO).launch {
//                        id?.let { postRepository.resetPosts(it, UserPost::class.java) }
                        it.let {
                            postRepository.resetPosts(it.id, UserPost::class.java)
                        }
                    }
                    profileData.postValue(it)
                    lastPostId = 0
                    forceOnline = true
                    CoroutineScope(Dispatchers.Main).launch {
                        loadPosts()
                    }
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }
    fun loadPosts(){
        if(profileData.value!=null){
            if(lastPostId!=-1L){
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        loading.postValue(true)
                        val res = postRepository.getUserPosts(profileData.value!!.id, limit, lastPostId, true)
                        if(res.isNotEmpty()){
                            posts.postValue(res as ArrayList<Post>)
                            emptyState.postValue(false)
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

                        if(res.isEmpty() && lastPostId == 0L){
                            emptyState.postValue(true)
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
    }

    fun subscribe(){
        if(profileData.value!=null && profileData.value!!.id!=0){
            CoroutineScope(Dispatchers.IO).launch {
                subscribeLoading.postValue(true)
                try {
                    val res = repository.subscribe(SubscribeParams(profileData.value!!.id))
                    if(!res.success){
                        throw java.lang.Exception(res.error?.message)
                    }
                    if(profileData.value!=null){
                        profileData.value!!.isSubscribed = true
                        profileData.value!!.followersCount++
                        CoroutineScope(Dispatchers.Main).launch {
                            profileData.notifyObserver()
                        }
                    }
                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    subscribeLoading.postValue(false)
                }
            }
        }
    }
    fun unSubscribe(){
        if(profileData.value!=null && profileData.value!!.id!=0){
            CoroutineScope(Dispatchers.IO).launch {
                subscribeLoading.postValue(true)
                try {
                    val res = repository.unSubscribe(SubscribeParams(profileData.value!!.id))
                    if(!res.success){
                        throw java.lang.Exception(res.error?.message)
                    }
                    if(profileData.value!=null){
                        profileData.value!!.isSubscribed = false
                        profileData.value!!.followersCount--
                        CoroutineScope(Dispatchers.Main).launch {
                            profileData.notifyObserver()
                        }
                    }
                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    subscribeLoading.postValue(false)
                }
            }
        }
    }

    fun subscribeRecommendation(userId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.subscribe(SubscribeParams(userId))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }

    fun unSubscribeRecommendation(userId:Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.unSubscribe(SubscribeParams(userId))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }

    fun loadRecommendations(userId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.getRecommendationUsers(userId)

                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data?.users?.isNotEmpty() == true){
                    loading.postValue(false)
                    recommendations.postValue(res.data.users)
                }

            }catch (err: Exception){
                error.postValue(err)
            }
        }
    }

    fun blockUser(userId:Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.blockUser(SubscribeParams(userId))

                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }

            }catch (err: Exception){
                error.postValue(err)
            }
        }
    }

    fun unblockUser(userId:Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.unblockUser(SubscribeParams(userId))

                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }

            }catch (err: Exception){
                error.postValue(err)
            }
        }
    }

    fun refresh(id:Int?, username: String?) {
        loadProfile(id, username)
    }

    fun loadBlockedUsers(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.getBlockedUsers()
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data != null){
                    blockedUsers.postValue(res.data as ArrayList<ProfileData>)
                }

            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }
}