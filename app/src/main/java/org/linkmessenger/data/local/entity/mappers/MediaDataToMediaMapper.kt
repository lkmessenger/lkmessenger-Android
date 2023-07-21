package org.linkmessenger.data.local.entity.mappers

import org.linkmessenger.data.local.entity.Media
import org.linkmessenger.request.models.MediaData

class MediaDataToMediaMapper:Mapper<MediaData, Media>{
    override fun transform(data: MediaData): Media {
        return Media(
            id = data.id,
            position = data.position,
            type = data.type,
            url = data.url,
            orientation = data.orientation,
            preload_photo = data.preload_photo
        )
    }
}