package org.linkmessenger.stickers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import org.linkmessenger.stickers.models.Sticker
import org.thoughtcrime.securesms.R

class StickerItemAdapter(val type:StickerItemType = StickerItemType.StickersHorizontal): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items:ArrayList<Sticker> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(type == StickerItemType.StickersVertical){
            val view =   LayoutInflater.from(parent.context).inflate(R.layout.sticker_item_vertical, parent, false)
            VerticalItemViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context).inflate(R.layout.sticker_item, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context
        val imageLink = "https://files.linkm.me/stickers/" + item.icon
        val placeholder = ContextCompat.getDrawable(context, R.drawable.ic_fluent_emoji_24_regular)
        placeholder?.setTint(ContextCompat.getColor(context, R.color.grey_500))

        if(holder is ItemViewHolder){
            holder.title.text = item.title

            Glide.with(context)
                .load(imageLink)
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image)
        }else if(holder is VerticalItemViewHolder){
            holder.title.text = item.title
            holder.author.text = item.author

            Glide.with(context)
                .load(imageLink)
                .placeholder(placeholder)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(holder.image)
        }
    }

    inner class ItemViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val image:ImageView = ItemView.findViewById(R.id.sticker)
        val title:TextView = ItemView.findViewById(R.id.title)
    }
    inner class VerticalItemViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val image:ImageView = ItemView.findViewById(R.id.image)
        val title:TextView = ItemView.findViewById(R.id.title)
        val author:TextView = ItemView.findViewById(R.id.author)
    }
}

enum class StickerItemType {
    StickersHorizontal,
    StickersVertical
}