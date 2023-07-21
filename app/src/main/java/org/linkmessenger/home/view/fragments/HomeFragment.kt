package org.linkmessenger.home.view.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.LayoutParams
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.kotlin.subscribeBy
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.linkmessenger.*
import org.linkmessenger.base.Config
import org.linkmessenger.base.ui.components.RecyclerListView
import org.linkmessenger.base.ui.components.post.PostItemCell
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.home.viewmodel.HomeViewModel
import org.linkmessenger.notifications.views.activities.NotificationsActivity
import org.linkmessenger.posts.view.activities.PostLikesActivity
import org.linkmessenger.posts.view.bottomsheet.AddTabSelectorBottomSheet
import org.linkmessenger.posts.view.bottomsheet.PostMoreBottomSheet
import org.linkmessenger.posts.viewmodel.PostsViewModel
import org.linkmessenger.profile.adapters.PostsAdapter
import org.linkmessenger.request.models.ErrorData
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.badges.Badges
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.contactshare.Contact
import org.thoughtcrime.securesms.conversation.ConversationIntents
import org.thoughtcrime.securesms.conversation.ConversationMessage
import org.thoughtcrime.securesms.conversation.mutiselect.MultiselectPart
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardBottomSheet
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragment
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragment.Companion.showBottomSheet
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragmentArgs
import org.thoughtcrime.securesms.conversation.mutiselect.forward.MultiselectForwardFragmentArgs.Companion.create
import org.thoughtcrime.securesms.database.documents.IdentityKeyMismatch
import org.thoughtcrime.securesms.database.documents.NetworkFailure
import org.thoughtcrime.securesms.database.model.*
import org.thoughtcrime.securesms.database.model.databaseprotos.GiftBadge
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.linkpreview.LinkPreview
import org.thoughtcrime.securesms.main.MainActivityListHostFragment
import org.thoughtcrime.securesms.main.Material3OnScrollHelperBinder
import org.thoughtcrime.securesms.mediasend.v2.MediaSelectionActivity
import org.thoughtcrime.securesms.mms.SlideDeck
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.posts.PostsStoryLandingItem
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.safety.SafetyNumberBottomSheet
import org.thoughtcrime.securesms.stories.StoryTextPostModel
import org.thoughtcrime.securesms.stories.StoryViewerArgs
import org.thoughtcrime.securesms.stories.dialogs.PostContextMenu
import org.thoughtcrime.securesms.stories.dialogs.StoryDialogs
import org.thoughtcrime.securesms.stories.landing.*
import org.thoughtcrime.securesms.stories.my.MyStoriesActivity
import org.thoughtcrime.securesms.stories.settings.StorySettingsActivity
import org.thoughtcrime.securesms.stories.tabs.ConversationListTab
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabsViewModel
import org.thoughtcrime.securesms.stories.viewer.StoryViewerActivity
import org.thoughtcrime.securesms.util.LifecycleDisposable
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.fragments.requireListener
import org.thoughtcrime.securesms.util.visible

