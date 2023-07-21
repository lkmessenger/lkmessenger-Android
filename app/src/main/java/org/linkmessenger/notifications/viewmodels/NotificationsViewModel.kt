package org.linkmessenger.notifications.viewmodels

import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.notifications.adapters.NotificationTabType
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.request.models.NotificationHistory

class NotificationsViewModel(val repository: ProfileRepository): BaseViewModel() {
    val notifications: MutableLiveData<ArrayList<NotificationHistory>> = MutableLiveData()
    val notificationsReaction: MutableLiveData<ArrayList<NotificationHistory>> = MutableLiveData()
    val notificationsComment: MutableLiveData<ArrayList<NotificationHistory>> = MutableLiveData()
    val notificationsFollowers: MutableLiveData<ArrayList<NotificationHistory>> = MutableLiveData()

//    val emptyState: MutableLiveData<Boolean> = MutableLiveData()
    val limit = 20
    var lastIdAll=0
    var lastIdFollow=0
    var lastIdReaction=0
    var lastIdComment=0
    fun loadNotification(type:String){
        if(lastIdAll != -1){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)

                    val response = repository.getNotificationsHistory(limit, lastIdAll, type)

                    if(!response.success){
                        throw java.lang.Exception(response.error?.message)
                    }

                    if(response.data?.notifications?.isNotEmpty() == true) {
                        notifications.postValue(response.data.notifications)
                    }

                    lastIdAll = if(response.data?.notifications?.isNotEmpty() == true){
                        if(response.data.notifications.size < limit){
                            -1
                        }else{
                            response.data.notifications.minByOrNull { p -> p.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                    loading.postValue(false)

                }catch (e:Exception){
                    FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                    error.postValue(e)
                }
            }
        }
    }

    fun refresh(type: NotificationTabType){
        when(type){
            NotificationTabType.All -> {
                lastIdAll = 0
                loadNotification("ALL")
            }
            NotificationTabType.Followers -> {
                lastIdFollow = 0
                loadNotificationFollowers("FOLLOW")
            }
            NotificationTabType.Comments -> {
                lastIdComment = 0
                loadNotificationComments("COMMENT")
            }
            NotificationTabType.Likes -> {
                lastIdReaction = 0
                loadNotificationLikes("REACTION")
            }
        }
    }

    fun loadNotificationLikes(typeString: String) {
        if(lastIdReaction != -1){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)

                    val response = repository.getNotificationsHistory(limit, lastIdReaction, typeString)

                    if(!response.success){
                        throw java.lang.Exception(response.error?.message)
                    }

                    if(response.data?.notifications?.isNotEmpty() == true) {
                        notificationsReaction.postValue(response.data.notifications)
                    }

                    lastIdReaction = if(response.data?.notifications?.isNotEmpty() == true){
                        if(response.data.notifications.size < limit){
                            -1
                        }else{
                            response.data.notifications.minByOrNull { p -> p.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                    loading.postValue(false)

                }catch (e:Exception){
                    FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                    error.postValue(e)
                }
            }
        }
    }

    fun loadNotificationComments(typeString: String) {
        if(lastIdComment != -1){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)

                    val response = repository.getNotificationsHistory(limit, lastIdComment, typeString)

                    if(!response.success){
                        throw java.lang.Exception(response.error?.message)
                    }

                    if(response.data?.notifications?.isNotEmpty() == true) {
                        notificationsComment.postValue(response.data.notifications)
                    }

                    lastIdComment = if(response.data?.notifications?.isNotEmpty() == true){
                        if(response.data.notifications.size < limit){
                            -1
                        }else{
                            response.data.notifications.minByOrNull { p -> p.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                    loading.postValue(false)

                }catch (e:Exception){
                    FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                    error.postValue(e)
                }
            }
        }
    }

    fun loadNotificationFollowers(typeString: String) {
        if(lastIdFollow != -1){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)

                    val response = repository.getNotificationsHistory(limit, lastIdFollow, typeString)

                    if(!response.success){
                        throw java.lang.Exception(response.error?.message)
                    }

                    if(response.data?.notifications?.isNotEmpty() == true) {
                        notificationsFollowers.postValue(response.data.notifications)
                    }

                    lastIdFollow = if(response.data?.notifications?.isNotEmpty() == true){
                        if(response.data.notifications.size < limit){
                            -1
                        }else{
                            response.data.notifications.minByOrNull { p -> p.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                    loading.postValue(false)

                }catch (e:Exception){
                    FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                    error.postValue(e)
                }
            }
        }
    }
}