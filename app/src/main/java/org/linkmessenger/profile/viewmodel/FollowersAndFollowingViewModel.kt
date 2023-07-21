package org.linkmessenger.profile.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.request.models.MessageRequest
import org.linkmessenger.request.models.SubscribeParams

class FollowersAndFollowingViewModel(val repository: ProfileRepository) : BaseViewModel() {
    val followers: MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val subscriptions: MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val subscribeLoading:MutableLiveData<Boolean> = MutableLiveData()
    val messageRequests: MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val recommendations: MutableLiveData<ArrayList<ProfileData>?> = MutableLiveData()
    val subscriptionsEmptyState: MutableLiveData<Boolean> = MutableLiveData()
    val messageRequestsEmptyState: MutableLiveData<Boolean> = MutableLiveData()
    val followersEmptyState: MutableLiveData<Boolean> = MutableLiveData()
    val recommendationsEmptyState: MutableLiveData<Boolean> = MutableLiveData()
    val sentMessageRequests: MutableLiveData<ArrayList<ProfileData>> = MutableLiveData()
    val sentMessageRequestsEmptyState: MutableLiveData<Boolean> = MutableLiveData()

    var followersPage = 1
    var subscriptionsPage = 1
    var messageRequestsId = 0L
    var sentMessageRequestsId = 0L
    var isFirstLoading = true
    var limit = 20

    init {
        subscriptionsEmptyState.postValue(false)
        followersEmptyState.postValue(false)
        recommendationsEmptyState.postValue(false)
        messageRequestsEmptyState.postValue(false)
        sentMessageRequestsEmptyState.postValue(false)
    }

    fun loadFollowers(isMyProfile: Boolean, userId: Int?){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = if(isMyProfile){repository.getMyFollowers(limit, followersPage)}
                          else{ repository.getUserFollowers(userId!!, limit, followersPage) }
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data?.users != null){
                    followers.postValue(res.data.users as ArrayList<ProfileData>)
                    followersPage++
                    followersEmptyState.postValue(false)
                }

                if(res.data?.users == null && followersPage == 1){
                    followersEmptyState.postValue(true)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun loadSubscriptions(isMyProfile: Boolean, userId: Int?){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)
                val res = if(isMyProfile){repository.getMySubscriptions(limit, subscriptionsPage)}
                          else{ repository.getUserSubscriptions(userId!!, limit, subscriptionsPage) }
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
                if(res.data?.users != null){
                    subscriptions.postValue(res.data.users as ArrayList<ProfileData>)
                    subscriptionsPage++
                    subscriptionsEmptyState.postValue(false)
                }

                if(res.data?.users == null && subscriptionsPage == 1){
                    subscriptionsEmptyState.postValue(true)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun denyRequest(id:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.denyMessage(id)

                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun acceptRequest(id:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.allowMessage(id)

                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }

    fun loadMessageRequests(){
        if(messageRequestsId != -1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)
                    val res = repository.getMessageRequests(limit, messageRequestsId)

                    if(!res.success){
                        throw java.lang.Exception(res.error?.message)
                    }
                    if(res.data?.items != null){
                        val profiles: ArrayList<ProfileData> = arrayListOf()
                        isFirstLoading = false
                        for (data in res.data.items){
                            val profileData = data.user
                            profileData?.messageRequestId = data.id
                            profiles.add(profileData!!)
                        }

                        messageRequests.postValue(profiles)
                        messageRequestsEmptyState.postValue(false)
                    }

                    messageRequestsId = if(res.data?.items?.isNotEmpty() == true){
                        if(res.data.items.size < limit){
                            -1L
                        }else{
                            res.data.items.minByOrNull { p -> p.id }?.id ?:0L
                        }
                    }else{
                        -1L
                    }

                    if(res.data?.items == null && isFirstLoading){
                        messageRequestsEmptyState.postValue(true)
                    }

                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    loading.postValue(false)
                }
            }
        }
    }
    fun loadSentMessageRequests(){
        if(sentMessageRequestsId != -1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)
                    val res = repository.getSentChatRequest(limit, sentMessageRequestsId)

                    if(!res.success){
                        throw java.lang.Exception(res.error?.message)
                    }
                    if(res.data?.items != null){
                        val profiles: ArrayList<ProfileData> = arrayListOf()
                        isFirstLoading = false
                        for (data in res.data.items){
                            val profileData = data.user
                            profileData?.messageRequestId = data.id
                            profileData?.messageRequestStatus = data.status
                            profiles.add(profileData!!)
                        }

                        sentMessageRequests.postValue(profiles)
                        sentMessageRequestsEmptyState.postValue(false)
                    }

                    sentMessageRequestsId = if(res.data?.items?.isNotEmpty() == true){
                        if(res.data.items.size < limit){
                            -1L
                        }else{
                            res.data.items.minByOrNull { p -> p.id }?.id ?:0L
                        }
                    }else{
                        -1L
                    }

                    if(res.data?.items == null && isFirstLoading){
                        sentMessageRequestsEmptyState.postValue(true)
                    }

                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    loading.postValue(false)
                }
            }
        }
    }

    fun subscribe(userId: Int){
            CoroutineScope(Dispatchers.IO).launch {
                subscribeLoading.postValue(true)
                try {
                    val res = repository.subscribe(SubscribeParams(userId))
                    if(!res.success){
                        throw java.lang.Exception(res.error?.message)
                    }
                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    subscribeLoading.postValue(false)
                }
            }
        }

    fun deleteFollower(userId: Int){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = repository.deleteFollower(SubscribeParams(userId))
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:java.lang.Exception){
                error.postValue(e)
            }
        }
    }
    fun unSubscribe(userId:Int){
            CoroutineScope(Dispatchers.IO).launch {
                subscribeLoading.postValue(true)
                try {
                    val res = repository.unSubscribe(SubscribeParams(userId))
                    if(!res.success){
                        throw java.lang.Exception(res.error?.message)
                    }
                }catch (e:java.lang.Exception){
                    error.postValue(e)
                }finally {
                    subscribeLoading.postValue(false)
                }
        }
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
                    recommendationsEmptyState.postValue(false)
                }else{
                    recommendationsEmptyState.postValue(true)
                }
            }catch (err: Exception){
                error.postValue(err)
            }
        }
    }
}