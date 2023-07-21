package org.linkmessenger.stickers.repository

import org.linkmessenger.request.api.SocialApi
import org.linkmessenger.request.models.SocialResponse
import org.linkmessenger.stickers.models.Sticker
import org.linkmessenger.stickers.models.StickersWithCategory

class StickersRepositoryImpl(val socialApi: SocialApi):StickersRepository {
    override suspend fun getStickersWithCategory(): SocialResponse<ArrayList<StickersWithCategory>> {
       return socialApi.getStickersWithCategory()
    }

    override suspend fun getStickersByCategory(
        id: Int,
        page: Int,
        limit: Int
    ): SocialResponse<ArrayList<Sticker>> {
        return socialApi.getStickersByCategory(id=id, limit=limit, page=page)
    }
}