package org.linkmessenger.data.local.dao

import io.realm.RealmModel
import org.linkmessenger.data.local.entity.*
import java.lang.reflect.Type

interface PostsDao {
    fun saveMyPosts(posts:List<MyPost>):Boolean
    fun getMyPosts(id:Long):List<MyPost>
    fun getMyPostsToId(postId: Long):List<MyPost>
    fun getUserPostsToId(userId:Int, postId: Long):List<UserPost>
    fun clearMyPosts():Boolean
    fun resetPosts(type: Type)
    fun resetPosts(userId:Int, type: Type)
    fun updateLikePost(postId:Long, status:Boolean)
    fun saveSubscriptionPost(posts: List<SubsPost>): Boolean
    fun getSubscriptionPost(id: Long):List<SubsPost>
    fun saveRecommendationPost(posts: List<SharePost>): Boolean
    fun getRecommendationPost(id: Long):List<SharePost>
    fun savePost(post:SharePost):Boolean
    fun getPost(postId:Long):SharePost?
    fun deletePost(postId:Long)
    fun saveUserPosts(posts: List<UserPost>): Boolean
    fun getUserPosts(userId:Int, id: Long):List<UserPost>
    fun clearCache()
}