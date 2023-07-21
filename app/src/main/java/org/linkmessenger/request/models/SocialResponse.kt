package org.linkmessenger.request.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.updateinfo.models.UpdateInfo
import org.thoughtcrime.securesms.components.sensors.Orientation
import java.util.Date

data class SocialResponse<T>(
    @SerializedName("error")
    val error:ErrorData?,
    @SerializedName("data")
    val data:T?,
    @SerializedName("success")
    val success:Boolean
    )
data class ErrorData(
    @SerializedName("message")
    val message:String?,
    @SerializedName("code")
    val code:Int?
    )
data class ChangeProfileResponse(
    @SerializedName("uploadUrl")
    val uploadUrl:String?,
    @SerializedName("filename")
    val filename:String?
)
data class DefaultResponse(
    @SerializedName("token")
    val token:String?
    )
data class UsernameResponse(
    @SerializedName("username")
    val username:String?
    )
data class AddPostResponse(
    @SerializedName("post_id")
    val post_id:Long,
//    val urls:ArrayList<AddPostUrl>?,
    @SerializedName("media")
    val media:AddPostMediaResponse?
)
data class AddPostMediaResponse(
    @SerializedName("video")
    val video:VideoUploadUrl?,
    @SerializedName("photos")
    val photos:List<PhotoUploadUrl>?
)
data class PhotoUploadUrl(
    @SerializedName("orientation")
    val orientation: Int,
    @SerializedName("position")
    val position: Int,
    @SerializedName("filename")
    val filename:String,
    @SerializedName("Urls")
    val Urls:List<PhotoUrl>
)
data class VideoUploadUrl(
    @SerializedName("orientation")
    val orientation: Int,
    @SerializedName("playlist")
    val playlist:VideoUrl,
    @SerializedName("preload_photo")
    val preload_photo:VideoUrl,
    @SerializedName("upload_urls")
    val upload_urls:List<VideoUrl>
)
data class VideoUrl(
    @SerializedName("filename")
    val filename:String,
    @SerializedName("pre_signed_url")
    val pre_signed_url: String
)
data class PhotoUrl(
    @SerializedName("size")
    val size:PhotoSize,
    @SerializedName("filename")
    val filename:String,
    @SerializedName("pre_signed_url")
    val pre_signed_url:String
)
data class PhotoSize(
    @SerializedName("width")
    val width:Int,
    @SerializedName("height")
    val height:Int,
)
//data class AddPostMediaSize(
//    val height:Int,
//    val width:Int,
//    val filename:String,
//    val pre_signed_url:String,
//)
data class UsersResponse(
    @SerializedName("users")
    val users:ArrayList<ProfileData>?
)

data class MessageRequestsResponse(
        @SerializedName("items")
        val items:ArrayList<MessageRequest>?
)

data class SentMessageRequestsResponse(
    @SerializedName("items")
    val items:ArrayList<SentMessageRequest>?
)

data class MessageRequest(
        val id:Long,
        val user: ProfileData?
)

data class SentMessageRequest(
    val id:Long,
    val status: Int,
    val user: ProfileData?
)

data class PostsResponse(
    @SerializedName("posts")
    var posts:ArrayList<PostData>?
)
data class PostResponse(
    @SerializedName("post")
    var post:PostData?
)
data class CommentResponse(
    @SerializedName("Comments")
        var Comments: ArrayList<Comment?>
)
data class CommentRepliesResponse(
    @SerializedName("Replies")
        var Replies: ArrayList<CommentReply>
)
data class AddCommentResponse(
    @SerializedName("id")
        var id: Long
)

data class NotificationResponse(
    @SerializedName("follow")
        var follow: Boolean,
    @SerializedName("comment")
        var comment: Boolean,
    @SerializedName("reaction")
        var reaction: Boolean,
)

@Parcelize
data class PostData(
    @SerializedName("id")
    val id:Long,
    @SerializedName("user")
    val user:ProfileData?,
    @SerializedName("description")
    val description:String?,
    @SerializedName("hashtags")
    val hashtags:String?,
    @SerializedName("medias")
    val medias:ArrayList<MediaData>?,
    @SerializedName("create_at")
    val create_at:Date?,
    @SerializedName("modified_at")
    val modified_at:Date?,
    @SerializedName("likes_count")
    val likes_count:Int,
    @SerializedName("comments_count")
    val comments_count:Int,
    @SerializedName("collections_count")
    val collections_count:Int,
    @SerializedName("isMy")
    var isMy:Boolean = false,
    @SerializedName("is_self")
    var isSelf:Boolean = false,
    @SerializedName("is_liked")
    var is_liked:Boolean = false,
    @SerializedName("view_count")
    var view_count:Long=0,
    @SerializedName("is_collected")
    var is_collected:Boolean = false,
    @SerializedName("allow_download")
    var allow_download:Int = 1,
    @SerializedName("comment_available")
    var comment_available:Int = 1
) : Parcelable

