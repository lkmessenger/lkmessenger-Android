package org.linkmessenger.stickers.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.linkmessenger.base.ui.components.RecyclerListView
import org.linkmessenger.stickers.models.Sticker
import org.linkmessenger.stickers.models.StickersWithCategory
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.stickers.StickerPackPreviewActivity

class StickerWithCategoryAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items: ArrayList<StickersWithCategory> = ArrayList()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.stickers_container, parent, false)

        return ItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ItemViewHolder){
            val item = items[position]
            val context = holder.itemView.context
            holder.titleView.text = item.title

            val adapter = StickerItemAdapter()
            adapter.items.addAll( item.stickers ?: arrayListOf())
            adapter.notifyItemRangeInserted(0, item.stickers?.size ?: 0 )

            val layoutManager = LinearLayoutManager(context).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }

            holder.recyclerView.layoutManager = layoutManager
            holder.recyclerView.adapter = adapter

            holder.recyclerView.setOnItemClickListener { _, sPosition, _, _ ->
                context.startActivity(StickerPackPreviewActivity.getIntent(adapter.items[sPosition].packId, adapter.items[sPosition].packKey))
            }
        }
    }

    inner class ItemViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val titleView: TextView = ItemView.findViewById(R.id.sticker_category)
        val recyclerView: RecyclerListView = ItemView.findViewById(R.id.recycler)
    }
}