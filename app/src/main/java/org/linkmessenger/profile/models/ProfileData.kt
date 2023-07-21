package org.linkmessenger.profile.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "profile")
data class ProfileData(
    @SerializedName("id")
    @ColumnInfo(name = "id")
    @PrimaryKey
    val id:Int,
    @SerializedName("phone")
    @ColumnInfo(name = "phone")
    val phone:String?,
    @SerializedName("profileName")
    @ColumnInfo(name = "profileName")
    val profileName:String?,
    @SerializedName("username")
    @ColumnInfo(name = "username")
    val username:String?,
    @SerializedName("description")
    @ColumnInfo(name = "description")
    val description:String?,
    @SerializedName("avatar")
    @ColumnInfo(name = "avatar")
    val avatar:String?,
    @SerializedName("isVerified")
    @ColumnInfo(name = "isVerified")
    val isVerified:Boolean=false,
    @SerializedName("isSubscribed")
    @ColumnInfo(name = "isSubscribed")
    var isSubscribed:Boolean?,
    @SerializedName("isFollower")
    @ColumnInfo(name = "isFollower")
    var isFollower:Boolean?,
    @SerializedName("posts_count")
    @ColumnInfo(name = "postsCount")
    val postsCount:Int=0,
    @SerializedName("followers_count")
    @ColumnInfo(name = "followersCount")
    var followersCount:Int=0,
    @SerializedName("subscriptions_count")
    @ColumnInfo(name = "subscriptionsCount")
    var subscriptionsCount:Int=0,
    @SerializedName("is_self")
    @ColumnInfo(name = "isSelf")
    val isSelf:Boolean=false,
    @ColumnInfo(name = "cacheDate")
    var cacheDate:Long=0,
    @ColumnInfo(name = "isChatRequested")
    var isChatRequested:Boolean=false,
    @ColumnInfo(name = "messageRequestId")
    var messageRequestId:Long=-1,
    @ColumnInfo(name = "ChatRequestsCount")
    var ChatRequestsCount:Int=0,
    @ColumnInfo(name = "sentMessageRequestStatus")
    var messageRequestStatus:Int=-1,
    @SerializedName("type")
    @ColumnInfo(name = "type")
    var type:Int=0,
) : Parcelable