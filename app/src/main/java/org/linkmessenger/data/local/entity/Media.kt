package org.linkmessenger.data.local.entity

import io.realm.RealmModel
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.request.models.MediaData

@RealmClass
open class Media(
    @PrimaryKey
    var id:Long=0,
    var position:Int=0,
    var type:Int=0,
    var url:String="",
    var orientation:Int=0,
    var preload_photo:String?=null
): RealmModel

fun Media.getThumbnail():String{
    return when (this.orientation) {
        2 -> {
            "${this.url}?h=45&w=80"
        }
        3 -> {
            "${this.url}?h=100&w=80"
        }
        else -> {
            "${this.url}?w=80&h=80"
        }
    }
}
fun Media.getSmallPhoto(viewType: PostViewType):String{
    return when (this.orientation) {
        2 -> {
            if(viewType==PostViewType.List){
                "${this.url}?h=608&w=1080"
//                "${this.url}?h=270&w=480"
            }else{
                "${this.url}?h=270&w=480"
            }
        }
        3 -> {
            if(viewType==PostViewType.List){
//                "${this.url}?h=600&w=480"
                "${this.url}?h=1350&w=1080"
            }else{
                "${this.url}?h=600&w=480"
            }
        }
        else -> {
            if(viewType==PostViewType.List){
                "${this.url}?w=1080&h=1080"
//                "${this.url}?w=480&h=480"
            }else{
                "${this.url}?w=480&h=480"
            }
        }
    }
}
//fun Media.getSmallPhoto(or:String, viewType: PostViewType):String{
//    return when (or) {
//        "l.jpeg" -> {
//            if(viewType==PostViewType.List){
//                "${this.url}?h=405&w=720"
//            }else{
//                "${this.url}?h=270&w=480"
//            }
//        }
//        "p.jpeg" -> {
//            if(viewType==PostViewType.List){
//                "${this.url}?h=900&w=720"
//            }else{
//                "${this.url}?h=600&w=480"
//            }
//        }
//        else -> {
//            if(viewType==PostViewType.List){
//                "${this.url}?w=720&h=720"
//            }else{
//                "${this.url}?w=480&h=480"
//            }
//        }
//    }
//}