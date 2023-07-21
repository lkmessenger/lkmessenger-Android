package org.linkmessenger.profile.repository

import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.request.models.*

interface ProfileRepository {
    fun uploadProfile(params:ChangeProfileParams, avatar: ByteArray?)
    fun updateProfile(params:ChangeProfileParams, avatar: ByteArray?)
    fun updateToken(token:String?)
    suspend fun changeProfile(params:ChangeProfileParams):SocialResponse<ChangeProfileResponse>
    suspend fun uploadAvatar(url:String, avatar:ByteArray):Boolean
    suspend fun deleteAvatar()
    fun updateAvatar(avatar:ByteArray?)
    suspend fun setAvatar(fileName:String):SocialResponse<DefaultResponse>
    suspend fun requestToSendMessage(userId:Long):SocialResponse<DefaultResponse>
    suspend fun allowMessage(id:Long):SocialResponse<DefaultResponse>
    suspend fun denyMessage(id:Long):SocialResponse<DefaultResponse>
    suspend fun subscribe(params: SubscribeParams):SocialResponse<DefaultResponse>
    suspend fun unSubscribe(params: SubscribeParams):SocialResponse<DefaultResponse>
    suspend fun deleteFollower(params: SubscribeParams):SocialResponse<DefaultResponse>
    suspend fun getMyProfile(refresh:Boolean):ProfileData
    suspend fun getAntherProfile(id:Int):SocialResponse<ProfileData>
    suspend fun getAntherProfileByUsername(username: String): SocialResponse<ProfileData>
    suspend fun getUsername():SocialResponse<UsernameResponse>
    fun getUsernameAsync(callback:(username:String?)->Void?)
    fun setUsername(username:String?)
    suspend fun getMySubscriptions(limit:Int, lastId: Int):SocialResponse<UsersResponse>
    suspend fun getMyFollowers(limit:Int, lastId: Int):SocialResponse<UsersResponse>
    suspend fun getUserFollowers(userId:Int, limit:Int, lastId: Int):SocialResponse<UsersResponse>
    suspend fun getUserSubscriptions(userId:Int, limit:Int, lastId: Int):SocialResponse<UsersResponse>
    suspend fun getRecommendationUsers(userId: Int?): SocialResponse<UsersResponse>
    suspend fun getPrivacySettings(): SocialResponse<PrivacySettingsResponse>
    suspend fun updatePrivacySettings(params: PrivacySettingsParams): SocialResponse<DefaultResponse>
    suspend fun getNotificationsHistory(limit:Int, lastId: Int, type:String): SocialResponse<NotificationsResponse>
    suspend fun getMessageRequests(limit: Int, id: Long): SocialResponse<MessageRequestsResponse>
    suspend fun getUserByUuid(uuid:String): SocialResponse<ProfileData>
    suspend fun getVersion(lang :String):VersionResponse?
    suspend fun getSentChatRequest(limit: Int, id: Long): SocialResponse<SentMessageRequestsResponse>
    suspend fun getTrendings(params: TrendingParams): SocialResponse<ArrayList<ProfileData>>
    fun createGroup(id: String, name:String, url: String)
    suspend fun blockUser(params: SubscribeParams): SocialResponse<DefaultResponse>
    suspend fun unblockUser(params: SubscribeParams): SocialResponse<DefaultResponse>
    suspend fun getBlockedUsers(): SocialResponse<ArrayList<ProfileData>>
    suspend fun changePostNotificationSettings(params: NotificationSettingsParams): SocialResponse<DefaultResponse>
}