package org.linkmessenger.posts.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.realm.DynamicRealm.Transaction.OnSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.base.BaseViewModel
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.posts.repository.PostRepository
import org.linkmessenger.request.models.Comment
import org.linkmessenger.request.models.CommentReply

class SinglePostViewModel(private val postRepository: PostRepository):BaseViewModel() {
    val post:MutableLiveData<Post> = MutableLiveData()
    val comments: MutableLiveData<ArrayList<Comment?>> = MutableLiveData()
    val commentsReplies: MutableLiveData<ArrayList<CommentReply>> = MutableLiveData()

    var limit = 10
    var lastCommentId=0L
    var commentReplyLimit = 5
    var lastCommentReplyId = 0L
    var isFirstLoading = true
    var isLoadingComment = false
    val replyLoading:MutableLiveData<Boolean> = MutableLiveData()

    init {
        lastCommentId = 0L
        lastCommentReplyId = 0
        isFirstLoading = true
    }

    fun loadPost(postId:Long, forceOnline:Boolean=false){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = postRepository.getPost(postId, forceOnline)
                post.postValue(res)
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }

    fun addComment(postId: Long, comment: String, onSuccess: (id: Long, addedComment: String) -> Unit){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val res = postRepository.addComment(postId, comment)
                if(res.success){
                    CoroutineScope(Dispatchers.Main).launch{
                        onSuccess.invoke(res.data?.id!!, comment)
                    }
                }else{
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }

    fun addCommentReply(postId: Long, commentId: Long, comment: String, username: String, onSuccess: (id: Long, addedComment: String, username: String, commentId: Long) -> Unit){
        CoroutineScope(Dispatchers.IO).launch{
            try {
                val res = postRepository.addCommentReply(postId, commentId, comment, username)

                if(res.success){
                    CoroutineScope(Dispatchers.Main).launch{
                        onSuccess.invoke(res.data?.id!!, comment, username, commentId)
                    }
                }else{
                    throw java.lang.Exception(res.error?.message)
                }
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }
        }
    }

    fun loadComments(postId:Long){
        if(lastCommentId != -1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    loading.postValue(true)
                    val res = postRepository.getComments(postId, limit, lastCommentId)

                    if(res.data?.Comments?.isNotEmpty() == true){
                        comments.postValue(res.data.Comments)
                    }
                    lastCommentId = if(res.data?.Comments?.isNotEmpty() == true){
                        if(res.data.Comments.size < limit){
                            -1
                        }else{
                            res.data.Comments.maxByOrNull { p -> p!!.id }?.id ?:0
                        }
                    }else{
                        -1
                    }
                }catch (e:Exception){
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

    fun loadCommentReplies(postId:Long, commentId: Long){
        if(lastCommentReplyId != -1L){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    replyLoading.postValue(true)
                    val res = postRepository.getCommentReplies(postId, commentId, commentReplyLimit, lastCommentReplyId)

                    if(res.data?.Replies?.isNotEmpty() == true){
                        if(isFirstLoading){
                            res.data.Replies.removeFirst()
                            commentsReplies.postValue(res.data.Replies)
                        }else{
                            commentsReplies.postValue(res.data.Replies)
                        }
                    }
                    lastCommentReplyId = if(res.data?.Replies?.isNotEmpty() == true){
                        if(res.data.Replies.size < commentReplyLimit && !isFirstLoading){
                            -1
                        }else{
                            res.data.Replies.maxByOrNull { p -> p.id }?.id ?: 0
                        }
                    }else{
                        -1
                    }

                    isFirstLoading = false
                }catch (e:Exception){
                    error.postValue(e)
                }finally {
                    replyLoading.postValue(false)
                }
            }
        }else{
            replyLoading.postValue(false)
        }
    }

    fun deleteComment(commentId: Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                postRepository.deleteComment(commentId)
            }catch (e: Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
                error.postValue(e)
            }finally {

            }
        }
    }
}