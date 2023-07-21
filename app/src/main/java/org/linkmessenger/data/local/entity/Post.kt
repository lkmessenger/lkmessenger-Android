package org.linkmessenger.data.local.entity

import android.os.Parcelable
import io.realm.RealmList
import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass


interface Post{
    var id:Long
    var user: Profile?
    var description:String?
    var hashtags:String?
    var medias: RealmList<Media>?
    var createAt: Long?
    var modifiedAt: Long?
    var likesCount:Int
    var commentsCount:Int
    var collectionsCount:Int
    var isMy:Boolean
    var isLiked:Boolean
    var isSelf: Boolean
    var saved:Boolean
    var viewCount:Long
    var allowDownload:Int
    var commentAvailable:Int
}

@RealmClass
open class MyPost(
    @PrimaryKey
    override var id: Long=0,
    override var user: Profile?=null,
    override var description: String?=null,
    override var hashtags: String?=null,
    override var medias: RealmList<Media>?=null,
    override var createAt: Long?=null,
    override var modifiedAt: Long?=null,
    override var likesCount: Int=0,
    override var commentsCount: Int=0,
    override var isSelf: Boolean= false,
    override var collectionsCount: Int=0,
    override var isMy: Boolean=false,
    override var isLiked: Boolean=false,
    override var saved: Boolean=false,
    override var viewCount: Long=0,
    override var allowDownload: Int=1,
    override var commentAvailable: Int=1,
) :RealmModel, Post
@RealmClass
open class SubsPost(
    @PrimaryKey
    override var id: Long=0,
    override var user: Profile?=null,
    override var description: String?=null,
    override var hashtags: String?=null,
    override var medias: RealmList<Media>?=null,
    override var createAt: Long?=null,
    override var modifiedAt: Long?=null,
    override var likesCount: Int=0,
    override var commentsCount: Int=0,
    override var isSelf: Boolean= false,
    override var collectionsCount: Int=0,
    override var isMy: Boolean=false,
    override var isLiked: Boolean=false,
    override var saved: Boolean=false,
    override var viewCount: Long=0,
    override var allowDownload: Int=1,
    override var commentAvailable: Int=1,
) :RealmModel, Post
@RealmClass
open class CollectedPost(
    @PrimaryKey
    override var id: Long=0,
    override var user: Profile?=null,
    override var description: String?=null,
    override var hashtags: String?=null,
    override var isSelf: Boolean= false,
    override var medias: RealmList<Media>?=null,
    override var createAt: Long?=null,
    override var modifiedAt: Long?=null,
    override var likesCount: Int=0,
    override var commentsCount: Int=0,
    override var collectionsCount: Int=0,
    override var isMy: Boolean=false,
    override var isLiked: Boolean=false,
    override var saved: Boolean=false,
    override var viewCount: Long=0,
    override var allowDownload: Int=1,
    override var commentAvailable: Int=1,
) :RealmModel, Post
@RealmClass
open class LikedPost(
    @PrimaryKey
    override var id: Long=0,
    override var user: Profile?=null,
    override var description: String?=null,
    override var hashtags: String?=null,
    override var medias: RealmList<Media>?=null,
    override var createAt: Long?=null,
    override var modifiedAt: Long?=null,
    override var likesCount: Int=0,
    override var isSelf: Boolean= false,
    override var commentsCount: Int=0,
    override var collectionsCount: Int=0,
    override var isMy: Boolean=false,
    override var isLiked: Boolean=false,
    override var saved: Boolean=false,
    override var viewCount: Long=0,
    override var allowDownload: Int=1,
    override var commentAvailable: Int=1,
) :RealmModel, Post
@RealmClass
open class SharePost(
    @PrimaryKey
    override var id: Long=0,
    override var user: Profile?=null,
    override var description: String?=null,
    override var hashtags: String?=null,
    override var medias: RealmList<Media>?=null,
    override var createAt: Long?=null,
    override var isSelf: Boolean= false,
    override var modifiedAt: Long?=null,
    override var likesCount: Int=0,
    override var commentsCount: Int=0,
    override var collectionsCount: Int=0,
    override var isMy: Boolean=false,
    override var isLiked: Boolean=false,
    override var saved: Boolean=false,
    override var viewCount: Long=0,
    override var allowDownload: Int=1,
    override var commentAvailable: Int=1,
) :RealmModel, Post
@RealmClass
open class UserPost(
    @PrimaryKey
    override var id: Long=0,
    override var user: Profile?=null,
    override var description: String?=null,
    override var hashtags: String?=null,
    override var medias: RealmList<Media>?=null,
    override var createAt: Long?=null,
    override var modifiedAt: Long?=null,
    override var isSelf: Boolean= false,
    override var likesCount: Int=0,
    override var commentsCount: Int=0,
    override var collectionsCount: Int=0,
    override var isMy: Boolean=false,
    override var isLiked: Boolean=false,
    override var saved: Boolean=false,
    override var viewCount: Long=0,
    override var allowDownload: Int=1,
    override var commentAvailable: Int=1,
) :RealmModel, Post