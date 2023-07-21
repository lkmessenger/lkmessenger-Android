package org.linkmessenger.posts.viewmodel

import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.profile.models.ProfileData

class PostLikesViewModel(private val postRepository: PostRepository):BaseViewModel() {
    val users:MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    var limit = 20
    var page = 0

    init {
        page = 0
    }

    fun loadUsers(postId:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)

                val res = postRepository.getPostLikes(postId, limit, page)

                loading.postValue(false)

                if(res.data != null){
                    if(res.data.users?.isNotEmpty() == true){
                        users.postValue(res.data.users as ArrayList<ProfileData>)
                        page++
                    }
                }

            }catch (e:Exception){
                loading.postValue(false)
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }
}