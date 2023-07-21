package org.linkmessenger.profile.view.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsetsController
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.base.ui.AndroidUtilities
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.data.local.entity.UserPost
import org.linkmessenger.openPostsView
import org.linkmessenger.openProfile
import org.linkmessenger.profile.adapters.PostsAdapter
import org.linkmessenger.profile.adapters.RecommendationsAdapter
import org.linkmessenger.profile.view.bottomsheets.SendRequestBottomSheet
import org.linkmessenger.profile.viewmodel.ProfileViewModel
import org.linkmessenger.showError
import org.linkmessenger.showOfficialButtonSheep
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.linkmessenger.utils.PostUtil
import org.linkmessenger.utils.view.ItemOffsetDecoration
import org.linkmessenger.utils.view.PaddingItemDecoration
import org.signal.core.util.concurrent.SimpleTask
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.contacts.sync.ContactDiscovery.refresh
import org.thoughtcrime.securesms.conversation.ConversationIntents
import org.thoughtcrime.securesms.database.SignalDatabase.Companion.threads
import org.thoughtcrime.securesms.databinding.ActivityProfileBinding
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.views.SimpleProgressDialog
import org.thoughtcrime.securesms.util.visible
import java.io.IOException
import kotlin.math.absoluteValue


class ProfileActivity : AppCompatActivity(), KoinComponent {
    companion object{
        val TAG: String = ProfileActivity::class.java.toString()

        fun newIntent(context: Context, userId:Int?, username:String?, uuid: String?):Intent{
            return Intent(context, ProfileActivity::class.java).apply {
                putExtra("id", userId)
                putExtra("username", username)
                putExtra("uuid", uuid)
            }
        }
    }
    private lateinit var binding:ActivityProfileBinding
    private val viewModel:ProfileViewModel = get()
    private var postsAdapter: PostsAdapter? = null
    var count = 0
    var recommendationsAdapter = RecommendationsAdapter()
    var isShowingRecommendations = false
    private var userId:Int?=null
    private var username:String?=null
    private var uuid:String?=null
    var isOpeningActivity = false


    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // First, check if the dark theme is enabled

//        this.changeStatusBarColor()
        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        userId = intent.getIntExtra("id", 0)
        if(userId==0) userId = null
        username = intent.getStringExtra("username")
        uuid = intent.getStringExtra("uuid")

        if(userId==null && username.isNullOrEmpty() && uuid.isNullOrEmpty()){
            finish()
            return
        }


