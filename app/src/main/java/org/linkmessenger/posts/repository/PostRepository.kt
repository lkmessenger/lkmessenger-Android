package org.linkmessenger.posts.repository


import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.request.models.*
import java.io.File
import java.lang.reflect.Type

interface PostRepository {
    suspend fun addPost(params:AddPostParams):SocialResponse<AddPostResponse>
    fun addLog(params:AddLogParams)
    fun uploadMedia(url:String, avatar:ByteArray):String?
    fun uploadMedia(url:String, file: File):String?
    fun uploadVideo(url: String, file: File): String?
    suspend fun setPost(params:SetPostParams):SocialResponse<DefaultResponse>
    suspend fun addPostView(params:AddPostViewParams):SocialResponse<DefaultResponse>
    suspend fun getPost(postId:Long, forceOnline: Boolean):Post
    suspend fun deletePost(postId:Long)
    suspend fun getMyPosts(limit:Int, id:Long, forceOnline:Boolean):List<Post>
    suspend fun getMyPosts(limit:Int, id:Long):List<PostData>
    suspend fun getMyPostsToId(postId: Long):List<Post>
    suspend fun getUserPostsToId(userId:Int, postId: Long):List<Post>
    suspend fun getSubscriptionPosts(limit:Int, id:Long, forceOnline:Boolean):List<Post>
    suspend fun getRecommendationPosts(limit:Int, id:Long, forceOnline:Boolean):List<Post>
    suspend fun getUserPosts(userId:Int, limit:Int, id:Long, forceOnline:Boolean):List<Post>
    suspend fun likePost(postId:Long)
    suspend fun unLikePost(postId:Long)
    suspend fun resetPosts(type:Type)
    suspend fun resetPosts(userId:Int, type:Type)
    suspend fun addPostToCollections(postId: Long)
    suspend fun deletePostFromCollection(postId: Long)
    suspend fun getCollections(page: Int, limit: Int):SocialResponse<PostsResponse>
    suspend fun addComment(postId: Long, comment: String):SocialResponse<AddCommentResponse>
    suspend fun addCommentReply(postId: Long, commentId: Long, comment: String, username: String):SocialResponse<AddCommentResponse>
    suspend fun getComments(postId: Long, limit: Int, lastPost: Long): SocialResponse<CommentResponse>
    suspend fun getCommentReplies(postId: Long, commentId: Long, limit: Int, lastPost: Long): SocialResponse<CommentRepliesResponse>
    suspend fun deleteComment(commentId: Long): SocialResponse<DefaultResponse>
    suspend fun getNotificationsCount():SocialResponse<NotificationCountResponse>
    suspend fun deleteNotificationCount():SocialResponse<DefaultResponse>
    suspend fun getPostLikes(postId: Long, limit: Int, page: Int):SocialResponse<UsersResponse>
}