package org.linkmessenger.contacts.viewmodel

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.contacts.repository.ContactsRepository
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.data.local.entity.SharePost
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.profile.models.ProfileData

class ContactsViewModel(val repository: ContactsRepository, private val postRepository: PostRepository):BaseViewModel() {
    val users:MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val posts: MutableLiveData<ArrayList<Post>> = MutableLiveData(arrayListOf())
    var page = 1
    var limit = 20
    var lastPostId=0L
    var forceOnline = true

    private lateinit var state: Parcelable
    fun saveRecyclerViewState(parcelable: Parcelable) { state = parcelable }
    fun restoreRecyclerViewState() : Parcelable = state
    fun stateInitialized() : Boolean = ::state.isInitialized

    init {
        forceOnline = true
        lastPostId = 0
    }
    fun searchContacts(q:String){
        if(q.length<2) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = repository.searchUsersByUsername(q, limit, page)
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data?.users?.isNotEmpty() == true){
                    res.data.users.let { u->
                        users.postValue(u)
                        page++
                    }
                }
            }catch (e:java.lang.Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun loadPosts(){
        if(lastPostId!=-1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)
                    val res = postRepository.getRecommendationPosts(limit, lastPostId, true)
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
                    FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                    error.postValue(e)
                }finally {
                    loading.postValue(false)
                }
            }
        }else{
            loading.postValue(false)
        }
    }

    fun onDestroy(oldPosts: ArrayList<Post>){
        posts.postValue(oldPosts)
    }



    fun refresh(){
        forceOnline = true
        CoroutineScope(Dispatchers.IO).launch {
            postRepository.resetPosts(SharePost::class.java)
        }
        lastPostId = 0
        loadPosts()
    }
}