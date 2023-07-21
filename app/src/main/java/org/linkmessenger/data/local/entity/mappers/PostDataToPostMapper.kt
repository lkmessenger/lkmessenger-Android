package org.linkmessenger.data.local.entity.mappers

import io.realm.RealmList
import org.linkmessenger.data.local.entity.*
import org.linkmessenger.request.models.PostData
import java.lang.reflect.Type

class PostDataToPostMapper(private var isMy:Boolean=false,val type:Type):Mapper<PostData, Post>{
    override fun transform(data: PostData): Post {
        val list = RealmList<Media>()
        if(data.medias!=null){
            for (it in data.medias) {
                list.add(MediaDataToMediaMapper().transform(it))
            }
        }
        when (type) {
            MyPost::class.java -> {
                return MyPost(
                    id=data.id,
                    user=ProfileDataToProfileMapper().transform(data.user),
                    description=data.description,
                    hashtags=data.hashtags,
                    medias= list,
                    createAt=if(data.create_at!=null) data.create_at.time else null,
                    modifiedAt=if(data.modified_at!=null) data.modified_at.time else null,
                    likesCount=data.likes_count,
                    commentsCount=data.comments_count,
                    collectionsCount=data.collections_count,
                    isMy = isMy,
                    isSelf = data.isSelf,
                    isLiked = data.is_liked,
                    saved = data.is_collected,
                    viewCount = data.view_count,
                    allowDownload = data.allow_download,
                    commentAvailable = data.comment_available
                )
            }
            SubsPost::class.java -> {
                return SubsPost(
                    id=data.id,
                    user=ProfileDataToProfileMapper().transform(data.user),
                    description=data.description,
                    isSelf = data.isSelf,
                    hashtags=data.hashtags,
                    medias= list,
                    createAt=if(data.create_at!=null) data.create_at.time else null,
                    modifiedAt=if(data.modified_at!=null) data.modified_at.time else null,
                    likesCount=data.likes_count,
                    commentsCount=data.comments_count,
                    collectionsCount=data.collections_count,
                    isMy = isMy,
                    isLiked = data.is_liked,
                    saved = data.is_collected,
                    viewCount = data.view_count,
                    allowDownload = data.allow_download,
                    commentAvailable = data.comment_available
                )
            }
            CollectedPost::class.java -> {
                return CollectedPost(
                    id=data.id,
                    user=ProfileDataToProfileMapper().transform(data.user),
                    description=data.description,
                    hashtags=data.hashtags,
                    isSelf = data.isSelf,
                    medias= list,
                    createAt=if(data.create_at!=null) data.create_at.time else null,
                    modifiedAt=if(data.modified_at!=null) data.modified_at.time else null,
                    likesCount=data.likes_count,
                    commentsCount=data.comments_count,
                    collectionsCount=data.collections_count,
                    isMy = isMy,
                    isLiked = data.is_liked,
                    saved = data.is_collected,
                    viewCount = data.view_count,
                    allowDownload = data.allow_download,
                    commentAvailable = data.comment_available
                )
            }
            LikedPost::class.java -> {
                return LikedPost(
                    id=data.id,
                    user=ProfileDataToProfileMapper().transform(data.user),
                    description=data.description,
                    hashtags=data.hashtags,
                    medias= list,
                    isSelf = data.isSelf,
                    createAt=if(data.create_at!=null) data.create_at.time else null,
                    modifiedAt=if(data.modified_at!=null) data.modified_at.time else null,
                    likesCount=data.likes_count,
                    commentsCount=data.comments_count,
                    collectionsCount=data.collections_count,
                    isMy = isMy,
                    isLiked = data.is_liked,
                    saved = data.is_collected,
                    viewCount = data.view_count,
                    allowDownload = data.allow_download,
                    commentAvailable = data.comment_available
                )
            }
            UserPost::class.java -> {
                return UserPost(
                    id=data.id,
                    user=ProfileDataToProfileMapper().transform(data.user),
                    description=data.description,
                    hashtags=data.hashtags,
                    medias= list,
                    createAt=if(data.create_at!=null) data.create_at.time else null,
                    modifiedAt=if(data.modified_at!=null) data.modified_at.time else null,
                    likesCount=data.likes_count,
                    commentsCount=data.comments_count,
                    collectionsCount=data.collections_count,
                    isMy = isMy,
                    isSelf = data.isSelf,
                    isLiked = data.is_liked,
                    saved = data.is_collected,
                    viewCount = data.view_count,
                    allowDownload = data.allow_download,
                    commentAvailable = data.comment_available
                )
            }
            else -> {
                return SharePost(
                    id=data.id,
                    user=ProfileDataToProfileMapper().transform(data.user),
                    description=data.description,
                    hashtags=data.hashtags,
                    medias= list,
                    isSelf = data.isSelf,
                    createAt=if(data.create_at!=null) data.create_at.time else null,
                    modifiedAt=if(data.modified_at!=null) data.modified_at.time else null,
                    likesCount=data.likes_count,
                    commentsCount=data.comments_count,
                    collectionsCount=data.collections_count,
                    isMy = isMy,
                    isLiked = data.is_liked,
                    saved = data.is_collected,
                    viewCount = data.view_count,
                    allowDownload = data.allow_download,
                    commentAvailable = data.comment_available
                )
            }
        }
    }
}