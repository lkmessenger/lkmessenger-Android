package org.linkmessenger.profile.repository

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.util.Log
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.linkmessenger.data.local.dao.PostsDao
import org.linkmessenger.data.local.database.SocialDatabase
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.request.StorageClient
import org.linkmessenger.request.api.SocialApi
import org.linkmessenger.request.models.*
import org.linkmessenger.utils.PostUtil
import org.thoughtcrime.securesms.keyvalue.SignalStore
import java.util.Date

class ProfileRepositoryImpl(private val socialApi:SocialApi, private val storageClient: StorageClient,
                            private val socialDatabase: SocialDatabase, private val sharedPreferences: SharedPreferences
):ProfileRepository {
    private val TAG:String = this.javaClass.toString()

    @SuppressLint("LogNotSignal")
    @OptIn(DelicateCoroutinesApi::class)
    override fun uploadProfile(params: ChangeProfileParams, avatar: ByteArray?) {
        GlobalScope.launch(Dispatchers.IO) {
            if(avatar!=null){
                params.avatarExt = "jpeg"
            }
            try {
                val response = changeProfile(params)
                if(response.success){
                    if(response.data?.uploadUrl != null){
                        val res = avatar?.let { uploadAvatar(response.data.uploadUrl, it) }
                        if(res==true){
                            response.data.filename?.let { setAvatar(it) }
                        }
                    }
                    SignalStore.account().socialRegistred = true
                }
            }catch (e:retrofit2.HttpException){
                Log.e(TAG,e.message())
            }
        }
    }
    @SuppressLint("LogNotSignal")
    @OptIn(DelicateCoroutinesApi::class)
    override fun updateProfile(params: ChangeProfileParams, avatar: ByteArray?) {
        GlobalScope.launch(Dispatchers.IO) {
            if(avatar!=null){
                params.avatarExt = "jpeg"
            }
            try {
                val response = socialApi.updateProfile(params)
                if(response.success){
                    if(response.data?.uploadUrl != null){
                        val res = avatar?.let { uploadAvatar(response.data.uploadUrl, it) }
                        if(res==true){
                            response.data.filename?.let { setAvatar(it) }
                        }
                    }
                }
            }catch (e:retrofit2.HttpException){
                Log.e(TAG,e.message())
            }
        }
    }

    @SuppressLint("LogNotSignal")
    override fun updateToken(token: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = token?.let { ChangeTokenParams(it) }
                    ?.let { socialApi.updateToken(it) }
            }catch (e:Exception){
                e.message?.let { Log.e(TAG, it) }
            }
        }
    }

    override suspend fun changeProfile(params: ChangeProfileParams): SocialResponse<ChangeProfileResponse> {
        return socialApi.changeProfile(params)
    }

    @SuppressLint("LogNotSignal")
    override suspend fun uploadAvatar(url: String, avatar: ByteArray): Boolean {
        return try {
            val requestBody: RequestBody = RequestBody.create(MediaType.get("image/jpeg"), avatar)
            val request: Request = Request.Builder()
                .url(url)
                .method("PUT", requestBody)
                .addHeader("Content-type", "application/octet-stream")
                .build()
            val response = storageClient.client.newCall(request).execute()
            response.isSuccessful
        }catch (e:java.lang.Exception){
            Log.e(TAG, e.message.toString())
            false
        }
    }

    override suspend fun deleteAvatar() {
        socialApi.deleteAvatar()
    }

    @SuppressLint("LogNotSignal")
    override fun updateAvatar(avatar: ByteArray?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                deleteAvatar()
                if (avatar==null) this.cancel()
                val response = socialApi.generateUrlForAvatar()
                if(!response.success)throw FromServerException(response.error?.message)
                response.data?.uploadUrl?.let {
                    avatar?.let { it1 ->
                        val b = uploadAvatar(it, it1)
                        if(b){
                            response.data.filename?.let { it2 -> setAvatar(it2) }
                        }
                    }
                }
            }catch (e:Exception){
                Log.e(TAG, e.message.toString())
            }
        }
    }

    override suspend fun setAvatar(fileName: String): SocialResponse<DefaultResponse> {
        return socialApi.setAvatar(SetAvatarParams(fileName))
    }

    override suspend fun requestToSendMessage(userId: Long): SocialResponse<DefaultResponse> {
        return socialApi.sendRequestForMessage(SendRequestForMessageParams(userId))
    }

    override suspend fun allowMessage(id: Long): SocialResponse<DefaultResponse> {
        return socialApi.allowMessage(SendRequestForMessageAnswerParams(id))
    }

    override suspend fun denyMessage(id: Long): SocialResponse<DefaultResponse> {
        return socialApi.denyMessage(SendRequestForMessageAnswerParams(id))
    }

    override suspend fun subscribe(params: SubscribeParams): SocialResponse<DefaultResponse> {
        return socialApi.subscribe(params)
    }

    override suspend fun unSubscribe(params: SubscribeParams): SocialResponse<DefaultResponse> {
        return socialApi.unSubscribe(params)
    }

    override suspend fun deleteFollower(params: SubscribeParams): SocialResponse<DefaultResponse> {
        return socialApi.deleteFollower(params)
    }

    override suspend fun getMyProfile(refresh:Boolean): ProfileData {
        val dbProfile = socialDatabase.socialDao().getSelfProfile()
        return if(refresh || dbProfile==null || PostUtil.cacheInvalid(dbProfile.cacheDate)){
            val res = socialApi.getMyProfile()
            if(!res.success)throw FromServerException(res.error?.message)
            res.data!!.cacheDate = Date().time
            socialDatabase.socialDao().insertSelfProfile(res.data)
            res.data
        }else{
            dbProfile
        }
    }

    override suspend fun getAntherProfile(id:Int): SocialResponse<ProfileData> {
        return socialApi.getAntherProfile(id)
    }
    override suspend fun getAntherProfileByUsername(username: String): SocialResponse<ProfileData> {
        return socialApi.getAntherProfileByUsername(username)
    }

    override suspend fun getUsername(): SocialResponse<UsernameResponse> {
        return socialApi.getUsername()
    }

    override fun getUsernameAsync(callback: (username:String?) -> Void?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val un = sharedPreferences.getString("username", null)
                if(un.isNullOrEmpty()){
                    val res = socialApi.getUsername()
                    if(!res.success) throw Exception()
                    callback.invoke(res.data?.username)
                }else{
                    callback.invoke(un)
                }
            }catch (_:Exception){
                callback.invoke(null)
            }
        }
    }
    override fun setUsername(username:String?){
        sharedPreferences.edit().putString("username", username).apply()
    }

    override suspend fun getMySubscriptions(limit: Int, lastId: Int): SocialResponse<UsersResponse> {
        return socialApi.getSubscriptions(limit=20, lastId)
    }

    override suspend fun getMyFollowers(limit: Int, page: Int): SocialResponse<UsersResponse> {
      return  socialApi.getFollowers(limit=20, page)
    }

    override suspend fun getUserFollowers(userId: Int, limit: Int, lastId: Int): SocialResponse<UsersResponse> {
        return socialApi.getUserFollowers(userId, limit, lastId)
    }

    override suspend fun getUserSubscriptions(userId: Int, limit: Int, lastId: Int): SocialResponse<UsersResponse> {
        return socialApi.getUserSubscriptions(userId, limit, lastId)
    }

    override suspend fun getRecommendationUsers(userId: Int?): SocialResponse<UsersResponse> {
        return socialApi.getRecommendations(userId)
    }

    override suspend fun getPrivacySettings(): SocialResponse<PrivacySettingsResponse> {
        return socialApi.getPrivacySettings()
    }

    override suspend fun updatePrivacySettings(params: PrivacySettingsParams): SocialResponse<DefaultResponse> {
        return socialApi.updatePrivacySettings(params)
    }

    override suspend fun getNotificationsHistory(limit: Int, lastId: Int, type: String): SocialResponse<NotificationsResponse> {
        return socialApi.getNotificationsHistory(limit, lastId, type)
    }

    override suspend fun getMessageRequests(limit: Int, id: Long): SocialResponse<MessageRequestsResponse> {
        return socialApi.getChatRequestList(limit, id)
    }

    override suspend fun getUserByUuid(uuid: String): SocialResponse<ProfileData> {
        return socialApi.getUserByUuid(uuid)
    }
    override suspend fun getVersion(lang: String): VersionResponse? {
        return  socialApi.getVersion("en_EN")
    }

    override suspend fun getSentChatRequest(limit: Int, id: Long): SocialResponse<SentMessageRequestsResponse> {
        return socialApi.getSentChatRequestList(limit, id)
    }

    override suspend fun getTrendings(params: TrendingParams): SocialResponse<ArrayList<ProfileData>> {
        return socialApi.getTrending(params)
    }

    override fun createGroup(
        id: String,
        name: String,
        url: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            socialApi.createGroup(CreateGroupParams(id, name, url))
        }
    }

    override suspend fun blockUser(params: SubscribeParams): SocialResponse<DefaultResponse> {
        return socialApi.blockUser(params)
    }

    override suspend fun unblockUser(params: SubscribeParams): SocialResponse<DefaultResponse> {
        return socialApi.unblockUser(params)
    }

    override suspend fun getBlockedUsers(): SocialResponse<ArrayList<ProfileData>> {
        return socialApi.getBlockedUsers()
    }

    override suspend fun changePostNotificationSettings(params: NotificationSettingsParams): SocialResponse<DefaultResponse> {
        return socialApi.updateNotificationSettings(params)
    }

}