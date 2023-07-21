package org.linkmessenger.collections.viewmodels

import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.data.local.entity.MyPost
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.data.local.entity.mappers.PostDataToPostMapper
import org.linkmessenger.posts.repository.PostRepository

class CollectionsViewModel(val repository: PostRepository) : BaseViewModel() {
    val posts: MutableLiveData<ArrayList<Post>> = MutableLiveData()
    var page = 1
    val limit = 20
    val emptyState: MutableLiveData<Boolean> = MutableLiveData()

    init {
        emptyState.postValue(false)
    }

    fun loadCollections(){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                loading.postValue(true)

                val res = repository.getCollections(page, limit)
                if(!res.success){
                    throw java.lang.Exception(res.error?.message)
                }

                if(res.data?.posts?.isNotEmpty() == true){
                    emptyState.postValue(false)
                    val response = res.data.posts!!.map {p-> PostDataToPostMapper(false, MyPost::class.java).transform(p) }
                    response.reversed()
                    posts.postValue(response as ArrayList<Post>)
                    loading.postValue(false)
                    page++
                }else if(page == 1){
                    emptyState.postValue(true)
                }

            }catch (e:java.lang.Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }finally {
                loading.postValue(false)
            }
        }
    }
}