package org.linkmessenger.profile.adapters

import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.request.models.MessageRequest
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.util.visible


class UsersAdapter(val type: UsersListType = UsersListType.Search, val isMyProfile: Boolean = false) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items: ArrayList<ProfileData?> = ArrayList()

    companion object{
        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_LOADING){
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item_loading, parent, false)
            ItemLoadingViewHolder(view)
        }else{
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_item, parent, false)
            ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is ItemLoadingViewHolder) {
            showLoadingView(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ItemViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val avatarView: AvatarView = ItemView.findViewById(R.id.avatar)
        val titleView: TextView = ItemView.findViewById(R.id.title)
        val subTitleView: TextView = ItemView.findViewById(R.id.sub_title)
        val actionButton: MaterialButton = ItemView.findViewById(R.id.action)
        val denyButton: ImageButton = ItemView.findViewById(R.id.deny)
        val acceptButton: ImageButton = ItemView.findViewById(R.id.accept)

    }
    class ItemLoadingViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val progressBar: CircularProgressIndicator = itemView.findViewById(R.id.progressBar)
    }

    override fun getItemViewType(position: Int): Int {
        return if(items[position] == null){
            VIEW_TYPE_LOADING
        }else{
            VIEW_TYPE_ITEM
        }
    }

    private fun showLoadingView(viewHolder: ItemLoadingViewHolder, position: Int) {
        //ProgressBar would be displayed
    }

    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        val title = items[position]?.profileName
        val user = items[position]
        viewHolder.titleView.text = if(title.isNullOrEmpty()){"Unknown"}else{title}
        if(items[position]?.username.isNullOrEmpty()){
            viewHolder.subTitleView.visible = false
        }else{
            viewHolder.subTitleView.visible = true
            viewHolder.subTitleView.text = items[position]?.username
        }

        try {
            if(items[position]?.isVerified==true){
                viewHolder.titleView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getUserBadge(3), null)
            }else{
                viewHolder.titleView.setCompoundDrawablesWithIntrinsicBounds(null, null, context.getUserBadge(items[position]?.type), null)
            }
        }catch (_:Exception){

        }

        viewHolder.avatarView.displayProfileAvatarLink(items[position]?.profileName, items[position]?.id.toString(), items[position]?.avatar)

        if(type==UsersListType.Search){
            viewHolder.actionButton.visible = false
        }
        if(type == UsersListType.Send){
            viewHolder.actionButton.text = context.getString(R.string.conversation_activity__send)
        }
//        if(type == UsersListType.Followers){
//            viewHolder.actionButton.visible = false
//        }
        if(type == UsersListType.Subscriptions
            || type == UsersListType.Recommendations
            || type == UsersListType.TrendToday
            || type == UsersListType.TrendWeek
            || type == UsersListType.TrendMonth
            || type == UsersListType.TrendAll){
            if(user?.isSubscribed == true){
                viewHolder.actionButton.text = context.getString(R.string.un_subscribe)
                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.signal_colorOnPrimaryContainer))
                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorSurfaceVariant))
            }else{
                viewHolder.actionButton.text = context.getString(R.string.follow)
                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.white))
                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorPrimary))
            }
        }else if(type == UsersListType.Followers){
            if(isMyProfile){
                viewHolder.actionButton.text = context.getString(R.string.ClearProfileActivity_remove)
                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.signal_colorOnPrimaryContainer))
                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorSurfaceVariant))
            }else{
                if(user?.isSubscribed == true){
                    viewHolder.actionButton.text = context.getString(R.string.un_subscribe)
                    viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.signal_colorOnPrimaryContainer))
                    viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorSurfaceVariant))
                }else{
                    viewHolder.actionButton.text = context.getString(R.string.follow)
                    viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.white))
                    viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorPrimary))
                }
            }
        }else if(type == UsersListType.BlockedUsers){
            viewHolder.actionButton.text = context.getString(R.string.BlockedUsersActivity__unblock)
            viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.signal_colorOnPrimaryContainer))
            viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorSurfaceVariant))
        }
//        if(type == UsersListType.Recommendations){
//            if(user?.isSubscribed == true){
//                viewHolder.actionButton.text = context.getString(R.string.un_subscribe)
//                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.signal_colorOnPrimaryContainer))
//                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorSurfaceVariant))
//            }else{
//                viewHolder.actionButton.text = context.getString(R.string.follow)
//                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.white))
//                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorPrimary))
//            }
//        }

        if(type == UsersListType.MessageRequest){
            viewHolder.actionButton.visibility = View.GONE
            viewHolder.acceptButton.visibility = View.VISIBLE
            viewHolder.denyButton.visibility = View.VISIBLE
        }else{
            viewHolder.acceptButton.visibility = View.GONE
            viewHolder.denyButton.visibility = View.GONE
        }

        if(type == UsersListType.PostLikes){
            viewHolder.actionButton.visibility = View.GONE
            viewHolder.acceptButton.visibility = View.GONE
            viewHolder.denyButton.visibility = View.GONE
        }

        if(type == UsersListType.SentMessageRequest){
            viewHolder.actionButton.visibility = View.VISIBLE
            if(user?.messageRequestStatus == 1){
                viewHolder.actionButton.text = context.getString(R.string.waiting)
                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.signal_colorOnPrimaryContainer))
                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorSurfaceVariant))
            }else if(user?.messageRequestStatus == 2){
                viewHolder.actionButton.text = context.getString(R.string.rejected)
                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.red_800))
                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.red_50))
            }else if(user?.messageRequestStatus == 3){
                viewHolder.actionButton.text = context.getString(R.string.accepted)
                viewHolder.actionButton.setTextColor(ContextCompat.getColor(context, R.color.green_800))
                viewHolder.actionButton.setBackgroundColor(ContextCompat.getColor(context, R.color.green_50))
            }
        }
    }
}

enum class UsersListType{
    Followers,
    Subscriptions,
    Recommendations,
    MessageRequest,
    SentMessageRequest,
    Search,
    Send,
    PostLikes,
    TrendToday,
    TrendWeek,
    TrendMonth,
    TrendAll,
    BlockedUsers
}