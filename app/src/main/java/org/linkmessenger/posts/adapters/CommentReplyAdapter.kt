package org.linkmessenger.posts.adapters

import android.content.Context
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import org.linkmessenger.base.ui.AndroidUtilities
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.request.models.Comment
import org.linkmessenger.request.models.CommentReply
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.linkmessenger.utils.DateParser
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.contacts.avatars.GeneratedContactPhoto
import org.thoughtcrime.securesms.contacts.avatars.ResourceContactPhoto
import org.thoughtcrime.securesms.conversation.colors.AvatarColor
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.DateUtils
import java.util.*

class CommentReplyAdapter (val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items:ArrayList<CommentReply?> = ArrayList()

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int {
        return if(items[position] == null){
            -1
        }else{
            position
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.comment_reply_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentText: TextView = itemView.findViewById(R.id.comment)
        val displayName: TextView = itemView.findViewById(R.id.display_name)
        val date: TextView = itemView.findViewById(R.id.time_ago)
        val avatarView: ImageView = itemView.findViewById(R.id.avatar)
        val replyComment: TextView = itemView.findViewById(R.id.reply_comment_action)
    }

    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        val item = items[position]

        if(item!=null){
            val builder = SpannableStringBuilder()
            val parts = item.comment!!.split("\\s".toRegex())
            val spannable = SpannableString("${item.username} ")
            spannable.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(context, R.color.username_comment_color)),
                    0, item.username.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            builder.append(spannable)
            for (word in parts) {
                if (word.startsWith("@")) {
                    val commentSpan = SpannableString(word)
                    commentSpan.setSpan(
                            ForegroundColorSpan(ContextCompat.getColor(context, R.color.username_comment_color)),
                            0, word.length,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    builder.append(commentSpan)
                    builder.append(" ")
                } else {
                    builder.append(word)
                    builder.append(" ")
                }
            }

            viewHolder.commentText.text = builder
            val name = if(!item.user?.username.isNullOrEmpty()){
                item.user?.username
            }else if(!item.user?.profileName.isNullOrEmpty()){
                item.user?.profileName
            }else{
                "Unknown"
            }

            if(item.user?.isVerified==true){
                viewHolder.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getUserBadge(3), null)
            }else{
                viewHolder.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getUserBadge(item.user?.type), null)
            }

            viewHolder.displayName.text = name
            viewHolder.date.text = DateUtils.getBriefRelativeTimeSpanString(context, Locale.getDefault(), DateParser().getDateTimeInMilliseconds(item.created_at))

            val fallbackPhotoDrawable = if(item.user?.username!=null || item.user?.profileName!=null){
                val avatarColor = AvatarColor.valueOf(SignalStore.settings().getUserAvatarIfExist(item.user.id.toString()))

                GeneratedContactPhoto(
                        viewHolder.displayName.text.toString(),
                        R.drawable.ic_profile_outline_40,
                        AndroidUtilities.dp(30f)
                ).asSmallDrawable(context, avatarColor, false)
            }else{
                ResourceContactPhoto(
                        R.drawable.ic_profile_outline_40,
                        R.drawable.ic_profile_outline_20
                ).asDrawable(context, AvatarColor.UNKNOWN, false)
            }

            if(item.user?.avatar!=null){
                Glide.with(context)
                        .load(item.user.avatar)
                        .transform(CircleCrop())
                        .dontAnimate()
                        .fallback(fallbackPhotoDrawable)
                        .error(fallbackPhotoDrawable)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .into(viewHolder.avatarView)
            }else{
                Glide.with(context)
                        .load(fallbackPhotoDrawable)
                        .transform(CircleCrop())
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .into(viewHolder.avatarView)
            }
        }
    }
}