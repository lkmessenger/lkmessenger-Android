package org.linkmessenger.posts.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.*
import android.widget.ImageButton
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.base.ui.AndroidUtilities
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.base.ui.components.RecyclerListView
import org.linkmessenger.base.ui.components.URLSpanNoUnderline
import org.linkmessenger.base.ui.components.browser.Browser
import org.linkmessenger.openProfile
import org.linkmessenger.posts.viewmodel.SinglePostViewModel
import org.linkmessenger.request.models.Comment
import org.linkmessenger.request.models.CommentReply
import org.linkmessenger.utils.ContextUtils.getUserBadge
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
import kotlin.math.absoluteValue


class CommentsAdapter(val context: Context, val mIsSelf: Boolean, val replyClicked: (CommentReply?, Long)-> Unit): RecyclerView.Adapter<RecyclerView.ViewHolder>(), KoinComponent {
    val items:ArrayList<Comment?> = ArrayList()

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
                    .inflate(R.layout.comment_item, parent, false)
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
        val viewMoreReply: TextView = itemView.findViewById(R.id.view_more_replies)
        val viewMoreDivider: View = itemView.findViewById(R.id.views_reply_divider)
        val repliesRecyclerView: RecyclerListView = itemView.findViewById(R.id.comment_replies_recycler)
        val replyLoading: LinearProgressIndicator = itemView.findViewById(R.id.loading)

        val adapter = CommentReplyAdapter(itemView.context)
        var repliesCount = 0
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        var isRecyclerVisible: Boolean
        var isRemoveFirst = true
        var activeContextMenu: SignalContextMenu? = null
        val item = items[position]
        viewHolder.repliesCount = item?.replies_count ?: 0
        viewHolder.repliesCount--
        val viewModel: SinglePostViewModel = get ()

        if(item!=null){
            val oldText = try {
                AndroidUtilities.getSafeString(item.comment ?: "")
            } catch (e: Throwable) {
                item.comment ?: ""
            }
            val stringBuilder = SpannableStringBuilder(oldText)
            stringBuilder.setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(context, R.color.signal_text_primary)
                ), 0, oldText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            Browser.addLinks(false, stringBuilder, false, false, false)

            viewHolder.commentText.text = stringBuilder

            viewHolder.commentText.movementMethod = LinkMovementMethod.getInstance()

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

