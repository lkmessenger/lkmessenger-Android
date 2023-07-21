package org.linkmessenger.stickers.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Sticker(
    @SerializedName("pack_id")
    val packId: String,
    @SerializedName("pack_key")
    val packKey: String,
    val title: String,
    val author: String,
    val icon:String
):Parcelable

data class StickersWithCategory(
    @SerializedName("category_id")
    val categoryId: Int,
    val title: String,
    val stickers: ArrayList<Sticker>?
)