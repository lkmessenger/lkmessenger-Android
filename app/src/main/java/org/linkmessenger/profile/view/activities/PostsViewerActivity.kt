package org.linkmessenger.profile.view.activities


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.linkmessenger.base.Config
import org.linkmessenger.base.ui.components.post.PostItemCell
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.data.local.entity.MyPost
import org.linkmessenger.data.local.entity.SharePost
import org.linkmessenger.data.local.entity.UserPost
import org.linkmessenger.getVisibleHeightPercentage
import org.linkmessenger.openComment
import org.linkmessenger.openProfile
import org.linkmessenger.posts.view.activities.PostLikesActivity
import org.linkmessenger.posts.view.bottomsheet.PostMoreBottomSheet
import org.linkmessenger.posts.view.bottomsheet.PostMoreSelfBottomSheetDialogFragment
import org.linkmessenger.posts.viewmodel.PostsViewModel
import org.linkmessenger.profile.adapters.PostsAdapter
import org.linkmessenger.profile.viewmodel.PostsViewerViewModel
import org.linkmessenger.profile.viewmodel.ProfileViewModel
import org.linkmessenger.request.models.ErrorData
import org.linkmessenger.showError
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.conversation.ConversationMessage
import org.thoughtcrime.securesms.conversation.mutiselect.MultiselectPart
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragment
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragmentArgs
import org.thoughtcrime.securesms.database.model.MediaMmsMessageRecord
import org.thoughtcrime.securesms.database.model.StoryType
import org.thoughtcrime.securesms.databinding.ActivityPostsViewerBinding
import org.thoughtcrime.securesms.linkpreview.LinkPreview
import org.thoughtcrime.securesms.mms.SlideDeck
import java.lang.reflect.Type

class PostsViewerActivity : AppCompatActivity() {
    private var binding: ActivityPostsViewerBinding?=null
    private val postsViewModel:PostsViewModel by viewModel()
    private val profileViewModel: ProfileViewModel by viewModel()
    private val viewModel:PostsViewerViewModel by viewModel()
    private var postId:Long = 0L
    private var userId:Int = 0
    private var lastPostId:Long? = null
    var postsAdapter: PostsAdapter?=null
    var isLoading = false

    var type:Type?=null

    private var currentPost:PostItemCell?=null
    var currentPostPosition:Int? = null
    private var layoutManager:LinearLayoutManager?=null

    var listener: PostMoreBottomSheet.BottomSheetListener?=null
    private var listenerSelfBottomSheet: PostMoreSelfBottomSheetDialogFragment.BottomSheetListener?=null

