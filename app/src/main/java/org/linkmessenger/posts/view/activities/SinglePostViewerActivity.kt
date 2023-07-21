package org.linkmessenger.posts.view.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.base.ui.AndroidUtilities
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.base.ui.components.RecyclerListView
import org.linkmessenger.base.ui.components.post.PostItemCell
import org.linkmessenger.base.ui.components.post.PostOpenType
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.openProfile
import org.linkmessenger.posts.adapters.CommentsAdapter
import org.linkmessenger.posts.viewmodel.PostsViewModel
import org.linkmessenger.posts.viewmodel.SinglePostViewModel
import org.linkmessenger.profile.viewmodel.MyProfileViewModel
import org.linkmessenger.request.models.Comment
import org.linkmessenger.request.models.CommentReply
import org.linkmessenger.showError
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.components.menu.ActionItem
import org.thoughtcrime.securesms.components.menu.SignalContextMenu
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragment
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragmentArgs
import org.thoughtcrime.securesms.databinding.ActivitySinglePostViewerBinding
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.visible
import java.text.SimpleDateFormat
import java.util.*

class SinglePostViewerActivity : AppCompatActivity(), KoinComponent {
    companion object{
        fun newIntent(context: Context, postId:Long):Intent{
            return Intent(context, SinglePostViewerActivity::class.java).apply {
                putExtra("post_id", postId)
            }
        }
    }

    var isReplying = false
    var replyingToUsername: String? = null
    var commentId: Long? = null
    val viewModel:SinglePostViewModel = get()
    private val myProfileViewModel:MyProfileViewModel = get()
    val postsViewModel:PostsViewModel = get()
    lateinit var postView:PostItemCell
    var postId:Long = 0
    var count = 0
    var avatar:AvatarView? = null
    lateinit var commentsAdapter: CommentsAdapter
    var adapterPosition: Int = 0
    var activeContextMenu: SignalContextMenu? = null

    var rowsArrayList: ArrayList<String> = ArrayList()
    var isLoading = false

    private lateinit var binding: ActivitySinglePostViewerBinding

    @SuppressLint("NotifyDataSetChanged", "SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySinglePostViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        this.changeStatusBarColor()

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'")
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        postId = intent.getLongExtra("post_id", 0)

        postView = PostItemCell(this, PostOpenType.Single)
        binding.postView.addView(postView, 0)

        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }

        binding.shareButton.setOnClickListener {
            MultiselectForwardFragmentArgs.create("https://linkm.me/posts/$postId"){
                MultiselectForwardFragment.showBottomSheet(supportFragmentManager, it)
            }
        }

        binding.emoji1.setOnClickListener {
            binding.commentEditText.append(binding.emoji1.text)
        }
        binding.emoji2.setOnClickListener {
            binding.commentEditText.append(binding.emoji2.text)
        }
        binding.emoji3.setOnClickListener {
            binding.commentEditText.append(binding.emoji3.text)
        }
        binding.emoji4.setOnClickListener {
            binding.commentEditText.append(binding.emoji4.text)
        }
        binding.emoji5.setOnClickListener {
            binding.commentEditText.append(binding.emoji5.text)
        }
        binding.emoji6.setOnClickListener {
            binding.commentEditText.append(binding.emoji6.text)
        }
        binding.emoji7.setOnClickListener {
            binding.commentEditText.append(binding.emoji7.text)
        }
        binding.emoji8.setOnClickListener {
            binding.commentEditText.append(binding.emoji8.text)
        }


        viewModel.error.observe(this){
            it?.let { it1 ->
                binding.root.showError(it1)
                viewModel.error.postValue(null)
            }
        }