            val fallbackPhotoDrawable = if(item.user?.username !=null || item.user?.profileName !=null){
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

            if(item.user?.avatar !=null){
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

            if(item.replies.isNotEmpty()){
                viewHolder.adapter.items.addAll(item.replies)
                viewHolder.adapter.notifyItemRangeInserted(0, item.replies.size)
                viewHolder.repliesRecyclerView.visible = true
                viewHolder.viewMoreReply.visible = true
                viewHolder.viewMoreDivider.visible = true

                if(viewHolder.repliesCount != 0){
                    val viewMoreText = "View ${viewHolder.repliesCount.absoluteValue} more reply"
                    viewHolder.viewMoreReply.text = viewMoreText
                }else{
                    val viewMoreText = "Hide replies"
                    viewHolder.viewMoreReply.text = viewMoreText
                }
            }else{
                viewHolder.viewMoreReply.visible = false
                viewHolder.viewMoreDivider.visible = false
                viewHolder.repliesRecyclerView.visible = false
            }

            viewHolder.viewMoreReply.setOnClickListener {
                if(viewHolder.repliesCount > 5){
                    if(isRemoveFirst){
                        isRemoveFirst = false
                        viewHolder.repliesCount -= 4
                    }else{
                        viewHolder.repliesCount -= 5
                    }
                }else if(viewHolder.repliesCount != 0){
                    viewHolder.repliesCount = 0
                }else{
                    viewHolder.repliesCount = -1
                }

                if(viewHolder.repliesCount > 0){
                    viewModel.loadCommentReplies(item.post_id, item.id)
                    isRecyclerVisible = true
                    val viewMoreText = "View ${viewHolder.repliesCount} more reply"
                    viewHolder.viewMoreReply.text = viewMoreText

                }else if(viewHolder.repliesCount == 0){
                    viewModel.loadCommentReplies(item.post_id, item.id)
                    isRecyclerVisible = true
                    val viewMoreText = "Hide replies"
                    viewHolder.viewMoreReply.text = viewMoreText

                }else{
                    isRecyclerVisible = false
                    viewHolder.repliesCount = item.replies_count
                    val viewMoreText = "View ${viewHolder.repliesCount} more reply"
                    viewModel.commentsReplies.value?.clear()
                    viewHolder.adapter.items.clear()
                    viewModel.lastCommentReplyId = 0
                    viewHolder.viewMoreReply.text = viewMoreText
                }

                viewHolder.repliesRecyclerView.visible =  isRecyclerVisible
            }

            viewHolder.repliesRecyclerView.setOnItemClickListener{ view, recyclerItemPosition, x, y ->
                val replyButton: TextView = view.findViewById(R.id.reply_comment_action)
                val avatar: ImageView = view.findViewById(R.id.avatar)
                val moreButton: ImageButton = view.findViewById(R.id.more)
                val nameView: TextView = view.findViewById(R.id.display_name)
                if (replyButton.left<x && replyButton.measuredWidth + replyButton.left>x &&
                        replyButton.top<y && replyButton.measuredHeight + replyButton.top>y){
                    try {
                        replyClicked.invoke(viewHolder.adapter.items.get(recyclerItemPosition), item.id)
                    }catch (e:Exception){ }

                }else if (avatar.left<x && avatar.measuredWidth + avatar.left>x &&
                        avatar.top<y && avatar.measuredHeight + avatar.top>y ||
                        nameView.left<x && nameView.measuredWidth + nameView.left>x &&
                        nameView.top<y && nameView.measuredHeight + nameView.top>y){
                    context.openProfile(viewHolder.adapter.items[recyclerItemPosition]?.user?.id)
                }else if(moreButton.left<x && moreButton.measuredWidth + moreButton.left>x &&
                        moreButton.top<y && moreButton.measuredHeight + moreButton.top>y){
                    if (activeContextMenu == null) {
                        view.isSelected = true
                        val items: MutableList<ActionItem> = ArrayList()

                        if(viewHolder.adapter.items.size > position && viewHolder.adapter.items[position]?.isSelf == true || mIsSelf){
                            items.add(ActionItem(R.drawable.ic_fluent_delete_24_regular, context.resources.getQuantityString(R.plurals.ConversationListFragment_delete_plural, 1), R.color.signal_text_primary) {
                                showYesNoDialog {
                                    viewModel.deleteComment(viewHolder.adapter.items.get(position)!!.id)
                                    viewHolder.adapter.items.removeAt(position)
                                    viewHolder.adapter.notifyItemRemoved(position)
                                    viewHolder.adapter.notifyItemRangeChanged(position, viewHolder.adapter.items.size-position);
                                }
                            })
                        }
                        items.add(ActionItem(R.drawable.ic_fluent_warning_24_regular, context.resources.getQuantityString(R.plurals.ConversationListFragment_report_plural, 1), R.color.signal_text_primary, {  }))

                        activeContextMenu = SignalContextMenu.Builder(view, viewHolder.repliesRecyclerView)
                                .preferredHorizontalPosition(SignalContextMenu.HorizontalPosition.END)
                                .preferredVerticalPosition(SignalContextMenu.VerticalPosition.ABOVE)
                                .offsetX(ViewUtil.dpToPx(12))
                                .offsetY(ViewUtil.dpToPx(12))
                                .onDismiss {
                                    activeContextMenu = null
                                    view.isSelected = false
                                    viewHolder.repliesRecyclerView.suppressLayout(false)
                                }
                                .show(items)
                        viewHolder.repliesRecyclerView.suppressLayout(true)
                    }
                }
            }

            viewHolder.repliesRecyclerView.setOnItemLongClickListener { view, position, x, y ->
                if (activeContextMenu == null) {
                    view.isSelected = true
                    val items: MutableList<ActionItem> = ArrayList()

                    if(viewHolder.adapter.items[position]?.isSelf == true){
                        items.add(ActionItem(R.drawable.ic_fluent_delete_24_regular, context.resources.getQuantityString(R.plurals.ConversationListFragment_delete_plural, 1), R.color.signal_text_primary) {
                            showYesNoDialog {
                                viewModel.deleteComment(viewHolder.adapter.items.get(position)!!.id)
                                viewHolder.adapter.items.removeAt(position)
                                viewHolder.adapter.notifyItemRemoved(position)
                                viewHolder.adapter.notifyItemRangeChanged(position, viewHolder.adapter.items.size-position);
                            }
                        })
                    }

                    items.add(ActionItem(R.drawable.ic_fluent_warning_24_regular, context.resources.getQuantityString(R.plurals.ConversationListFragment_report_plural, 1), R.color.signal_text_primary, {  }))

                    activeContextMenu = SignalContextMenu.Builder(view, viewHolder.repliesRecyclerView)
                            .preferredHorizontalPosition(SignalContextMenu.HorizontalPosition.END)
                            .preferredVerticalPosition(SignalContextMenu.VerticalPosition.ABOVE)
                            .offsetX(ViewUtil.dpToPx(12))
                            .offsetY(ViewUtil.dpToPx(12))
                            .onDismiss {
                                activeContextMenu = null
                                view.isSelected = false
                                viewHolder.repliesRecyclerView.suppressLayout(false)
                            }
                            .show(items)
                    viewHolder.repliesRecyclerView.suppressLayout(true)
                }

                true
            }

            val layoutManager = LinearLayoutManager(context)
            viewHolder.repliesRecyclerView.layoutManager = layoutManager
            viewHolder.repliesRecyclerView.adapter = viewHolder.adapter

            viewModel.commentsReplies.observe(context as LifecycleOwner){
                val tmp = viewHolder.adapter.items.size
                viewHolder.adapter.items.addAll(it)
                viewHolder.adapter.notifyItemRangeInserted(tmp, it.size)
            }

            viewModel.replyLoading.observe(context as LifecycleOwner){
                viewHolder.replyLoading.visible = it
            }
        }
    }

    private fun showYesNoDialog(onDeleted:() -> Unit){
        val builder = MaterialAlertDialogBuilder(context)
        builder.setTitle(context.getString(R.string.delete_comment_title))
        builder.setPositiveButton(R.string.yes) { _, _ -> onDeleted.invoke() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(context.getString(R.string.delete_comment_message))
        val dialog = builder.create()
        dialog.show()
    }
}