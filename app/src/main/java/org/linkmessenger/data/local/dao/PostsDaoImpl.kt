package org.linkmessenger.data.local.dao

import android.util.Log
import io.realm.Realm
import io.realm.RealmModel
import io.realm.Sort
import io.realm.kotlin.deleteFromRealm
import io.realm.kotlin.where
import kotlinx.coroutines.*
import org.linkmessenger.data.local.entity.*
import java.lang.reflect.Type

class PostsDaoImpl:PostsDao {
    init {
        CoroutineScope(Dispatchers.IO).launch {
            clearCache()
        }

    }
    override fun saveMyPosts(posts: List<MyPost>): Boolean {
        return try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                it.insertOrUpdate(posts)
            }
            realm.close()
            true
        }catch (e:java.lang.Exception){
            Log.d("Adding Exception", e.message.toString())
            false
        }
    }

    override fun getMyPosts(id: Long): List<MyPost> {
        val realm = Realm.getDefaultInstance()
        val list = if(id==0L){
            realm.copyFromRealm(realm.where(MyPost::class.java).sort("id", Sort.DESCENDING).limit(20).findAll())
        }else{
            realm.copyFromRealm(realm.where(MyPost::class.java).lessThanOrEqualTo("id", id).sort("id", Sort.DESCENDING).limit(20).findAll())
        }
        realm.close()
        return list
    }

    override fun getMyPostsToId(postId: Long): List<MyPost> {
        val realm = Realm.getDefaultInstance()
        val list = if(postId!=-1L){
            realm.copyFromRealm(realm.where(MyPost::class.java).greaterThanOrEqualTo("id", postId).sort("id", Sort.DESCENDING).findAll())
        }else{
            realm.copyFromRealm(realm.where(MyPost::class.java).sort("id", Sort.DESCENDING).findAll())
        }
        realm.close()
        return list
    }

    override fun getUserPostsToId(userId: Int, postId: Long): List<UserPost> {
        val realm = Realm.getDefaultInstance()
        val list = if(postId!=-1L){
            realm.copyFromRealm(realm.where(UserPost::class.java).equalTo("user.id", userId).greaterThanOrEqualTo("id", postId).sort("id", Sort.DESCENDING).findAll())
        }else{
            realm.copyFromRealm(realm.where(UserPost::class.java).equalTo("user.id", userId).sort("id", Sort.DESCENDING).findAll())
        }
        realm.close()
        return list
    }

    override fun clearMyPosts():Boolean {
        return try {
            var res = false
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val result = it.where(MyPost::class.java).equalTo("isMy", true).findAll()
                res = result.deleteAllFromRealm()
            }
            realm.close()
            res
        }catch (e:java.lang.Exception){
            Log.d("Delete Exception", e.message.toString())
            false
        }
    }

    override fun resetPosts(type: Type) {
        when(type){
            MyPost::class.java->{
                try {
                    val realm = Realm.getDefaultInstance()
                    realm.executeTransaction {
                        val result = it.where(MyPost::class.java).findAll()
                        result.deleteAllFromRealm()
                    }
                    realm.close()
                }catch (_:Exception){

                }
            }
        }
    }

    override fun resetPosts(userId: Int, type: Type) {
        try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                val result = it.where(UserPost::class.java).equalTo("user.id", userId).findAll()
                result.deleteAllFromRealm()
            }
            realm.close()
        }catch (_:Exception){

        }
    }

    override fun updateLikePost(postId: Long, status: Boolean) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val resultMyPost = it.where(MyPost::class.java).equalTo("id", postId).findFirst()
            resultMyPost?.isLiked = status

            val resultSubsPost = it.where(SubsPost::class.java).equalTo("id", postId).findFirst()
            resultSubsPost?.isLiked = status

            val resultCollectedPost = it.where(CollectedPost::class.java).equalTo("id", postId).findFirst()
            resultCollectedPost?.isLiked = status

            val resultLikedPost = it.where(LikedPost::class.java).equalTo("id", postId).findFirst()
            resultLikedPost?.isLiked = status

            val resultSharePost = it.where(SharePost::class.java).equalTo("id", postId).findFirst()
            resultSharePost?.isLiked = status

            val resultUserPost = it.where(UserPost::class.java).equalTo("id", postId).findFirst()
            resultUserPost?.isLiked = status
        }
        realm.close()
    }

    override fun saveSubscriptionPost(posts: List<SubsPost>): Boolean {
        return try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                it.insertOrUpdate(posts)
            }
            realm.close()
            true
        }catch (e:java.lang.Exception){
            Log.d("Adding Exception", e.message.toString())
            false
        }
    }

    override fun getSubscriptionPost(id: Long): List<SubsPost> {
        val realm = Realm.getDefaultInstance()
        val list = if(id==0L){
            realm.copyFromRealm(realm.where(SubsPost::class.java).sort("id", Sort.DESCENDING).limit(20).findAll())
        }else{
            realm.copyFromRealm(realm.where(SubsPost::class.java).lessThanOrEqualTo("id", id).sort("id", Sort.DESCENDING).limit(20).findAll())
        }
        realm.close()
        return list
    }

    override fun saveRecommendationPost(posts: List<SharePost>): Boolean {
        return try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                it.insertOrUpdate(posts)
            }
            realm.close()
            true
        }catch (e:java.lang.Exception){
            Log.d("Adding Exception", e.message.toString())
            false
        }
    }

    override fun getRecommendationPost(id: Long): List<SharePost> {
        val realm = Realm.getDefaultInstance()
        val list = if(id==0L){
            realm.copyFromRealm(realm.where(SharePost::class.java).sort("id", Sort.DESCENDING).limit(20).findAll())
        }else{
            realm.copyFromRealm(realm.where(SharePost::class.java).lessThanOrEqualTo("id", id).sort("id", Sort.DESCENDING).limit(20).findAll())
        }
        realm.close()
        return list
    }

    override fun savePost(post: SharePost): Boolean {
        return try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                it.insertOrUpdate(post)
            }
            realm.close()
            true
        }catch (e:java.lang.Exception){
            Log.d("Adding Exception", e.message.toString())
            false
        }
    }

    override fun getPost(postId: Long): SharePost? {
        val realm = Realm.getDefaultInstance()
        val post = realm.where(SharePost::class.java).equalTo("id", postId).findFirst()
            ?.let { realm.copyFromRealm(it) }
        realm.close()
        return post
    }

    override fun deletePost(postId: Long) {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            val post = realm.where(MyPost::class.java).equalTo("id", postId).findFirst()
            post?.deleteFromRealm()
        }
        realm.close()
    }

    override fun saveUserPosts(posts: List<UserPost>): Boolean {
        return try {
            val realm = Realm.getDefaultInstance()
            realm.executeTransaction {
                it.insertOrUpdate(posts)
            }
            realm.close()
            true
        }catch (e:java.lang.Exception){
            Log.d("Adding Exception", e.message.toString())
            false
        }
    }

    override fun getUserPosts(userId: Int, id: Long): List<UserPost> {
        val realm = Realm.getDefaultInstance()
        val list = if(id==0L){
            realm.copyFromRealm(realm.where(UserPost::class.java).equalTo("user.id", userId).sort("id", Sort.DESCENDING).limit(20).findAll())
        }else{
            realm.copyFromRealm(realm.where(UserPost::class.java).equalTo("user.id", userId).lessThanOrEqualTo("id", id).sort("id", Sort.DESCENDING).limit(20).findAll())
        }
        realm.close()
        return list
    }

    override fun clearCache() {
        val realm = Realm.getDefaultInstance()
        realm.executeTransaction {
            realm.deleteAll()
        }
        realm.close()
    }
}