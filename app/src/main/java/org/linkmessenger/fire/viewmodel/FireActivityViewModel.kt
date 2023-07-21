package org.linkmessenger.fire.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.request.models.PostData

class FireActivityViewModel(private val postRepository: PostRepository): BaseViewModel() {
    val posts: MutableLiveData<ArrayList<PostData>> = MutableLiveData()
    var lastPostId=0L
    var limit = 20

    init {
        loadPosts()
    }
    fun loadPosts(){
        if(lastPostId!=-1L){
            CoroutineScope(Dispatchers.IO).launch {
                loading.postValue(true)
                val res = postRepository.getMyPosts(limit, lastPostId)
                if(res.isNotEmpty()){
                    posts.postValue(res as ArrayList<PostData>?)
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
            }
        }
    }
}