package org.linkmessenger.data.local.entity

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
@RealmClass
open class Profile(
    @PrimaryKey
    var id:Int=0,
    var profileName:String?=null,
    var username:String?=null,
    var description:String?=null,
    var avatar:String?=null,
    var isVerified:Boolean=false,
    var isSubscribed:Boolean?=false,
    var postsCount:Int=0,
    var followersCount:Int=0,
    var subscriptionsCount:Int=0,
    var isSelf:Boolean = false,
    var type:Int=0,
): RealmModel