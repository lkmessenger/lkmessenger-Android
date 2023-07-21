package org.linkmessenger.posts.repository

import com.google.gson.Gson
import io.realm.RealmModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.linkmessenger.data.local.dao.PostsDao
import org.linkmessenger.data.local.entity.*
import org.linkmessenger.data.local.entity.mappers.PostDataToPostMapper
import org.linkmessenger.request.StorageClient
import org.linkmessenger.request.api.SocialApi
import org.linkmessenger.request.models.*
import org.signal.glide.Log
import java.io.File
import java.lang.reflect.Type

class PostRepositoryImpl(private val socialApi: SocialApi, private val storageClient: StorageClient,
                         private val postsDao: PostsDao
): PostRepository {
    private val TAG:String = this.javaClass.toString()
    override suspend fun addPost(params: AddPostParams): SocialResponse<AddPostResponse> {
        return socialApi.addPost(params)
    }

    override fun addLog(params: AddLogParams) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val res = socialApi.addLog(params)
                Log.d("bbb", res.success.toString())
            }catch (e:Exception){
                Log.d("bbb", e.stackTraceToString())
            }
        }
    }

    override fun uploadVideo(url: String, file: File): String? {
        val requestBody: RequestBody = RequestBody.create(null, file)
        val request: Request = Request.Builder()
            .url(url)
            .method("PUT", requestBody)
            .addHeader("Content-type", "application/octet-stream")
            .build()
        val response = storageClient.client.newCall(request).execute()
        if(!response.isSuccessful){
            return response.message()
        }
        return null
    }
    override fun uploadMedia(url: String, file: File): String? {
        val requestBody: RequestBody = RequestBody.create(MediaType.get("image/jpeg"), file)
        val request: Request = Request.Builder()
            .url(url)
            .method("PUT", requestBody)
            .addHeader("Content-type", "application/octet-stream")
            .build()
        val response = storageClient.client.newCall(request).execute()
        if(!response.isSuccessful){
            return response.message()
        }
        return null
    }
    override fun uploadMedia(url: String, avatar: ByteArray): String? {
        val requestBody: RequestBody = RequestBody.create(MediaType.get("image/jpeg"), avatar)
        val request: Request = Request.Builder()
            .url(url)
            .method("PUT", requestBody)
            .addHeader("Content-type", "application/octet-stream")
            .build()
        val response = storageClient.client.newCall(request).execute()
        if(!response.isSuccessful){
            return response.message()
        }
        return null
    }

    override suspend fun setPost(params: SetPostParams): SocialResponse<DefaultResponse> {
        return socialApi.setPost(params)
    }

    override suspend fun addPostView(params: AddPostViewParams): SocialResponse<DefaultResponse> {
        return socialApi.addPostView(params)
    }

    override suspend fun getPost(postId: Long, forceOnline: Boolean): Post {
        if(forceOnline){
            val response = socialApi.getPost(postId)
            if(!response.success){
                throw FromServerException(response.error?.message)
            }
            if(response.data==null){
                throw FromServerException("data is null")
            }
            val postDb = PostDataToPostMapper(true, SharePost::class.java).transform(response.data)

            val saved = postsDao.savePost(postDb as SharePost)
            if(!saved){
                throw FromDbException()
            }
            return postDb
        }else{
            var postDb:SharePost? = postsDao.getPost(postId)
            if(postDb==null){
                val response = socialApi.getPost(postId)
                if(!response.success){
                    throw FromServerException(response.error?.message)
                }
                if(response.data==null){
                    throw FromServerException("data is null")
                }
                postDb = PostDataToPostMapper(true, SharePost::class.java).transform(response.data) as SharePost

                val saved = postsDao.savePost(postDb)
                if(!saved){
                    throw FromDbException()
                }
                return postDb
            }else{
                return postDb
            }
        }
    }

    override suspend fun deletePost(postId: Long) {
        val response = socialApi.deletePost(postId)
        if(!response.success) throw FromServerException(response.error?.message)
        postsDao.deletePost(postId)
    }

    override suspend fun likePost(postId:Long){
        val res = socialApi.like(LikeParams(postId))
        if(!res.success){
            throw FromServerException(res.error?.message)
        }
        postsDao.updateLikePost(postId, true)
    }

    override suspend fun unLikePost(postId: Long) {
        val res = socialApi.unLike(LikeParams(postId))
        if(!res.success){
            throw FromServerException(res.error?.message)
        }
        postsDao.updateLikePost(postId, false)
    }

    override suspend fun resetPosts(type: Type) {
        postsDao.resetPosts(type)
    }

    override suspend fun resetPosts(userId: Int, type: Type) {
        postsDao.resetPosts(userId, type)
    }

    override suspend fun addPostToCollections(postId: Long) {
        val res = socialApi.addPostToCollections(CollectionParams(postId))
        if(!res.success){
            throw FromServerException(res.error?.message)
        }
    }

    override suspend fun deletePostFromCollection(postId: Long) {
        val res = socialApi.deletePostFromCollection(postId)
        if (!res.success){
            throw FromServerException(res.error?.message)
        }
    }

    override suspend fun getCollections(page: Int, limit: Int): SocialResponse<PostsResponse> {
        return socialApi.getCollections(limit, page)
    }

    override suspend fun addComment(postId: Long, comment: String): SocialResponse<AddCommentResponse> {
        val res = socialApi.addComment(CommentParams(postId, comment))
        if(!res.success){
            throw FromServerException(res.error?.message)
        }

        return res
    }

    override suspend fun addCommentReply(postId: Long, commentId: Long, comment: String, username: String): SocialResponse<AddCommentResponse> {
        val res = socialApi.addCommentReply(CommentReplyParams(postId, commentId,comment, username))
        if(!res.success){
            throw FromServerException(res.error?.message)
        }

        return res
    }

    override suspend fun getComments(postId: Long, limit: Int, lastComment: Long): SocialResponse<CommentResponse> {
        return socialApi.getComments(postId, limit, lastComment)
    }

    override suspend fun getCommentReplies(postId: Long, commentId: Long, limit: Int, lastPost: Long): SocialResponse<CommentRepliesResponse> {
        return socialApi.getCommentReplies(postId, commentId, limit, lastPost)
    }

    override suspend fun deleteComment(commentId: Long): SocialResponse<DefaultResponse> {
        return socialApi.deleteComment(commentId)
    }
    override suspend fun getMyPosts(limit:Int, id:Long): List<PostData> {
        val response = socialApi.getMyPosts(limit, id)
        if(!response.success){
            throw FromServerException(response.error?.message)
        }
        if(response.data==null){
            throw FromServerException("data is null")
        }
        return response.data.posts!!
    }
    override suspend fun getMyPosts(limit:Int, id:Long, forceOnline:Boolean): List<Post> {
        if(forceOnline){
            val response = socialApi.getMyPosts(limit, id)
            if(!response.success){
                throw FromServerException(response.error?.message)
            }
            if(response.data==null){
                throw FromServerException("data is null")
            }
            val postsDb =if(response.data.posts==null){
                listOf()
            }else{
                response.data.posts!!.map {p-> PostDataToPostMapper(true, MyPost::class.java).transform(p) }
            }
            val saved = postsDao.saveMyPosts(postsDb as List<MyPost>)
            if(!saved){
                throw FromDbException()
            }
            return postsDb
        }else{
            var postsDb:List<Post> = postsDao.getMyPosts(id)
            if(postsDb.size<limit){
                val response = socialApi.getMyPosts(limit, id)
                if(!response.success){
                    throw FromServerException(response.error?.message)
                }
                if(response.data==null){
                    throw FromServerException("data is null")
                }
                postsDb =if(response.data.posts==null){
                    listOf()
                }else{
                    response.data.posts!!.map {p-> PostDataToPostMapper(true, MyPost::class.java).transform(p) }
                }
                val saved = postsDao.saveMyPosts(postsDb as List<MyPost>)
                if(!saved){
                    throw FromDbException()
                }
                return postsDb
            }
            return postsDb
        }
    }


    override suspend fun getSubscriptionPosts(
        limit: Int,
        id: Long,
        forceOnline: Boolean
    ): List<Post>{
        if(forceOnline){
            val response = socialApi.getSubscriptionPosts(limit, id)
            if(!response.success){
                throw FromServerException(response.error?.message)
            }
            if(response.data==null){
                throw FromServerException("data is null")
            }
            val postsDb =if(response.data.posts==null){
                listOf()
            }else{
                response.data.posts!!.map {p-> PostDataToPostMapper(true, SubsPost::class.java).transform(p) }
            }
            val saved = postsDao.saveSubscriptionPost(postsDb as List<SubsPost>)
            if(!saved){
                throw FromDbException()
            }
            return postsDb
        }else{
            var postsDb:List<Post> = postsDao.getSubscriptionPost(id)
            if(postsDb.size<limit){
                val response = socialApi.getSubscriptionPosts(limit, id)
                if(!response.success){
                    throw FromServerException(response.error?.message)
                }
                if(response.data==null){
                    throw FromServerException("data is null")
                }
                postsDb =if(response.data.posts==null){
                    listOf()
                }else{
                    response.data.posts!!.map {p-> PostDataToPostMapper(true, SubsPost::class.java).transform(p) }
                }
                val saved = postsDao.saveSubscriptionPost(postsDb as List<SubsPost>)
                if(!saved){
                    throw FromDbException()
                }
                return postsDb
            }
            return postsDb
        }
    }

    override suspend fun getRecommendationPosts(
        limit: Int,
        id: Long,
        forceOnline: Boolean
    ): List<Post> {
        if(forceOnline){
            val response = socialApi.getRecommendationPosts(limit, id)
            if(!response.success){
                throw FromServerException(response.error?.message)
            }
            if(response.data==null){
                throw FromServerException("data is null")
            }
            val postsDb =if(response.data.posts==null){
                listOf()
            }else{
                response.data.posts!!.map {p-> PostDataToPostMapper(true, SharePost::class.java).transform(p) }
            }
            val saved = postsDao.saveRecommendationPost(postsDb as List<SharePost>)
            if(!saved){
                throw FromDbException()
            }
            return postsDb
        }else{
            var postsDb:List<Post> = postsDao.getRecommendationPost(id)
            if(postsDb.size<limit){
                val tmp = if(postsDb.isNotEmpty()){
                    postsDb[0]
                }else{
                    null
                }
                val response = socialApi.getRecommendationPosts(limit, id)
                if(!response.success){
                    throw FromServerException(response.error?.message)
                }
                if(response.data==null){
                    throw FromServerException("data is null")
                }
                postsDb =if(response.data.posts==null){
                    listOf()
                }else{
                    response.data.posts!!.map {p-> PostDataToPostMapper(true, SharePost::class.java).transform(p) }
                }
                if(postsDb.isNotEmpty() && tmp!=null){
                    val list = postsDb.toMutableList()
                    list.add(0, tmp)
                    postsDb = list
                }
                val saved = postsDao.saveRecommendationPost(postsDb as List<SharePost>)
                if(!saved){
                    throw FromDbException()
                }
                return postsDb
            }
            return postsDb
        }
    }

    override suspend fun getUserPosts(
        userId: Int,
        limit: Int,
        id: Long,
        forceOnline: Boolean
    ): List<Post> {
        if(forceOnline){
            val response = socialApi.getUserPosts(userId, limit, id)
            if(!response.success){
                throw FromServerException(response.error?.message)
            }
            if(response.data==null){
                throw FromServerException("data is null")
            }
            val postsDb =if(response.data.posts==null){
                listOf()
            }else{
                response.data.posts!!.map {p-> PostDataToPostMapper(true, UserPost::class.java).transform(p) }
            }
            val saved = postsDao.saveUserPosts(postsDb as List<UserPost>)
            if(!saved){
                throw FromDbException()
            }
            return postsDb
        }else{
            var postsDb:List<Post> = postsDao.getUserPosts(userId, id)
            if(postsDb.size<limit){
                val response = socialApi.getUserPosts(userId, limit, id)
                if(!response.success){
                    throw FromServerException(response.error?.message)
                }
                if(response.data==null){
                    throw FromServerException("data is null")
                }
                postsDb =if(response.data.posts==null){
                    listOf()
                }else{
                    response.data.posts!!.map {p-> PostDataToPostMapper(true, UserPost::class.java).transform(p) }
                }
                val saved = postsDao.saveUserPosts(postsDb as List<UserPost>)
                if(!saved){
                    throw FromDbException()
                }
                return postsDb
            }
            return postsDb
        }
    }


    override suspend fun getMyPostsToId(postId: Long): List<Post> {
        return postsDao.getMyPostsToId(postId)
    }

    override suspend fun getUserPostsToId(userId: Int, postId: Long): List<Post> {
        return postsDao.getUserPostsToId(userId, postId)
    }
    override suspend fun getNotificationsCount(): SocialResponse<NotificationCountResponse> {
        return socialApi.getNotificationCount()
    }
    override suspend fun deleteNotificationCount(): SocialResponse<DefaultResponse> {
        return socialApi.deleteNotificationCount()
    }

    override suspend fun getPostLikes(postId: Long, limit: Int, page: Int): SocialResponse<UsersResponse> {
        val params = GetPostLikesParams(postId, page, limit)
        return socialApi.getPostLikes(params)
    }

}