package org.linkmessenger.stickers.repository

import org.linkmessenger.request.models.SocialResponse
import org.linkmessenger.stickers.models.Sticker
import org.linkmessenger.stickers.models.StickersWithCategory

interface StickersRepository {
    suspend fun getStickersWithCategory(): SocialResponse<ArrayList<StickersWithCategory>>
    suspend fun getStickersByCategory(id: Int, page:Int, limit:Int):SocialResponse<ArrayList<Sticker>>
}