        viewModel.post.observe(this){ post ->
            postView.setData(post, PostViewType.List)

            postsViewModel.addPostView(post.id)

            if(post.user?.isVerified==true){
                binding.title.setCompoundDrawablesWithIntrinsicBounds(null, null, this.getUserBadge(3), null)
            }else{
                binding.title.setCompoundDrawablesWithIntrinsicBounds(null, null, this.getUserBadge(post.user?.type), null)
            }

            binding.title.text = post.user!!.username?.ifEmpty {
                post.user!!.profileName
            }

            binding.title.setOnClickListener {
                this.openProfile(post.user!!.id, post.user!!.username?.ifEmpty { post.user!!.profileName })
            }

            try {
                if (postView.data != null && postView.data?.medias!!.size == 1) {
                    val md = postView.data!!.medias!![0]
                    if (md!!.type == 2) {
                        postView.player = ExoPlayer.Builder(this).build()
                        postView.player?.addListener(object: Player.Listener{
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                when (playbackState) {
                                    ExoPlayer.STATE_ENDED -> {
                                        postView.player?.seekTo(0)
                                        postView.player?.pause()
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            postView.onLoading(false)
                                            val icon = ContextCompat.getDrawable(this@SinglePostViewerActivity, R.drawable.ic_fluent_arrow_clockwise_48_filled)
                                            Theme.setDrawableColor(icon, ContextCompat.getColor(this@SinglePostViewerActivity, R.color.signal_play_pause))
                                            postView.playButton.setImageDrawable(icon)
                                        }, 200)
                                    }
                                    ExoPlayer.STATE_BUFFERING -> {
                                        postView.onLoading(true)
                                    }
                                    else -> {
                                        postView.onLoading(false)
                                    }
                                }
                            }

                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                if(isPlaying){
                                    postView.onVideoPlay()
                                }else{
                                    postView.onVideoPause()
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                super.onPlayerError(error)
                            }

                            override fun onPlayerErrorChanged(error: PlaybackException?) {
                                super.onPlayerErrorChanged(error)
                            }
                        })
                        postView.playerView.useController = false
                        postView.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        postView.playerView.player = postView.player
                        postView.player?.setMediaItem(MediaItem.fromUri(md.url))
                        postView.player?.prepare()
                    }
                }
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
            }

            postView.playerView.setOnClickListener {
                postView.playButton.performClick()
            }
            postView.playButton.setOnClickListener {
                if(postView.player?.isPlaying==true){
                    postView.player?.pause()
                }else{
                    postView.player?.play()
                }
            }
            postView.countView.setOnClickListener {
                startActivity(PostLikesActivity.newIntent(this, post.id))
            }
            postsViewModel.addPostView(post.id)
        }

        viewModel.loadPost(postId)

        binding.postComment.setOnClickListener { clickedView ->
            val comment = binding.commentEditText.text
            val formattedDate = inputFormat.format(Date())

            if(comment.isNotEmpty()){
                if(commentId != null && replyingToUsername != null){
                    viewModel.addCommentReply(postId, commentId!!, comment.toString(), replyingToUsername!!){ id, addedComment, username, parentId ->
                        val commentViewHolder = binding.comments.findViewHolderForAdapterPosition(adapterPosition)
                        if(commentViewHolder!=null && commentViewHolder is CommentsAdapter.ItemViewHolder){
                            val tmpSize = commentViewHolder.adapter.items.size
                            val newComment = CommentReply(id, myProfileViewModel.myProfileData.value, postId, parentId, addedComment,
                                    formattedDate.toString(), username, true)
                            commentViewHolder.adapter.items.add(0, newComment)
                            commentViewHolder.repliesCount++
                            commentViewHolder.repliesRecyclerView.visible = true
                            commentViewHolder.adapter.notifyItemRangeInserted(tmpSize, 1)
                            commentViewHolder.adapter.notifyDataSetChanged()
                        }
                    }
                }else{
                    viewModel.addComment(postId, comment.toString()){id, addedComment ->
                        val tmpSize = commentsAdapter.items.size
                        commentsAdapter.items.add(0, Comment(id, myProfileViewModel.myProfileData.value, postId, addedComment,
                                0, arrayListOf(), formattedDate.toString(), true))
                        commentsAdapter.notifyItemRangeInserted(tmpSize, 1)
                        commentsAdapter.notifyDataSetChanged()
                    }
                }
                dismissReply(clickedView)

            }
        }

        binding.dismissReply.setOnClickListener{
            dismissReply(it)
        }

        initComments()
    }

    override fun onResume() {
        try {
            AndroidUtilities.checkDisplaySize(this, resources.configuration)
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
        }
        super.onResume()
    }

    private fun dismissReply(view: View) {
        binding.replyingToContainer.visible = false
        replyingToUsername = null
        commentId = null
        ViewUtil.hideKeyboard(this, view)
        binding.commentEditText.clearFocus()
        binding.commentEditText.text.clear()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initComments() {
        commentsAdapter = CommentsAdapter(this, viewModel.post.value?.isSelf == true) { commentReply, id ->
            if (commentReply != null) {
                commentingAction(commentReply.user?.username ?: commentReply.user?.profileName
                ?: "Unknown", id)
            }
        }
        val layoutManager = LinearLayoutManager(this)
        binding.comments.layoutManager = layoutManager
        binding.comments.adapter = commentsAdapter

        binding.comments.setOnItemClickListener { view, position, x, y ->
            val replyCommentAction: TextView = view.findViewById(R.id.reply_comment_action)
            val avatar: ImageView = view.findViewById(R.id.avatar)
            val more: ImageButton = view.findViewById(R.id.more)
            val nameView: TextView = view.findViewById(R.id.display_name)

            adapterPosition = position

            if(replyCommentAction.left<x && replyCommentAction.measuredWidth + replyCommentAction.left>x &&
                    replyCommentAction.top<y && replyCommentAction.measuredHeight + replyCommentAction.top>y){
                val name = commentsAdapter.items[position]?.user?.username ?: commentsAdapter.items[position]?.user?.profileName ?: "Unknown"
                val id = commentsAdapter.items[position]?.id ?: 0
                commentingAction(name, id)
            }else if (avatar.left<x && avatar.measuredWidth + avatar.left>x &&
                    avatar.top<y && avatar.measuredHeight + avatar.top>y){
                this.openProfile(commentsAdapter.items[position]?.user!!.id)
            }else if (nameView.left<x && nameView.measuredWidth + nameView.left>x &&
                    nameView.top<y && nameView.measuredHeight + nameView.top>y){
                this.openProfile(commentsAdapter.items[position]?.user?.id)
            } else if (more.left<x && more.measuredWidth + more.left>x &&
                    more.top<y && more.measuredHeight + more.top>y){
                if (activeContextMenu == null) {
                    view.isSelected = true
                    val items: MutableList<ActionItem> = ArrayList()

                    if(commentsAdapter.items[position]?.isSelf == true || viewModel.post.value?.isSelf == true){
                        items.add(ActionItem(R.drawable.ic_fluent_delete_24_regular, resources.getQuantityString(R.plurals.ConversationListFragment_delete_plural, 1), R.color.signal_text_primary) {
                            handleDeleteComment(commentsAdapter.items[position]!!.id, position)
                        })
                    }
                    items.add(ActionItem(R.drawable.ic_fluent_warning_24_regular, resources.getQuantityString(R.plurals.ConversationListFragment_report_plural, 1), R.color.signal_text_primary, { }))

                    activeContextMenu = SignalContextMenu.Builder(view, binding.comments)
                            .preferredHorizontalPosition(SignalContextMenu.HorizontalPosition.END)
                            .preferredVerticalPosition(SignalContextMenu.VerticalPosition.ABOVE)
                            .offsetX(ViewUtil.dpToPx(12))
                            .offsetY(ViewUtil.dpToPx(12))
                            .onDismiss {
                                activeContextMenu = null
                                view.isSelected = false
                                binding.comments.suppressLayout(false)
                            }
                            .show(items)
                    binding.comments.suppressLayout(true)
                }
            }
        }

        binding.comments.setOnItemLongClickListener { view, position, x, y ->
            val replies: RecyclerListView = view.findViewById(R.id.comment_replies_recycler)

            if(!(replies.left<x && replies.measuredWidth + replies.left>x &&
                    replies.top<y && replies.measuredHeight + replies.top>y)){
                onItemLongClick(view, commentsAdapter.items[position]!!.id, position, commentsAdapter.items[position]?.isSelf == true)
            }

            true
        }

        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                count++
                if (count < 10 && !viewModel.isLoadingComment) {
                    viewModel.isLoadingComment = true
                    viewModel.loadComments(postId)
                }
            }
        })

        viewModel.loading.observe(this){
            binding.loading.isVisible = it
        }
        viewModel.error.observe(this){
            it?.let { it1 ->
                binding.root.showError(it1)
                viewModel.error.postValue(null)
            }
        }

        viewModel.comments.observe(this){
            viewModel.isLoadingComment = false
            val tmpSize = commentsAdapter.items.size
            commentsAdapter.items.addAll(it)
            commentsAdapter.notifyItemRangeInserted(tmpSize, it.size)
            isLoading = false
        }

