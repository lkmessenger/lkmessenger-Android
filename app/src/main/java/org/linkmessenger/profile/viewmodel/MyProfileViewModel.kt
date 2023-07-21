package org.linkmessenger.profile.viewmodel

import android.os.Looper
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.data.local.entity.MyPost
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.notifyObserver
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.request.models.PrivacySettingsParams
import org.linkmessenger.request.models.PrivacySettingsResponse
import org.linkmessenger.request.models.SubscribeParams
import org.linkmessenger.request.models.VersionResponse
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.ApplicationContext
import org.thoughtcrime.securesms.backup.BackupProtos.SharedPreference

class MyProfileViewModel(val repository: ProfileRepository,
                         private val postRepository: PostRepository):BaseViewModel() {
    private val TAG = MyProfileViewModel::class.java.toString()
    val myProfileData:MutableLiveData<ProfileData> = MutableLiveData()
    val posts:MutableLiveData<ArrayList<Post>> = MutableLiveData()
    val recommendations: MutableLiveData<ArrayList<ProfileData>?> = MutableLiveData()
    val emptyState: MutableLiveData<Boolean> = MutableLiveData()

    var limit = 20
    var lastPostId=0L
    var forceOnline = true

    var version: MutableLiveData<VersionResponse?> = MutableLiveData()

//    val privacySettings: MutableLiveData<PrivacySettingsParams?> = MutableLiveData()

    init {
        forceOnline = true
        lastPostId = 0
        loadProfile(false)
//        getPrivacySettings()
    }
    private fun loadProfile(refresh:Boolean){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val profileData = repository.getMyProfile(refresh)
                myProfileData.postValue(profileData)
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }
    fun loadPosts(){
        if(lastPostId!=-1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)
                    val res = postRepository.getMyPosts(limit, lastPostId, forceOnline)
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
    fun loadPostsToId(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = postRepository.getMyPostsToId(postId)
                if(res.isNotEmpty()){
                    posts.postValue(res as ArrayList<Post>?)
                }
                lastPostId = if(res.isNotEmpty()){
                    res.minByOrNull { p -> p.id }?.id ?:0
                }else{
                    -1
                }
            }catch (e:Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun refresh() {
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.resetPosts(MyPost::class.java)
        }
        loadProfile(true)
        forceOnline = true
        lastPostId = 0
        loadPosts()
    }

    fun loadRecommendations(userId: Int?){
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
                FirebaseCrashlytics.getInstance().log(err.stackTraceToString())
                error.postValue(err)
            }
        }
    }

    fun subscribe(userId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.subscribe(SubscribeParams(userId))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(myProfileData.value!=null){
                    myProfileData.value!!.subscriptionsCount++
                    CoroutineScope(Dispatchers.Main).launch {
                        myProfileData.notifyObserver()
                    }
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }

    fun unSubscribe(userId:Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.unSubscribe(SubscribeParams(userId))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(myProfileData.value!=null){
                    myProfileData.value!!.subscriptionsCount--
                    CoroutineScope(Dispatchers.Main).launch {
                        myProfileData.notifyObserver()
                    }
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }

    fun getVersion(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.getVersion("en_EN")
                if(res != null){
                    version.postValue(res)
                }
            }catch (e:Exception){
                error.postValue(e)
            }
        }
    }
//    fun updatePrivacySettings(params: PrivacySettingsParams){
//        privacySettings.postValue(params)
//
//        CoroutineScope(Dispatchers.IO).launch {
//            try{
//                val response = repository.updatePrivacySettings(params)
//
//                if(!response.success){
//                    throw java.lang.Exception(response.error?.message)
//                }
//            }catch (err: Exception){
//                error.postValue(err)
//            }
//        }
//    }

//    fun getPrivacySettings(){
//        CoroutineScope(Dispatchers.IO).launch {
//            try{
//                val response = repository.getPrivacySettings()
//                if(!response.success){
//                    throw java.lang.Exception(response.error?.message)
//                }
//
//                if(response.data != null){
//                    privacySettings.postValue(PrivacySettingsParams(response.data.phoneVisibility, response.data.messengerAccess))
//                }
//            }catch (err: Exception){
//                error.postValue(err)
//            }
//        }
//    }
}