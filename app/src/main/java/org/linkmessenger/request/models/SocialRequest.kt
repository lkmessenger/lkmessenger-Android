package org.linkmessenger.request.models

import com.google.gson.annotations.SerializedName

class SocialRequest {
}
data class ChangeProfileParams(
    @SerializedName("profileName")
    var profileName:String?,
    @SerializedName("avatarExt")
    var avatarExt:String?,
    @SerializedName("description")
    var description: String?=null,
    )
data class AddLogParams(
    @SerializedName("key")
    var key:String,
    @SerializedName("value")
    var value:String,
)
data class ChangeTokenParams(
    @SerializedName("token")
    var token:String,
)
data class SetAvatarParams(
    @SerializedName("filename")
    var filename:String
    )
data class AddPostParams(
    @SerializedName("hashtags")
    var hashtags:ArrayList<String>?,
    @SerializedName("description")
    var description:String?,
    @SerializedName("media")
    var media:AddPostMedias,
    @SerializedName("comment_available")
    var commentAvailable: Boolean,
    @SerializedName("type")
    var type:Int,
    @SerializedName("allow_download")
    var allow_download:Boolean
)
data class AddPostMediaParams(
    @SerializedName("ext")
    var ext:String,
    @SerializedName("position")
    var position:Int,
    @SerializedName("type")
    var type:Int,
    @SerializedName("orientation")
    var orientation:Int,
    @SerializedName("sizes")
    var sizes:ArrayList<MediaSizeParams>
)
data class AddPostVideoParams(
    @SerializedName("orientation")
    var orientation:Int,
    @SerializedName("preload_photo_filename")
    var preloadPhotoFilename:String,
    @SerializedName("playlist_filename")
    var playlistFilename:String,
    @SerializedName("segment_filenames")
    var segmentFilenames:List<String>
)
data class AddPostMedias(
    @SerializedName("photos")
    val photos:ArrayList<AddPostMediaParams>?,
    @SerializedName("video")
    val video:AddPostVideoParams?
)
data class MediaSizeParams(
    @SerializedName("width")
    var width:Int,
    @SerializedName("height")
    var height:Int
)
data class SendRequestForMessageAnswerParams(
    @SerializedName("id")
    var id:Long,
)
data class SendRequestForMessageParams(
    @SerializedName("user_id")
    var user_id:Long,
)
data class AddPostViewParams(
    @SerializedName("post_id")
    var post_id:Long,
)
data class SetPostParams(
    @SerializedName("post_id")
    var post_id:Long,
    @SerializedName("type")
    var type:Int,
    @SerializedName("media")
    var media:SetPostMedia
)
data class SetPostMedia(
    @SerializedName("photos")
    val photos:ArrayList<SetMediaParams>?,
    @SerializedName("video")
    val video:SetVideoParam?
)
data class SetVideoParam(
    @SerializedName("playlist_filename")
    var playlist_filename:String,
    @SerializedName("preload_photo_filename")
    var preload_photo_filename:String,
    @SerializedName("orientation")
    var orientation:Int,
)
data class SetMediaParams(
    @SerializedName("filename")
    var filename:String,
    @SerializedName("type")
    var type:Int,
    @SerializedName("position")
    var position:Int,
    @SerializedName("orientation")
    var orientation:Int,
)
data class SubscribeParams(
    @SerializedName("user_id")
    var userId:Int
)
data class LikeParams(
    @SerializedName("post_id")
    var postId:Long
)

data class GetPostLikesParams(
    @SerializedName("post_id")
    var postId:Long,
    var page:Int,
    var limit: Int
)

data class TrendingParams(
    @SerializedName("time_interval")
    var time_interval:String,
    var page:Int,
    var limit: Int
)

data class CreateGroupParams(
    var id:String,
    var name: String,
    var url: String
)

data class CollectionParams(
        @SerializedName("post_id")
        var postId:Long
)

data class CommentParams(
        @SerializedName("post_id")
        var postId:Long,
        @SerializedName("comment")
        val comment: String
)
data class CommentReplyParams(
        @SerializedName("post_id")
        var postId:Long,
        @SerializedName("comment_id")
        val commentId: Long,
        @SerializedName("comment")
        val comment: String,
        @SerializedName("username")
        val username: String
)
data class NotificationSettingsParams(
    @SerializedName("comment")
        var comment:Boolean?,
    @SerializedName("reaction")
        var reaction:Boolean?,
    @SerializedName("follow")
        var follow:Boolean?
)
data class ReportPostParams(
        @SerializedName("post_id")
        var postId: Long,
        @SerializedName("type_id")
        var typeId: Int,
)

data class PrivacySettingsParams(
        @SerializedName("phone_visibility")
        val phoneVisibility: Boolean?,
        @SerializedName("messenger_access")
        val messengerAccess: Boolean?,
)