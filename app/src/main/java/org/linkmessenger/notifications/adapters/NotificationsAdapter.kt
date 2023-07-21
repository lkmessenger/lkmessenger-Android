package org.linkmessenger.notifications.adapters

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.media.Image
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_INCLUSIVE_INCLUSIVE
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.koin.core.component.get
import org.linkmessenger.base.ui.AndroidUtilities
import org.linkmessenger.base.ui.components.RecyclerListView
import org.linkmessenger.openProfile
import org.linkmessenger.posts.adapters.CommentReplyAdapter
import org.linkmessenger.posts.viewmodel.SinglePostViewModel
import org.linkmessenger.request.models.Comment
import org.linkmessenger.request.models.NotificationHistory
import org.linkmessenger.utils.DateParser
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.menu.ActionItem
import org.thoughtcrime.securesms.components.menu.SignalContextMenu
import org.thoughtcrime.securesms.contacts.avatars.GeneratedContactPhoto
import org.thoughtcrime.securesms.contacts.avatars.ResourceContactPhoto
import org.thoughtcrime.securesms.conversation.colors.AvatarColor
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.DateUtils
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.visible
import java.util.*

class NotificationsAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items: ArrayList<NotificationHistory> = ArrayList()

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.notification_new_follower_item, parent, false)
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
        val content: TextView = itemView.findViewById(R.id.title)
        val post: ImageView = itemView.findViewById(R.id.post)
        val action: MaterialButton = itemView.findViewById(R.id.action)
        val avatar: ImageView = itemView.findViewById(R.id.avatar)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        val item = items[position]
        val builder = SpannableStringBuilder()
        val sender = if(item.user.Username.isNullOrEmpty()) {
            item.user.ProfileName
        }else{
            "@${item.user.Username}"
        }

        val timeAgo = DateUtils.getBriefRelativeTimeSpanString(context, Locale.getDefault(), DateParser().getDateTimeInMilliseconds(item.createdAt))

        val usernameSpan = SpannableString(sender)
        usernameSpan.setSpan(
                StyleSpan(Typeface.BOLD), 0, sender!!.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        builder.append(usernameSpan)
        if(item.type == "FOLLOW"){
            builder.append(context.getString(R.string.started_following))
        }else if(item.type == "COMMENT"){
            builder.append(" ")
            builder.append(context.getString(R.string.commented_to_post))
            builder.append(" ")
            builder.append(item.comment?.comment)
        }else if(item.type == "REACTION"){
            builder.append(context.getString(R.string.liked_post))
        }

        val timeAgoSpan = SpannableString(timeAgo)
        timeAgoSpan.setSpan(
                ForegroundColorSpan(ContextCompat.getColor(context, R.color.signal_text_secondary)),
                0, timeAgoSpan.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        timeAgoSpan.setSpan(AbsoluteSizeSpan(12, true), 0, timeAgo.length, SPAN_INCLUSIVE_INCLUSIVE)
        builder.append(" ")
        builder.append(timeAgoSpan)

        viewHolder.content.text = builder

        val fallbackPhotoDrawable = if(item.user.Username !=null || item.user.ProfileName !=null){
            val avatarColor = AvatarColor.valueOf(SignalStore.settings().getUserAvatarIfExist(item.user.ID.toString()))
            GeneratedContactPhoto(
                    sender,
                    R.drawable.ic_profile_outline_40,
                    AndroidUtilities.dp(30f)
            ).asSmallDrawable(context, avatarColor, false)
        }else{
            ResourceContactPhoto(
                    R.drawable.ic_profile_outline_40,
                    R.drawable.ic_profile_outline_20
            ).asDrawable(context, AvatarColor.UNKNOWN, false)
        }

        if(!item.user.Avatar.isNullOrEmpty()){
            Glide.with(context)
                    .load(getAvatarLink(item.user.Avatar))
                    .transform(CircleCrop())
                    .dontAnimate()
                    .fallback(fallbackPhotoDrawable)
                    .error(fallbackPhotoDrawable)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                    .into(viewHolder.avatar)
        }else{
            Glide.with(context)
                    .load(fallbackPhotoDrawable)
                    .transform(CircleCrop())
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .downsample(DownsampleStrategy.CENTER_INSIDE)
                    .into(viewHolder.avatar)
        }
    }

    fun getAvatarLink(link: String): String{
        return "https://files.linkm.me/users/$link"
    }
}

enum class NotificationTabType{
    All,
    Followers,
    Comments,
    Likes
}