//        refreshView?.setOnRefreshListener {
//            refreshView?.isRefreshing = false
//            homeViewModel.refresh()
//        }

        viewModel.lastCommentId = 0
        viewModel.loadComments(postId)
    }

    private fun commentingAction(replyingTo: String, id: Long) {
        binding.replyingToContainer.visible = true
        replyingToUsername = "@$replyingTo"
        val replyingToText = "Replying to $replyingToUsername"
        commentId = id
        binding.replyingTo.text = replyingToText
        binding.commentEditText.requestFocus()
        openKeyboard()
    }

    private fun openKeyboard() {
        val imm: InputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.commentEditText, InputMethodManager.SHOW_IMPLICIT)
    }

    override fun onDestroy() {
        postView.player?.release()
        viewModel.comments.value?.clear()
        viewModel.lastCommentId = 0L
        viewModel.lastCommentReplyId = 0L
        super.onDestroy()
    }

    override fun onPause() {
        postView.player?.pause()
        super.onPause()
    }

    private fun onItemLongClick(view: View, id: Long, position: Int, isSelf: Boolean): Boolean {
        if (activeContextMenu != null) {
            return true
        }
        view.isSelected = true
        val items: MutableList<ActionItem> = ArrayList()

        if(isSelf){
            items.add(ActionItem(R.drawable.ic_fluent_delete_24_regular, resources.getQuantityString(R.plurals.ConversationListFragment_delete_plural, 1), R.color.signal_text_primary) {
                handleDeleteComment(id, position)
            })
        }

        items.add(ActionItem(R.drawable.ic_fluent_warning_24_regular, resources.getQuantityString(R.plurals.ConversationListFragment_report_plural, 1), R.color.signal_text_primary, {  }))

        activeContextMenu = SignalContextMenu.Builder(view, binding.comments)
                .preferredHorizontalPosition(SignalContextMenu.HorizontalPosition.END)
                .preferredVerticalPosition(SignalContextMenu.VerticalPosition.ABOVE)
                .offsetX(ViewUtil.dpToPx(12))
                .offsetY(ViewUtil.dpToPx(12))
                .onDismiss {
                    activeContextMenu = null
                    view.isSelected = false
                    binding.comments.suppressLayout(false)
                }
                .show(items)
        binding.comments.suppressLayout(true)
        return true
    }

    private fun handleDeleteComment(commentId: Long, position: Int) {
        showYesNoDialog {
            viewModel.deleteComment(commentId)
            commentsAdapter.items.removeAt(position)
            commentsAdapter.notifyItemRemoved(position)
            commentsAdapter.notifyItemRangeChanged(position, commentsAdapter.items.size-position);
        }
    }

    private fun showYesNoDialog(onDeleted:() -> Unit){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.delete_comment_title))
        builder.setPositiveButton(R.string.yes) { _, _ -> onDeleted.invoke() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(getString(R.string.delete_comment_message))
        val dialog = builder.create()
        dialog.show()
    }
}