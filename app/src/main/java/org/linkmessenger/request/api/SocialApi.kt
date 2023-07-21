package org.linkmessenger.request.api

import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.reports.models.ReportResponse
import org.linkmessenger.request.models.*
import org.linkmessenger.stickers.models.Sticker
import org.linkmessenger.stickers.models.StickersWithCategory
import retrofit2.http.*


interface SocialApi {
    @Headers("Content-Type: application/json")
    @POST("/api/profile/")
    suspend fun changeProfile(@Body params: ChangeProfileParams): SocialResponse<ChangeProfileResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/post/addLog")
    suspend fun addLog(@Body params: AddLogParams): SocialResponse<DefaultResponse>
    @Headers("Content-Type: application/json")
    @PUT("/api/profile/")
    suspend fun updateProfile(@Body params: ChangeProfileParams): SocialResponse<ChangeProfileResponse>
    @PUT("/api/profile/firebase/token")
    suspend fun updateToken(@Body params: ChangeTokenParams): SocialResponse<DefaultResponse>
    @Headers("Content-Type: application/json")
    @PUT("/api/profile/avatar")
    suspend fun setAvatar(@Body params: SetAvatarParams): SocialResponse<DefaultResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/chat_requests/add")
    suspend fun sendRequestForMessage(@Body params: SendRequestForMessageParams): SocialResponse<DefaultResponse>
    @GET("/api/chat_requests/list")
    suspend fun getChatRequestList(@Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Id") id:Long=0):SocialResponse<MessageRequestsResponse>
    @GET("/api/chat_requests/from_list")
    suspend fun getSentChatRequestList(@Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Id") id:Long=0):SocialResponse<SentMessageRequestsResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/chat_requests/accept")
    suspend fun allowMessage(@Body params: SendRequestForMessageAnswerParams): SocialResponse<DefaultResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/chat_requests/reject")
    suspend fun denyMessage(@Body params: SendRequestForMessageAnswerParams): SocialResponse<DefaultResponse>
    @DELETE("/api/profile/avatar")
    suspend fun deleteAvatar(): SocialResponse<DefaultResponse>
    @GET("/api/profile/avatar/generate")
    suspend fun generateUrlForAvatar(): SocialResponse<ChangeProfileResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/follower/subscribe")
    suspend fun subscribe(@Body params: SubscribeParams): SocialResponse<DefaultResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/follower/unsubscribe")
    suspend fun unSubscribe(@Body params: SubscribeParams): SocialResponse<DefaultResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/post/post")
    suspend fun addPost(@Body params: AddPostParams): SocialResponse<AddPostResponse>
    @POST("/api/follower/delete_follower")
    suspend fun deleteFollower(@Body params: SubscribeParams):SocialResponse<DefaultResponse>
    @Headers("Content-Type: application/json")
    @POST("/api/post/post/medias")
    suspend fun setPost(@Body params: SetPostParams): SocialResponse<DefaultResponse>

    @Headers("Content-Type: application/json")
    @POST("/api/analytics/addView")
    suspend fun addPostView(@Body params: AddPostViewParams): SocialResponse<DefaultResponse>
    @GET("/api/profile")
    suspend fun getMyProfile():SocialResponse<ProfileData>
    @GET("/api/user/user/{id}")
    suspend fun getAntherProfile(@Path("id") id:Int):SocialResponse<ProfileData>
    @GET("/api/user/user/username/{username}")
    suspend fun getAntherProfileByUsername(@Path("username") username:String):SocialResponse<ProfileData>
    @GET("/api/user/users")
    suspend fun searchUsersByUsername(@Query("search") q:String?, @Query("limit") limit:Int=20, @Query("page") page:Int=1):SocialResponse<UsersResponse>
    @GET("/api/profile/posts")
    suspend fun getMyPosts(@Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Id") id:Long=0):SocialResponse<PostsResponse>
    @GET("/api/user/user/{user_id}/posts")
    suspend fun getUserPosts(@Path("user_id") userId:Int, @Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Id") id:Long=0):SocialResponse<PostsResponse>
    @GET("/api/post/posts")
    suspend fun getSubscriptionPosts(@Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Id") id:Long=0):SocialResponse<PostsResponse>
    @GET("/api/post/post/{post_id}")
    suspend fun getPost(@Path("post_id") postId:Long):SocialResponse<PostData>
    @DELETE("/api/post/post/{post_id}")
    suspend fun deletePost(@Path("post_id") postId:Long):SocialResponse<DefaultResponse>
    @GET("/api/post/recommendations")
    suspend fun getRecommendationPosts(@Query("Pagination.Limit") limit:Int=12, @Query("Pagination.Id") id:Long=0):SocialResponse<PostsResponse>
    @POST("/api/reaction/")
    suspend fun like(@Body params: LikeParams): SocialResponse<DefaultResponse>
    @DELETE("/api/reaction/")
    suspend fun unLike(@Body params: LikeParams): SocialResponse<DefaultResponse>
    @GET("/api/profile/username")
    suspend fun getUsername(): SocialResponse<UsernameResponse>
    @GET("/api/profile/followers")
    suspend fun getFollowers(@Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Page") id:Int=0):SocialResponse<UsersResponse>
    @GET("/api/profile/subscriptions")
    suspend fun getSubscriptions(@Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Page") id:Int=0):SocialResponse<UsersResponse>
    @GET("/api/user/user/{user_id}/followers")
    suspend fun getUserFollowers(@Path("user_id") userId: Int, @Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Page") id:Int=0):SocialResponse<UsersResponse>
    @GET("/api/user/user/{user_id}/subscriptions")
    suspend fun getUserSubscriptions(@Path("user_id") userId: Int, @Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Page") id:Int=0):SocialResponse<UsersResponse>
    @GET("/api/profile/collections")
    suspend fun getCollections(@Query("Pagination.Limit") limit:Int=20, @Query("Pagination.Page") page:Int=0):SocialResponse<PostsResponse>
    @DELETE("/api/collection/")
    suspend fun deletePostFromCollection(@Query("post_id") postId: Long):SocialResponse<DefaultResponse>
    @POST("/api/collection/")
    suspend fun addPostToCollections(@Body params: CollectionParams):SocialResponse<DefaultResponse>
    @POST("/api/comments/comments")
    suspend fun addComment(@Body params: CommentParams):SocialResponse<AddCommentResponse>
    @POST("/api/comments/comments/reply")
    suspend fun addCommentReply(@Body params: CommentReplyParams):SocialResponse<AddCommentResponse>
    @GET("/api/post/post/{post_id}/comments")
    suspend fun getComments(@Path("post_id") postId: Long, @Query("Pagination.Limit") limit: Int = 20, @Query("Pagination.Id") id:Long=0):SocialResponse<CommentResponse>
    @GET("/api/post/post/{post_id}/comments/{comment_id}/replies")
    suspend fun getCommentReplies(@Path("post_id") postId: Long, @Path("comment_id") commentId: Long, @Query("Pagination.Limit") limit: Int = 20, @Query("Pagination.Id") id:Long=0):SocialResponse<CommentRepliesResponse>
    @DELETE("/api/comments/comments/{comment_id}")
    suspend fun deleteComment(@Path("comment_id") commentId: Long):SocialResponse<DefaultResponse>
    @PUT("/api/profile/settings/notifications")
    suspend fun updateNotificationSettings(@Body params: NotificationSettingsParams):SocialResponse<DefaultResponse>
    @GET("/api/profile/settings/notifications")
    suspend fun getNotificationSettings():SocialResponse<NotificationResponse>
    @GET("/api/user/user/recommendations")
    suspend fun getRecommendations(@Query("user_id") userId: Int?):SocialResponse<UsersResponse>

    @GET("/api/complaints/complaints/types/")
    suspend fun getReportTypes():SocialResponse<ReportResponse>
    @POST("/api/complaints/complaints")
    suspend fun reportPost(@Body params: ReportPostParams):SocialResponse<DefaultResponse>
    @GET("/api/profile/settings/privacy")
    suspend fun getPrivacySettings():SocialResponse<PrivacySettingsResponse>
    @PUT("/api/profile/settings/privacy")
    suspend fun updatePrivacySettings(@Body params: PrivacySettingsParams):SocialResponse<DefaultResponse>
    @GET("/api/profile/notifications")
    suspend fun getNotificationsHistory(@Query("Pagination.Limit") limit: Int = 20, @Query("Pagination.Id") id:Int=0, @Query("type") type:String="ALL"):SocialResponse<NotificationsResponse>
    @GET("/api/user/user/uuid/{uuid}")
    suspend fun getUserByUuid(@Path("uuid") uuid: String):SocialResponse<ProfileData>
    @GET("/public/versions/1/{lang}.json")
    suspend fun getVersion(@Path("lang") lang:String): VersionResponse?
    @GET("/api/profile/notifications_count")
    suspend fun getNotificationCount():SocialResponse<NotificationCountResponse>
    @DELETE("/api/profile/notifications_count")
    suspend fun deleteNotificationCount():SocialResponse<DefaultResponse>
    @POST("/api/post/activities/likes")
    suspend fun getPostLikes(@Body getLikeParams: GetPostLikesParams):SocialResponse<UsersResponse>
    @POST("/api/user/trending")
    suspend fun getTrending(@Body trendingParams: TrendingParams):SocialResponse<ArrayList<ProfileData>>
    @POST("/api/groups/groups/")
    suspend fun createGroup(@Body params: CreateGroupParams):SocialResponse<DefaultResponse>
    @POST("/api/user/block/user")
    suspend fun blockUser(@Body params: SubscribeParams):SocialResponse<DefaultResponse>
    @POST("/api/user/unblock/user")
    suspend fun unblockUser(@Body params: SubscribeParams):SocialResponse<DefaultResponse>
    @GET("/api/user/blocked_users")
    suspend fun getBlockedUsers():SocialResponse<ArrayList<ProfileData>>
    @GET("/api/stickers/")
    suspend fun getStickersWithCategory():SocialResponse<ArrayList<StickersWithCategory>>
    @GET("/api/stickers/category/{id}")
    suspend fun getStickersByCategory(@Path("id") id:Int = 0, @Query("Pagination.Limit") limit: Int = 20, @Query("Pagination.Page") page:Int=0,):SocialResponse<ArrayList<Sticker>>

}