        binding.posts.layoutManager = GridLayoutManager(this, 3)
        val itemDecoration = ItemOffsetDecoration(this, R.dimen.item_offset)
        binding.posts.addItemDecoration(itemDecoration)
        postsAdapter = PostsAdapter(this, PostViewType.Grid)
        binding.posts.adapter = postsAdapter
        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                count++
                if (count < 20 && !binding.refresh.isRefreshing) {
                    viewModel.loadPosts()
                }
            }
        })
        viewModel.posts.observe(this){
            binding.refresh.isRefreshing = false
            if(postsAdapter!=null){
                if(viewModel.forceOnline){
                    viewModel.forceOnline = false
                    postsAdapter!!.items.clear()
                    postsAdapter!!.notifyDataSetChanged()
                }
                val tmpSize = postsAdapter!!.itemCount
                postsAdapter!!.items.addAll(it)
                postsAdapter?.notifyItemRangeInserted(tmpSize, it.size)
//                val tmpSize = postsAdapter!!.itemCount
//                postsAdapter!!.items.addAll(it)
//                postsAdapter?.notifyItemRangeInserted(tmpSize, it.size)
            }
        }
        binding.posts.setOnItemClickListener { _, position, _, _ ->
            val postItem = postsAdapter?.items?.get(position)
            if(userId == 0 || userId == null){
                postItem?.let { openPostsView(it.id, viewModel.lastPostId, UserPost::class.java, viewModel.profileData.value?.id)
                }
            }else{
                postItem?.let { openPostsView(it.id, viewModel.lastPostId, UserPost::class.java, userId)
                }
            }

        }

        binding.action3.setOnClickListener{
            isShowingRecommendations = !isShowingRecommendations
            binding.recommendationsRecycler.visible = isShowingRecommendations
            binding.suggestionText.visible = isShowingRecommendations
            binding.seeAll.visible = isShowingRecommendations

            if(isShowingRecommendations){
                binding.action3.icon = ContextCompat.getDrawable(this, R.drawable.ic_fluent_chevron_up_24_filled)
            }else{
                binding.action3.icon = ContextCompat.getDrawable(this, R.drawable.ic_fluent_chevron_down_24_filled)
            }
        }

        recommendationsAdapter = RecommendationsAdapter()

        binding.recommendationsRecycler.layoutManager = LinearLayoutManager(this).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        binding.recommendationsRecycler.adapter = recommendationsAdapter
        binding.recommendationsRecycler.addItemDecoration(PaddingItemDecoration(this, R.dimen.first_item_padding))
        binding.posts.adapter = postsAdapter

        viewModel.recommendations.observe(this){
            recommendationsAdapter.items.addAll(it!!)
            recommendationsAdapter.notifyItemRangeInserted(0, it.size)
        }

        viewModel.error.observe(this){
            it?.let { it1 ->
                binding.refresh.isRefreshing = false
                binding.root.showError(it1)
                viewModel.error.postValue(null)
            }
        }
        viewModel.loading.observe(this){
            binding.loading.isVisible = it
        }
        viewModel.subscribeLoading.observe(this){
            binding.action1.isEnabled = !it
        }

        viewModel.emptyState.observe(this){
            binding.emptyState.visible = it
            binding.posts.visible = !it
        }

        binding.seeAll.setOnClickListener {
            if(!isOpeningActivity) {
                viewModel.profileData.value?.let { it1 -> openFollowers(2, it1.id) }
            }
        }


        viewModel.profileData.observe(this){
            binding.refresh.isRefreshing = false
            if(it!=null){
                binding.action2.visibility = View.VISIBLE
                binding.title.text = it.username?.ifEmpty {
                    it.profileName ?: "Unknown"
                }
                binding.avatar.displayProfileAvatarLink(it.profileName, it.id.toString(), it.avatar)
                if(it.isVerified){
                    binding.title.setCompoundDrawablesWithIntrinsicBounds(null, null, getUserBadge(3), null)
                    binding.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, getUserBadge(3), null)
                }else{
                    binding.title.setCompoundDrawablesWithIntrinsicBounds(null, null, getUserBadge(it.type), null)
                    binding.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, getUserBadge(it.type), null)
                }

                binding.title.setOnTouchListener { _, event ->
                    val drawableRight = binding.title.compoundDrawables[2] // Index 2 represents the right drawable
                    if (drawableRight!=null && event.action == MotionEvent.ACTION_DOWN && event.rawX >= binding.title.right - drawableRight.bounds.width()) {
                        this.showOfficialButtonSheep(supportFragmentManager, it)
                        true
                    } else {
                        false
                    }
                }
                binding.displayName.setOnTouchListener { _, event ->
                    val drawableRight = binding.title.compoundDrawables[2] // Index 2 represents the right drawable
                    if (drawableRight!=null && event.action == MotionEvent.ACTION_DOWN && event.rawX >= binding.displayName.right - drawableRight.bounds.width()) {
                        this.showOfficialButtonSheep(supportFragmentManager, it)
                        true
                    } else {
                        false
                    }
                }

                binding.displayName.text = it.profileName?.ifEmpty{"Unknown"}
                binding.bioText.text = it.description
                binding.postsCount.text = PostUtil.prettyCount(it.postsCount.absoluteValue)
                binding.followersCount.text = PostUtil.prettyCount(it.followersCount.absoluteValue)
                binding.followingCount.text = PostUtil.prettyCount(it.subscriptionsCount.absoluteValue)

                binding.action1.setOnClickListener {v->
                    if(it.isSubscribed == true){
                        showYesNoDialog { viewModel.unSubscribe() }
                    }else{
                        viewModel.subscribe()
                    }
                }
                if(it.isSubscribed == true){
                    binding.action1.setText(R.string.un_subscribe)
                    binding.action1.icon = ContextCompat.getDrawable(this, R.drawable.ic_fluent_person_subtract_20_filled)
                    binding.action1.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.signal_colorOnSurfaceVariant))
                    binding.action1.setTextColor(ContextCompat.getColor(this, R.color.signal_colorOnPrimaryContainer))
                    binding.action1.setBackgroundColor(ContextCompat.getColor(this, R.color.signal_colorSurfaceVariant))
                }else{
                    if(it.isFollower==true){
                        binding.action1.setText(R.string.follow_to_replay)
                    }else{
                        binding.action1.setText(R.string.follow)
                    }

                    binding.action1.icon = ContextCompat.getDrawable(this, R.drawable.ic_fluent_person_add_20_filled)
                    binding.action1.iconTint = ColorStateList.valueOf(ContextCompat.getColor(this, R.color.white))
                    binding.action1.setTextColor(ContextCompat.getColor(this, R.color.white))
                    binding.action1.setBackgroundColor(ContextCompat.getColor(this, R.color.signal_colorPrimary))
                }
                binding.action1.visibility = View.VISIBLE
                binding.action3.visibility = View.VISIBLE

                viewModel.loadRecommendations(it.id)
            }else{
                binding.title.text = ""
                binding.avatar.displayProfileAvatarLink(it?.profileName, it?.id.toString(), it?.avatar)
                binding.displayName.text = ""
                binding.bioText.text = ""
                binding.postsCount.text = ""
                binding.followersCount.text = ""
                binding.followingCount.text = ""

                binding.action1.setText(R.string.un_subscribe)
                binding.action1.isEnabled = false
            }
        }

        binding.refresh.setOnRefreshListener {
//            binding.refresh.isRefreshing = false
//            val oldSize = postsAdapter!!.items.size
//            postsAdapter!!.items.clear()
//            postsAdapter!!.notifyItemRangeRemoved(0, oldSize)
            viewModel.loadProfile(userId, username, uuid)
        }

        binding.followersCount.setOnClickListener {
            viewModel.profileData.value?.let { it1 -> openFollowers(0, it1.id) }
        }
        binding.followersLabel.setOnClickListener {
            viewModel.profileData.value?.let { it1 -> openFollowers(0, it1.id) }
        }
        binding.followingCount.setOnClickListener {
            viewModel.profileData.value?.let { it1 -> openFollowers(1, it1.id) }
        }
        binding.followingLabel.setOnClickListener {
            viewModel.profileData.value?.let { it1 -> openFollowers(1, it1.id) }
        }
        binding.action2.setOnClickListener {
            if(viewModel.profileData.value?.phone.isNullOrEmpty()){
                handleRequestBottomSheet()
            }else{
                val phone = viewModel.profileData.value?.phone!!
                if (SignalStore.account().isRegistered && NetworkConstraint.isMet(application)) {
                    val progress = SimpleProgressDialog.show(this)
                    SimpleTask.run(lifecycle, {
                        var resolved = Recipient.external(this, phone)
                        if (!resolved.isRegistered || !resolved.hasServiceId()) {
                            Log.i(TAG, "[onContactSelected] Not registered or no UUID. Doing a directory refresh.")
                            try {
                                refresh(this, resolved, false)
                                resolved = Recipient.resolved(resolved.id)
                            } catch (e: IOException) {
                                Log.w(TAG, "[onContactSelected] Failed to refresh directory for new contact.")
                            }
                        }
                        resolved
                    }) { resolved: Recipient? ->
                        progress.dismiss()
                        launch(resolved!!)
                    }
                }
            }
        }
        binding.recommendationsRecycler.setOnItemClickListener { view, position, x, y ->
            val removeRecommendation: ImageButton = view.findViewById(R.id.remove_recommendation)
            val followUser: MaterialButton = view.findViewById(R.id.action)
            val recUser = recommendationsAdapter.items[position]

            if(removeRecommendation.left<x && removeRecommendation.measuredWidth + removeRecommendation.left>x &&
                    removeRecommendation.top<y && removeRecommendation.measuredHeight + removeRecommendation.top>y){
                recommendationsAdapter.items.removeAt(position)
                recommendationsAdapter.notifyItemRemoved(position)
            }else if(followUser.left<x && followUser.measuredWidth + followUser.left>x &&
                    followUser.top<y && followUser.measuredHeight + followUser.top>y){
                if(recUser != null){
                    if(recUser.isSubscribed == true){
                        viewModel.unSubscribeRecommendation(recUser.id)
                        recUser.isSubscribed = false
                    }else{
                        viewModel.subscribeRecommendation(recUser.id)
                        recUser.isSubscribed = true
                    }
                    recommendationsAdapter.notifyItemChanged(position)
                }
            }else{
                this.openProfile(recUser?.id, recUser?.username)
            }
        }

        binding.share.setOnClickListener {
            viewModel.profileData.value?.username?.let{
                if (it.isEmpty()) return@let
                val username = viewModel.profileData.value!!.username
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(Intent.EXTRA_SUBJECT, "@$username")
                intent.putExtra(Intent.EXTRA_TEXT, "https://linkm.me/users/$it")
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            }
        }

        viewModel.loadProfile(userId, username, uuid)

    }
    private fun handleRequestBottomSheet() {
        if(viewModel.profileData.value==null) return
        SendRequestBottomSheet.create(viewModel.profileData.value!!.isChatRequested, object : SendRequestBottomSheet.BottomSheetListener{
            override fun onSent() {
                viewModel.sendRequestForMessage(viewModel.profileData.value!!.id.toLong())
            }

        }).show(supportFragmentManager, "BOTTOM")
    }

    private fun sendRequestHundle() {

    }

    override fun onResume() {
        try {
            AndroidUtilities.checkDisplaySize(this, resources.configuration)
        }catch (e:Exception){
            FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
        }
        super.onResume()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showYesNoDialog(onUnsubscribed:() -> Unit){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.unfollow_title))
        builder.setPositiveButton(R.string.yes) { _, _ -> onUnsubscribed.invoke() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(getString(R.string.unfollow_message))
        val dialog = builder.create()
        dialog.show()
    }

    private fun openFollowers(type:Int, userId: Int){
        startActivity(FollowingAndFollowersActivity.newIntent(this, false, type, userId, false))
    }
    private fun launch(recipient: Recipient) {
        val existingThread = threads.getThreadIdIfExistsFor(recipient.id)
        val intent = ConversationIntents.createBuilder(this, recipient.id, existingThread)
                .withDraftText(intent.getStringExtra(Intent.EXTRA_TEXT))
                .withDataUri(intent.data)
                .withDataType(intent.type)
                .build()
        startActivity(intent)
        finish()
    }
}