@Parcelize
data class MediaData(
    @SerializedName("id")
    val id:Long,
    @SerializedName("position")
    val position:Int,
    @SerializedName("type")
    val type:Int,
    @SerializedName("url")
    val url:String,
    @SerializedName("orientation")
    val orientation:Int,
    @SerializedName("preload_photo")
    var preload_photo:String?
) : Parcelable

@Parcelize
data class Comment(
    @SerializedName("id")
        val id:Long,
    @SerializedName("user")
        val user:ProfileData?,
    @SerializedName("post_id")
        val post_id: Long,
    @SerializedName("comment")
        val comment: String?,
    @SerializedName("replies_count")
        val replies_count: Int,
    @SerializedName("replies")
        val replies: ArrayList<CommentReply>,
    @SerializedName("created_at")
        val created_at: String,
    @SerializedName("is_self")
    val isSelf: Boolean,
) : Parcelable

data class PrivacySettingsResponse(
        @SerializedName("user_id")
        val userId: Int,
        @SerializedName("phone_visibility")
        val phoneVisibility: Boolean,
        @SerializedName("messenger_access")
        val messengerAccess: Boolean
)

data class NotificationsResponse(
    val notifications: ArrayList<NotificationHistory>
)

data class NotificationHistory (
        val id: Int,
        val type: String,
        @SerializedName("user_id")
        val userId: Int,
        val user: NotificationUser,
        val reaction: Reaction?,
        val comment: NotificationComment?,
        @SerializedName("created_at")
        val createdAt: String
)

data class Reaction (
        @SerializedName("post_id")
        val postId: Int,
)

data class NotificationComment(
        @SerializedName("post_id")
        val postId: Int,
        @SerializedName("comment_id")
        val commentId: Int,
        @SerializedName("comment_reply_id")
        val commentReplyId: Int,
        val comment: String
)

data class NotificationUser (
        val ID: Int,
        val ProfileName: String?,
        val Username: String?,
        val Phone: String?,
        val Avatar: String?,
        val Description: String,
        val Status: Int,
        val IsVerified: Int,
        val IsCurrentUser: Boolean,
        val notification_token: NotificationToken?,
        val CreatedAt: String
)

data class NotificationToken (
        val gcm: String,
        val apn: String
)

data class NotificationData(
        val id: String?,
        val avatar: String?,
        val userId:String?,
        val postId: String?,
        val username: String?,
        val type: String?,
        val profileName: String?,
        val comment: String?,
        val commentId: String?
)

@Parcelize
data class NotificationCount(
    val follow: String?,
    val reaction: String?,
    val comment: String?
): Parcelable

@Parcelize
data class NotificationCountResponse(
    val notifications: NotificationCount
):Parcelable

@Parcelize
data class CommentReply(
    @SerializedName("id")
        val id:Long,
    @SerializedName("user")
        val user:ProfileData?,
    @SerializedName("post_id")
        val post_id: Long,
    @SerializedName("parent_id")
        val parent_id: Long,
    @SerializedName("comment")
        val comment: String?,
    @SerializedName("created_at")
        val created_at: String,
    @SerializedName("username")
        val username: String,
    @SerializedName("is_self")
    val isSelf: Boolean,
) : Parcelable

@Parcelize
data class VersionResponse(
    @SerializedName("app_version")
    val appVersion: Int,
    val data: ArrayList<UpdateInfo>
) : Parcelable

fun MediaData.getMediumPhoto():String{
    return "${this.url}?w=720&h=720"
}
fun MediaData.getSmallPhoto():String{
    return "${this.url}?w=480&h=480"
}
fun MediaData.getSmallPhoto(or:String):String{
    return when (or) {
        "l.jpeg" -> {
            "${this.url}?w=270&h=480"
        }
        "p.jpeg" -> {
            "${this.url}?w=600&h=480"
        }
        else -> {
            "${this.url}?w=480&h=480"
        }
    }
}