    companion object{
        fun newIntent(context: Context, type: Type, postId:Long?, lastPostId:Long?, userId:Int?):Intent{
            return Intent(context, PostsViewerActivity::class.java).apply {
                val b = bundleOf("type" to type)
                putExtra("b", b)
                putExtra("post_id", postId)
                putExtra("user_id", userId)
                putExtra("last_post_id", lastPostId)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostsViewerBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide()

        postId = intent.getLongExtra("post_id", 0)
        userId = intent.getIntExtra("user_id", 0)
        lastPostId = intent.getLongExtra("last_post_id", 0)
        val b = intent.getBundleExtra("b")
        type = b?.get("type") as Type?

        binding?.toolbar?.setNavigationOnClickListener {
            onBackPressed()
        }

        viewModel.loading.observe(this){
            binding?.loading?.isVisible = it
        }

        layoutManager = LinearLayoutManager(this)
        binding?.items?.layoutManager = layoutManager
        postsAdapter = PostsAdapter(this, PostViewType.List)
        binding?.items?.adapter = postsAdapter

        listener = object : PostMoreBottomSheet.BottomSheetListener{
            override fun onSaveClick(position: Int, isSaved: Boolean) {
                postsAdapter?.items?.get(position)?.saved = isSaved
                postsAdapter?.notifyItemChanged(position)
            }
        }
        listenerSelfBottomSheet = object : PostMoreSelfBottomSheetDialogFragment.BottomSheetListener{
            override fun onSelfSaveClick(position: Int, isSaved: Boolean) {
                postsAdapter?.items?.get(position)?.saved = isSaved
                postsAdapter?.notifyItemChanged(position)
            }
        }

        var isBusy = false
        var clicks = 0
        val clickHandler = Handler(Looper.getMainLooper())
        binding?.items?.setOnItemClickListener { view, position, x, y ->
            if(view is PostItemCell){
                if(view.playButton.left<x && view.playButton.measuredWidth + view.playButton.left>x &&
                    view.playButton.top<y && view.playButton.measuredHeight + view.playButton.top>y){
                    if(view.data!=null && view.player!=null){
                        if(currentPostPosition != position){
                            currentPost?.stop()
                        }
                        if(view.player!!.isPlaying){
                            view.player!!.pause()
                        }else{
                            if(currentPostPosition == position){
                                view.player!!.play()
                            }else{
                                handleVideoPostItem(position)
                            }
                        }
                    }
                }else if(view.followButton.left<x && view.followButton.measuredWidth + view.followButton.left>x &&
                    view.followButton.top<y && view.followButton.measuredHeight + view.followButton.top>y){
                    if(view.data?.user?.isSubscribed != true){
                        try {
                            profileViewModel.subscribeRecommendation(view.data!!.user!!.id)
                            view.follow(true)
                        }catch (_:Exception){ }
                    }
                }else if(view.countView.left<x && view.countView.measuredWidth + view.countView.left>x &&
                    view.countView.top<y && view.countView.measuredHeight + view.countView.top>y){
                    startActivity(PostLikesActivity.newIntent(this, view.data?.id?:0))
                }else if((view.sliderView.left<x && view.sliderView.measuredWidth + view.sliderView.left>x &&
                            view.sliderView.top<y && view.sliderView.measuredHeight + view.sliderView.top>y)||
                    view.sliderOneView.left<x && view.sliderOneView.measuredWidth + view.sliderOneView.left>x &&
                    view.sliderOneView.top<y && view.sliderOneView.measuredHeight + view.sliderOneView.top>y){
                    if(!isBusy){
                        isBusy = true
                        clicks++
                        clickHandler.postDelayed(
                            {
                                 if(clicks>=2){
                                     if(view.data!=null){
                                         if(!view.data!!.isLiked){
                                             view.postLikeEvent(true)
                                             view.data?.id?.let { postsViewModel.like(it) }
                                         }
                                         view.animateLiked()
                                         view.data!!.isLiked = true
                                         view.changeLiked(view.data!!.isLiked)
                                     }
                                 }else{
                                     if(view.data!=null && view.player!=null){
                                         if(currentPostPosition != position){
                                             currentPost?.stop()
                                         }
                                         if(view.player!!.isPlaying){
                                             view.player!!.pause()
                                         }else{
                                             if(currentPostPosition == position){
                                                 view.player!!.play()
                                             }else{
                                                 handleVideoPostItem(position)
                                             }
                                         }
                                     }
                                 }
                                clicks=0
                            }, Config.DOUBLE_CLICK_INTERVAL)
                        isBusy = false
                    }
                }else if(view.commentButton.left<x && view.commentButton.measuredWidth + view.commentButton.left>x &&
                    view.commentButton.top<y && view.commentButton.measuredHeight + view.commentButton.top>y){
                    view.data?.let { openComment(it.id) }
                }else if(view.moreActionButton.left<x && view.moreActionButton.measuredWidth + view.moreActionButton.left>x &&
                    view.moreActionButton.top<y && view.moreActionButton.measuredHeight + view.moreActionButton.top>y){
                    if(type==MyPost::class.java || view.data?.isSelf == true){
                        PostMoreSelfBottomSheetDialogFragment.create(
                            {
                                view.data?.let { it1 ->
                                    viewModel.deletePost(it1.id) {
                                        postsAdapter?.items?.removeAt(position)
                                        postsAdapter?.notifyItemRemoved(position)
                                    }
                                }
                            }, postsAdapter!!.items.get(position), position, listenerSelfBottomSheet).show(supportFragmentManager, "BOTTOM")
                    }else{
                        PostMoreBottomSheet.create(postsAdapter!!.items.get(position), position, listener).show(supportFragmentManager, "BOTTOM")
                    }
                }else if(view.shareButton.left<x && view.shareButton.measuredWidth + view.shareButton.left>x &&
                    view.shareButton.top<y && view.shareButton.measuredHeight + view.shareButton.top>y){
                    val linkPrevs = mutableListOf<LinkPreview>()
                    linkPrevs.add(0, LinkPreview("https://linkm.me/posts/${view.data?.id}", "Post link", "", 0, null))
                    val mediaMmsMessageRecord = MediaMmsMessageRecord(
                        0, null, null, 0, 0, 0,
                        0, 0, 3, "https://linkm.me/posts/${view.data?.id}", SlideDeck(this, listOf()),
                        10485783, 0, null, null, 0, 0, 0, false,
                        0, null, listOf(), linkPrevs, false, listOf(), false, false,
                        0, 0, 0, null, StoryType.NONE, null, null)

                    val part: MultiselectPart = MultiselectPart.Text(ConversationMessage(mediaMmsMessageRecord, null, null, false))
                    val multiselectParts= mutableSetOf<MultiselectPart>()
                    multiselectParts.add(part)

                    MultiselectForwardFragmentArgs.create(
                        this,
                        multiselectParts
                    ) { args: MultiselectForwardFragmentArgs ->
                        MultiselectForwardFragment.showBottomSheet(supportFragmentManager, args)
                    }
                }else if(view.saveButton.left<x && view.saveButton.measuredWidth + view.saveButton.left>x &&
                    view.saveButton.top<y && view.saveButton.measuredHeight + view.saveButton.top>y){
                    if(view.data!=null){
                        if(view.data!!.saved){
                            postsViewModel.deletePostFromCollection(view.data!!.id)
                        }else{
                            postsViewModel.addPostToCollection(view.data!!.id)
                        }
                        view.data!!.saved = !view.data!!.saved
                        view.changeSaved(view.data!!.saved)
                    }
                }else if(view.downloadButton.left<x && view.downloadButton.measuredWidth + view.downloadButton.left>x &&
                    view.downloadButton.top<y && view.downloadButton.measuredHeight + view.downloadButton.top>y){
                    if(view.data!=null && view.data!!.allowDownload==1){
                        if(!postsViewModel.downloadVideo(this, view.data)){
                            binding?.root?.showError(ErrorData(getString(R.string.wait_for_anther_post), 0))
                        }
                    }
                }else if(view.likeButton.left<x && view.likeButton.measuredWidth + view.likeButton.left>x &&
                    view.likeButton.top<y && view.likeButton.measuredHeight + view.likeButton.top>y){
                    if(view.data!=null){
                        if(view.data!!.isLiked){
                            view.data?.id?.let { postsViewModel.unLike(it) }
                            view.postLikeEvent(false)
                        }else{
                            view.data?.id?.let { postsViewModel.like(it) }
                            view.postLikeEvent(true)
                            view.countView.invalidate()
                        }
                        view.data!!.isLiked = !view.data!!.isLiked
                        view.changeLiked(view.data!!.isLiked)
                    }
                }else if(view.avatarView.left<x && view.avatarView.measuredWidth + view.avatarView.left>x &&
                    view.avatarView.top<y && view.avatarView.measuredHeight + view.avatarView.top>y){
                    if(view.data!=null){
                        view.data!!.user?.let {
                            if(!it.isSelf){
                                openProfile(it.id)
                            }
                        }
                    }
                }else if(view.headerView.left<x && view.headerView.measuredWidth + view.headerView.left>x &&
                    view.headerView.top<y && view.headerView.measuredHeight + view.headerView.top>y){
                    if(view.data!=null){
                        view.data!!.user?.let {
                            if(!it.isSelf){
                                openProfile(it.id)
                            }
                        }
                    }
                }else if(view.allCommentsView.left<x && view.allCommentsView.measuredWidth + view.allCommentsView.left>x &&
                    view.allCommentsView.top<y && view.allCommentsView.measuredHeight + view.allCommentsView.top>y){
                    view.data?.let { openComment(it.id) }
                }
            }
        }


        postsViewModel.downloadState.observe(this){
            if(it!=null && postsAdapter!=null && binding!=null){
                val position = postsAdapter!!.items.indexOfFirst { item-> item.id==it.id }
                val postItemCell = binding!!.items.findViewHolderForAdapterPosition(position)
                if(postItemCell!=null && postItemCell.itemView is PostItemCell){
                    if(it.state!=1){
                        postItemCell.itemView.hideInfoView()
                        if(it.state==3){
                            binding?.root?.showError(ErrorData(getString(R.string.already_downloaded),0))
                        }
                    }else{
                        postItemCell.itemView.setInfoText(
                            getString(R.string.exo_download_downloading),
                            if(it.percent==100){
                                getString(R.string.saving)
                            }else{
                                "${it.percent}%"
                            }
                        )
                    }
                }
            }
        }


        viewModel.posts.observe(this){
            if(postsAdapter!=null){
                val tmpSize = postsAdapter!!.items.size
                postsAdapter!!.items.addAll(it)
                postsAdapter!!.notifyItemRangeInserted(tmpSize, it.size)
                if(viewModel.isFirst && postId!=0L){
                    viewModel.isFirst = false
                    var tmpIndex:Int? = null
                    postsAdapter!!.items.forEachIndexed { index, item ->
                        if(item.id == postId){
                            tmpIndex = index
                        }
                    }
                    if(tmpIndex!=null){
                        binding?.items?.scrollToPosition(tmpIndex!!)
                    }
                }
            }
            isLoading = false
        }
        viewModel.error.observe(this){
            if(it!=null){
                binding?.root?.showError(it)
                viewModel.error.postValue(null)
            }
        }
        if(type==MyPost::class.java){
            if(lastPostId!=null){
                viewModel.loadMyPostsToId(lastPostId!!)
            }else{
                viewModel.lastPostId = 0
                viewModel.loadMyPosts()
            }
        }else if(type==SharePost::class.java){
            viewModel.lastPostId = postId
            viewModel.loadSharePosts()
        }else if(type==UserPost::class.java && userId!=0){
            if(lastPostId!=null){
                viewModel.loadUserPostsToId(userId, lastPostId!!)
            }else{
                viewModel.lastPostId = 0
                viewModel.loadUserPosts(userId)
            }
        }else if(type == PostTypes.Collections::class.java){
            viewModel.lastPostId = postId
            viewModel.loadCollections()
        }

        binding?.items?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (layoutManager==null) return
                if (dy > 0) {
                    val totalItem: Int = layoutManager!!.itemCount
                    val lastVisibleItem: Int = layoutManager!!.findLastVisibleItemPosition()
                    if (!isLoading && lastVisibleItem == totalItem - 1) {
                        isLoading = true
                        loadMore()
                    }
                }

                val firstPosition = layoutManager!!.findFirstVisibleItemPosition()
                val lastPosition = layoutManager!!.findLastVisibleItemPosition()
                val globalVisibleRect = Rect()
                binding?.items?.getGlobalVisibleRect(globalVisibleRect)
                var max = 0.0
                var maxPosition = 0
                for (pos in firstPosition..lastPosition) {
                    val view1 = layoutManager!!.findViewByPosition(pos)
                    if (view1 != null) {
                        val percentage = view1.getVisibleHeightPercentage()
                        if(percentage>max){
                            max = percentage
                            maxPosition = pos
                        }
                    }
                }
                handleVideoPostItem(maxPosition)
            }
        })
    }
    fun handleVideoPostItem(newPosition:Int){
        if(layoutManager==null) return
        if(currentPostPosition == newPosition) return
        currentPostPosition = newPosition
        currentPost?.stop()
        currentPost = null
        val tmpPostItem = layoutManager!!.findViewByPosition(newPosition) ?: return
        if(currentPost == tmpPostItem) return
        if(tmpPostItem is PostItemCell){
            tmpPostItem.data?.id?.let { postsViewModel.addPostView(it) }
            if(tmpPostItem.data?.medias?.isNotEmpty()==true){
                val v = tmpPostItem.data?.medias?.get(0)
                if(v!=null && v.type==2){
                    currentPost = tmpPostItem
                }
            }
        }
        if(currentPost==null)return
        currentPost?.play()
    }
    private fun loadMore() {
        when (type) {
            MyPost::class.java -> {
                viewModel.loadMyPosts()
            }
            SharePost::class.java -> {
                viewModel.loadSharePosts()
            }
            UserPost::class.java -> {
                viewModel.loadUserPosts(userId)
            }
            PostTypes.Collections::class.java -> {
                viewModel.loadCollections()
            }

        }
    }

    override fun onDestroy() {
        currentPost?.player?.release()
        currentPost = null
        listener = null
        listenerSelfBottomSheet = null
        binding = null
        layoutManager = null
        postsAdapter = null

        super.onDestroy()
    }

    override fun onStop() {
        currentPost?.player?.pause()
        super.onStop()
    }
}

enum class PostTypes{
    Collections
}