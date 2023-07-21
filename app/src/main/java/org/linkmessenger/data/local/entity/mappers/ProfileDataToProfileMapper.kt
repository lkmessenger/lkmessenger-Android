package org.linkmessenger.data.local.entity.mappers
import org.linkmessenger.data.local.entity.Profile
import org.linkmessenger.profile.models.ProfileData

class ProfileDataToProfileMapper:Mapper<ProfileData?, Profile?>{
    override fun transform(data: ProfileData?): Profile? {
        if(data==null) return null
        return Profile(
            id = data.id,
            profileName = data.profileName,
            username = data.username,
            description = data.description,
            avatar = data.avatar,
            isVerified = data.isVerified,
            isSubscribed = data.isSubscribed,
            postsCount = data.postsCount,
            followersCount = data.followersCount,
            subscriptionsCount = data.subscriptionsCount,
            isSelf = data.isSelf,
            type = data.type
        )
    }
}