class HomeFragment : DSLSettingsFragment(
        layoutId = R.layout.fragment_posts,
        layoutManagerProducer = Badges::createLayoutManagerForHorizontalScroll,
), KoinComponent, MultiselectForwardBottomSheet.Callback, PostMoreBottomSheet.BottomSheetListener {
    companion object {
        private const val LIST_SMOOTH_SCROLL_TO_TOP_THRESHOLD = 25
        const val MEDIA_SENDER = 12
    }

    private val lifecycleDisposable = LifecycleDisposable()

    private val viewModel: StoriesLandingViewModel by viewModels(
            factoryProducer = {
                StoriesLandingViewModel.Factory(StoriesLandingRepository(requireContext()))
            }
    )

    private val tabsViewModel: ConversationListTabsViewModel by viewModels(ownerProducer = { requireActivity() })

    private lateinit var adapter: MappingAdapter

    private val postsViewModel:PostsViewModel = get()
    private val homeViewModel:HomeViewModel = get()
    private var postsView: RecyclerListView?=null
    private var refreshView: SwipeRefreshLayout?=null
    private var loadingView: LinearProgressIndicator?=null
    private var postsAdapter: PostsAdapter? = null
//    private var emptyState: LinearLayout? = null
    private lateinit var layoutManager:LinearLayoutManager

    private var isLoading = false

    lateinit var listener: PostMoreBottomSheet.BottomSheetListener

    var currentPost:PostItemCell?=null
    var currentPostPosition:Int? = null

    var emptyState: LinearLayout? = null
    var newPost: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listener = this

        requireCallback().getNotificationsAction()?.setOnClickListener {
            homeViewModel.deleteNotificationsCount()
            val intent = Intent(requireActivity(), NotificationsActivity::class.java)
            startActivity(intent)
        }

        homeViewModel.loadNotificationsCount()
    }

    @SuppressLint("InflateParams")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menu.clear()
                menuInflater.inflate(R.menu.posts_landing_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        startActivityIfAble(StorySettingsActivity.getIntent(requireContext()))
                        true
                    }
                    R.id.action_show_all_stories -> {
                        findNavController().navigate(R.id.action_postsFragment_to_storiesLandingFragment, null, null)
                        true
                    }
                    else -> {
                        false
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        postsView = view.findViewById(R.id.items)
        refreshView = view.findViewById(R.id.refresh)
        loadingView = view.findViewById(R.id.loading)
        emptyState = view.findViewById(R.id.empty_state)
        newPost = view.findViewById(R.id.conversation_list_empty_state_new_message)

        Log.d("token", "onViewCreated: " + SignalStore.account().socialToken)

        homeViewModel.notificationsCount.observe(viewLifecycleOwner){
            try {
                if(it != null){
                    val inflater = layoutInflater
                    val viewToolbar = inflater.inflate(R.layout.notification_tooltip, null)
                    val commentCount: TextView = viewToolbar.findViewById(R.id.comment_count)
                    val likesCount: TextView = viewToolbar.findViewById(R.id.likes_count)
                    val followerCount: TextView = viewToolbar.findViewById(R.id.follower_count)

//                    val commentIcon: ImageView = viewToolbar.findViewById(R.id.comment_icon)
//                    val likesIcon: ImageView = viewToolbar.findViewById(R.id.likes_icon)
//                    val followerIcon: ImageView = viewToolbar.findViewById(R.id.follower_icon)

//                    commentCount.visible = it.notifications.comment != null
//                    commentIcon.visible = it.notifications.comment != null
//                    likesCount.visible = it.notifications.reaction != null
//                    likesIcon.visible = it.notifications.reaction != null
//                    followerCount.visible = it.notifications.follow != null
//                    followerIcon.visible = it.notifications.follow != null

                    commentCount.text = notificationCountParse(it.notifications.comment)
                    likesCount.text = notificationCountParse(it.notifications.reaction)
                    followerCount.text = notificationCountParse(it.notifications.follow)

                    val popupWindow = PopupWindow(
                        viewToolbar,
                        LayoutParams.WRAP_CONTENT,
                        LayoutParams.WRAP_CONTENT
                    )

                    popupWindow.isOutsideTouchable = true
                    if(it.notifications.follow != null || it.notifications.comment != null || it.notifications.reaction != null){
                        popupWindow.showAsDropDown(requireCallback().getNotificationsAction(), 0, -24)
                    }
                }
            }catch (_:Exception){

            }
        }

        initPosts(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initPosts(view: View) {
        postsAdapter = PostsAdapter(requireContext(), PostViewType.List)
        layoutManager = LinearLayoutManager(requireContext())
        postsView?.layoutManager = layoutManager
        postsView?.adapter = postsAdapter

        if(requireParentFragment().parentFragment is MainActivityListHostFragment){
            (requireParentFragment().parentFragment as MainActivityListHostFragment).goToTop = {
                layoutManager.scrollToPosition(0)
            }
        }


        var isBusy = false
        var clicks = 0
        val clickHandler = Handler(Looper.getMainLooper())
        postsView?.setOnItemClickListener { view, position, x, y ->
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
                                }
                                else{
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
                    view.data?.let { requireContext().openComment(it.id) }
                } else if(view.moreActionButton.left<x && view.moreActionButton.measuredWidth + view.moreActionButton.left>x &&
                    view.moreActionButton.top<y && view.moreActionButton.measuredHeight + view.moreActionButton.top>y){
                    PostMoreBottomSheet.create(postsAdapter!!.items[position], position, listener).show(requireActivity().supportFragmentManager, "BOTTOM")

                }else if(view.shareButton.left<x && view.shareButton.measuredWidth + view.shareButton.left>x &&
                    view.shareButton.top<y && view.shareButton.measuredHeight + view.shareButton.top>y){
                    val linkPrevs = mutableListOf<LinkPreview>()
                    linkPrevs.add(0, LinkPreview("https://linkm.me/posts/${view.data?.id}", "Post link", "", 0, null))
                    val mediaMmsMessageRecord = MediaMmsMessageRecord(
                        0, null, null, 0, 0, 0,
                        0, 0, 3, "https://linkm.me/posts/${view.data?.id}", SlideDeck(requireContext(), listOf()),
                        10485783, 0, null, null, 0, 0, 0, false,
                        0, null, listOf(), linkPrevs, false, listOf(), false, false,
                        0, 0, 0, null, StoryType.NONE, null, null)

                    val part: MultiselectPart = MultiselectPart.Text(ConversationMessage(mediaMmsMessageRecord, null, null, false))
                    val multiselectParts= mutableSetOf<MultiselectPart>()
                    multiselectParts.add(part)

                    create(requireContext(),
                            multiselectParts
                    ) { args: MultiselectForwardFragmentArgs ->
                        showBottomSheet(childFragmentManager, args)
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
                    if(view.data!=null){
                        if(!postsViewModel.downloadVideo(requireContext(), view.data)){
                            refreshView?.showError(ErrorData(getString(R.string.wait_for_anther_post), 0))
                        }
                    }
                }else if(view.countView.left<x && view.countView.measuredWidth + view.countView.left>x &&
                    view.countView.top<y && view.countView.measuredHeight + view.countView.top>y){
                    startActivity(PostLikesActivity.newIntent(requireContext(), view.data?.id?:0))
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
                                requireContext().openProfile(it.id)
                            }
                        }
                    }
                }else if(view.headerView.left<x && view.headerView.measuredWidth + view.headerView.left>x &&
                    view.headerView.top<y && view.headerView.measuredHeight + view.headerView.top>y){
                    if(view.data!=null){
                        view.data!!.user?.let {
                            if(!it.isSelf){
                                requireContext().openProfile(it.id)
                            }
                        }
                    }
                }else if(view.allCommentsView.left<x && view.allCommentsView.measuredWidth + view.allCommentsView.left>x &&
                    view.allCommentsView.top<y && view.allCommentsView.measuredHeight + view.allCommentsView.top>y){
                    view.data?.let { requireContext().openComment(it.id) }
                }
            }
        }

        newPost?.setOnClickListener {
            AddTabSelectorBottomSheet.create().show(parentFragmentManager, "BOTTOM")
        }

        postsView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItem: Int = layoutManager.itemCount
                    val lastVisibleItem: Int = layoutManager.findLastVisibleItemPosition()
                    if (!isLoading && lastVisibleItem == totalItem - 1) {
                        isLoading = true
                        homeViewModel.loadPosts()
                    }
                }

                val firstPosition = layoutManager.findFirstVisibleItemPosition()
                val lastPosition = layoutManager.findLastVisibleItemPosition()
                val globalVisibleRect = Rect()
                postsView?.getGlobalVisibleRect(globalVisibleRect)
                var max = 0.0
                var maxPosition = 0
                for (pos in firstPosition..lastPosition) {
                    val view1 = layoutManager.findViewByPosition(pos)
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

        homeViewModel.loading.observe(viewLifecycleOwner){
            loadingView?.isVisible = it
        }
        homeViewModel.error.observe(viewLifecycleOwner){
            it?.let { it1 ->
                view.showError(it1)
                homeViewModel.error.postValue(null)
            }
        }

        homeViewModel.emptyState.observe(viewLifecycleOwner){
            emptyState?.visible = it
            postsView?.visible = !it
        }

        postsViewModel.downloadState.observe(viewLifecycleOwner){
            if(it!=null && postsAdapter!=null && postsView!=null){
                val position = postsAdapter!!.items.indexOfFirst { item-> item.id==it.id }
                val postItemCell = postsView!!.findViewHolderForAdapterPosition(position)
                if(postItemCell!=null && postItemCell.itemView is PostItemCell){
                    if(it.state!=1){
                        postItemCell.itemView.hideInfoView()
                        if(it.state==3){
                            refreshView?.showError(ErrorData(getString(R.string.already_downloaded),0))
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
        homeViewModel.posts.observe(viewLifecycleOwner){
            if(it.isNotEmpty() && postsAdapter!=null){
//                emptyState?.visible = false
                postsView?.visible = true

                if(homeViewModel.forceOnline){
                    homeViewModel.forceOnline = false
                    postsAdapter!!.items.clear()
                    postsAdapter!!.notifyDataSetChanged()
                }
                val tmpSize = postsAdapter!!.items.size
                postsAdapter!!.items.addAll(it)
                postsAdapter!!.notifyItemRangeInserted(tmpSize, it.size)

            }else{
//                emptyState?.visible = true
                postsView?.visible = false
            }
            isLoading = false
        }

        refreshView?.setOnRefreshListener {
            refreshView?.isRefreshing = false
            currentPost?.stop()
            homeViewModel.refresh()
        }
        if(homeViewModel.lastPostId == 0L){
            homeViewModel.loadPosts()
            homeViewModel.lastPostId = 0L
        }
        if (homeViewModel.stateInitialized()) {
            postsView?.layoutManager?.onRestoreInstanceState(
                homeViewModel.restoreRecyclerViewState()
            )
        }
    }

    fun handleVideoPostItem(newPosition:Int){
        if(currentPostPosition == newPosition) return
        currentPostPosition = newPosition
        currentPost?.stop()
        currentPost = null
        val tmpPostItem = layoutManager.findViewByPosition(newPosition) ?: return
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
    override fun onDestroyView() {
        currentPost?.player?.release()
//        homeViewModel.posts.value?.clear()
        postsView?.layoutManager?.onSaveInstanceState()?.let { homeViewModel.saveRecyclerViewState(it) }
//        postsView?.computeVerticalScrollOffset()?.let { homeViewModel.saveScrollPosition(it) }
        homeViewModel.onDestroy(postsAdapter!!.items)
        super.onDestroyView()
    }

    override fun onPause() {
        currentPost?.player?.pause()
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onResume() {
        super.onResume()
        viewModel.isTransitioningToAnotherScreen = false
    }

    override fun bindAdapter(adapter: MappingAdapter) {
        this.adapter = adapter

        PostsStoryLandingItem.register(adapter)
        PostsMyStoriesItem.register(adapter)
//        FiresItem.register(adapter)
        ExpandHeader.register(adapter)

        requireListener<Material3OnScrollHelperBinder>().bindScrollHelper(recyclerView!!)

        lifecycleDisposable.bindTo(viewLifecycleOwner)

        requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        tabsViewModel.onChatsSelected()
                    }
                }
        )

        viewModel.state.observe(viewLifecycleOwner) {
            if (it.loadingState == StoriesLandingState.LoadingState.LOADED) {
                adapter.submitList(getConfiguration(it).toMappingModelList())
//                emptyNotice.visible = it.hasNoStories
            }
        }

        lifecycleDisposable += tabsViewModel.tabClickEvents
                .filter { it == ConversationListTab.PROFILE }
                .subscribeBy(onNext = {
                    val layoutManager = recyclerView?.layoutManager as? LinearLayoutManager ?: return@subscribeBy
                    if (layoutManager.findFirstVisibleItemPosition() <= LIST_SMOOTH_SCROLL_TO_TOP_THRESHOLD) {
                        recyclerView?.smoothScrollToPosition(0)
                    } else {
                        recyclerView?.scrollToPosition(0)
                    }
                })
    }

    private fun getConfiguration(state: StoriesLandingState): DSLConfiguration {
        return configure {
            val (stories, hidden) = state.storiesLandingItems.map {
                createStoryLandingItem(it)
            }.partition {
                !it.data.isHidden
            }

            if (state.displayMyStoryItem) {
                customPref(
                        PostsMyStoriesItem.Model(
                                onClick = {
                                    openAddStory()
                                },
                                onAddStoryClick = {
                                    openAddStory()
                                }
                        )
                )
            }

//            customPref(
//                FiresItem.Model(
//                    onClick = {
//                        requireContext().openFire()
//                    }
//                )
//            )

            stories.forEach { item ->
                customPref(item)
            }

            if (hidden.isNotEmpty()) {
                customPref(
                        ExpandHeader.Model(
                                title = DSLSettingsText.from(R.string.StoriesLandingFragment__hidden_stories),
                                isExpanded = state.isHiddenContentVisible,
                                onClick = { viewModel.setHiddenContentVisible(it) }
                        )
                )
            }

            if (state.isHiddenContentVisible) {
                hidden.forEach { item ->
                    customPref(item)
                }
            }
        }
    }

    private fun openAddStory() {
        Permissions.with(requireActivity())
                .request(Manifest.permission.CAMERA)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.ConversationActivity_to_capture_photos_and_video_allow_signal_access_to_the_camera), R.drawable.ic_fluent_camera_24_regular)
                .withPermanentDenialDialog(getString(R.string.ConversationActivity_signal_needs_the_camera_permission_to_take_photos_or_video))
                .onAllGranted {
                    startActivityIfAble(MediaSelectionActivity.camera(requireContext(), isStory = true))
                }
                .onAnyDenied { Toast.makeText(requireContext(), R.string.ConversationActivity_signal_needs_camera_permissions_to_take_photos_or_video, Toast.LENGTH_LONG).show() }
                .execute()
    }

    private fun createStoryLandingItem(data: StoriesLandingItemData): PostsStoryLandingItem.Model {
        return PostsStoryLandingItem.Model(
            data = data,
            onRowClick = { model, preview ->
                openStoryViewer(model, preview, false)
            },
            onForwardStory = {
                MultiselectForwardFragmentArgs.create(
                    requireContext(),
                    it.data.primaryStory.multiselectCollection.toSet()
                ) { args ->
                    MultiselectForwardFragment.showBottomSheet(childFragmentManager, args)
                }
            },
            onGoToChat = {
                startActivityIfAble(
                    ConversationIntents.createBuilder(
                        requireContext(),
                        it.data.storyRecipient.id,
                        -1L
                    ).build()
                )
            },
            onHideStory = {
                if (!it.data.isHidden) {
                    handleHideStory(it)
                } else {
                    lifecycleDisposable += viewModel.setHideStory(
                        it.data.storyRecipient,
                        !it.data.isHidden
                    ).subscribe()
                }
            },
            onShareStory = {
                PostContextMenu.share(
                    this@HomeFragment,
                    it.data.primaryStory.messageRecord as MediaMmsMessageRecord
                )
            },
            onSave = {
                PostContextMenu.save(requireContext(), it.data.primaryStory.messageRecord)
            },
            onDeleteStory = {
                handleDeleteStory(it)
            },
            onInfo = { model, preview ->
                openStoryViewer(model, preview, true)
            },
            onAvatarClick = {},
            onAddToStoryClick = {
                openAddStory()
            }
        )
    }

    private fun openStoryViewer(model: PostsStoryLandingItem.Model, preview: View, isFromInfoContextMenuAction: Boolean) {
        if (model.data.storyRecipient.isMyStory) {
            startActivityIfAble(Intent(requireContext(), MyStoriesActivity::class.java))
        } else if (model.data.primaryStory.messageRecord.isOutgoing && model.data.primaryStory.messageRecord.isFailed) {
            if (model.data.primaryStory.messageRecord.isIdentityMismatchFailure) {
                SafetyNumberBottomSheet
                        .forMessageRecord(requireContext(), model.data.primaryStory.messageRecord)
                        .show(childFragmentManager)
            } else {
                StoryDialogs.resendStory(requireContext()) {
                    lifecycleDisposable += viewModel.resend(model.data.primaryStory.messageRecord).subscribe()
                }
            }
        } else {
            val options = ActivityOptionsCompat.makeSceneTransitionAnimation(requireActivity(), preview, ViewCompat.getTransitionName(preview) ?: "")

            val record = model.data.primaryStory.messageRecord as MmsMessageRecord
            val blur = record.slideDeck.thumbnailSlide?.placeholderBlur
            val (text: StoryTextPostModel?, image: Uri?) = if (record.storyType.isTextStory) {
                StoryTextPostModel.parseFrom(record) to null
            } else {
                null to record.slideDeck.thumbnailSlide?.uri
            }

            startActivityIfAble(
                    StoryViewerActivity.createIntent(
                            context = requireContext(),
                            storyViewerArgs = StoryViewerArgs(
                                    recipientId = model.data.storyRecipient.id,
                                    storyId = -1L,
                                    isInHiddenStoryMode = model.data.isHidden,
                                    storyThumbTextModel = text,
                                    storyThumbUri = image,
                                    storyThumbBlur = blur,
                                    recipientIds = viewModel.getRecipientIds(model.data.isHidden, model.data.storyViewState == StoryViewState.UNVIEWED),
                                    isUnviewedOnly = model.data.storyViewState == StoryViewState.UNVIEWED,
                                    isFromInfoContextMenuAction = isFromInfoContextMenuAction
                            )
                    ),
                    options.toBundle()
            )
        }
    }

    private fun handleDeleteStory(model: PostsStoryLandingItem.Model) {
        lifecycleDisposable += PostContextMenu.delete(requireContext(), setOf(model.data.primaryStory.messageRecord)).subscribe()
    }

    private fun handleHideStory(model: PostsStoryLandingItem.Model) {
        MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_Signal_MaterialAlertDialog)
                .setTitle(R.string.StoriesLandingFragment__hide_story)
                .setMessage(getString(R.string.StoriesLandingFragment__new_story_updates, model.data.storyRecipient.getShortDisplayName(requireContext())))
                .setPositiveButton(R.string.StoriesLandingFragment__hide) { _, _ ->
                    viewModel.setHideStory(model.data.storyRecipient, true).subscribe {
                        Snackbar.make(requireView(), R.string.StoriesLandingFragment__story_hidden, Snackbar.LENGTH_SHORT)
                                .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_FADE)
                                .show()
                    }
                }
                .setNegativeButton(android.R.string.cancel) { _, _ -> }
                .show()
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)",
        "org.thoughtcrime.securesms.permissions.Permissions"
    )
    )
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
    }

    private fun startActivityIfAble(intent: Intent, options: Bundle? = null)
        {
        if (viewModel.isTransitioningToAnotherScreen) {
            return
        }

        viewModel.isTransitioningToAnotherScreen = true
        startActivity(intent, options)
    }

    private fun requireCallback(): Callback {
        return requireParentFragment().parentFragment as Callback
    }

    interface Callback : Material3OnScrollHelperBinder {
        fun getToolbar(): Toolbar
        fun getNotificationsAction(): ImageView?
    }

    override fun onFinishForwardAction() {
    }

    override fun onDismissForwardSheet() {
    }

    override fun onSaveClick(position: Int, isSaved: Boolean) {
        postsAdapter?.items?.get(position)?.saved = isSaved
        postsAdapter?.notifyItemChanged(position)
    }

    fun notificationCountParse(count:String?): String {
        val intCount = count?.toInt() ?: 0

        return if(intCount > 99){
            "99+"
        }else{
            "$intCount"
        }
    }
}