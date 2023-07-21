package org.linkmessenger.profile.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import org.linkmessenger.base.ui.AndroidUtilities
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.components.settings.SettingHeader.Item
import org.thoughtcrime.securesms.contacts.avatars.GeneratedContactPhoto
import org.thoughtcrime.securesms.contacts.avatars.ResourceContactPhoto
import org.thoughtcrime.securesms.conversation.colors.AvatarColor
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.Recipient


class RecommendationsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items: ArrayList<ProfileData?> = ArrayList()

    companion object{
        private const val VIEW_TYPE_ITEM = 0
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recommendation_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        populateItemRows(holder as ItemViewHolder, position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class ItemViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val avatarView: ImageView = ItemView.findViewById(R.id.avatar)
        val username: TextView = ItemView.findViewById(R.id.username)
        val displayName: TextView = ItemView.findViewById(R.id.display_name)
        val action: MaterialButton = ItemView.findViewById(R.id.action)
        val removeItem: ImageButton = ItemView.findViewById(R.id.remove_recommendation)
    }

    override fun getItemViewType(position: Int): Int {
        return VIEW_TYPE_ITEM
    }


    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        val user = items[position]

        val fallbackPhotoDrawable = if(user?.username!=null || user?.profileName!=null){
            val avatarColor = AvatarColor.valueOf(SignalStore.settings().getUserAvatarIfExist(user.id.toString()))
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

        if(user?.avatar!=null){
            Glide.with(context)
                    .load(user.avatar)
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

        if(user?.isVerified==true){
            viewHolder.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getUserBadge(3), null)
        }else{
            viewHolder.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getUserBadge(user?.type), null)
        }

        if(user?.isSubscribed == true){
            viewHolder.action.text = context.getString(R.string.un_subscribe)
            viewHolder.action.setTextColor(ContextCompat.getColor(context, R.color.signal_colorOnPrimaryContainer))
            viewHolder.action.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorSurfaceVariant))
        }else{
            viewHolder.action.text = context.getString(R.string.follow)
            viewHolder.action.setTextColor(ContextCompat.getColor(context, R.color.white))
            viewHolder.action.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorPrimary))
        }

        viewHolder.username.text = user?.username
        viewHolder.displayName.text = user?.profileName
    }
}