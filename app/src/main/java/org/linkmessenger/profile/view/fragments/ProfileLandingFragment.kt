package org.linkmessenger.profile.view.fragments
import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.linkmessenger.*
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.data.local.entity.MyPost
import org.linkmessenger.posts.view.bottomsheet.AddTabSelectorBottomSheet
import org.linkmessenger.profile.adapters.PostsAdapter
import org.linkmessenger.profile.adapters.RecommendationsAdapter
import org.linkmessenger.profile.view.activities.FollowingAndFollowersActivity
import org.linkmessenger.profile.viewmodel.MyProfileViewModel
import org.linkmessenger.updateinfo.views.UpdateInfoBottomSheet
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.linkmessenger.utils.PostUtil
import org.linkmessenger.utils.view.ItemOffsetDecoration
import org.linkmessenger.utils.view.PaddingItemDecoration
import org.thoughtcrime.securesms.BuildConfig
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.FragmentProfileLandingBinding
import org.thoughtcrime.securesms.main.MainActivityListHostFragment
import org.thoughtcrime.securesms.main.Material3OnScrollHelperBinder
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.visible
import kotlin.math.absoluteValue


class ProfileLandingFragment : Fragment(R.layout.fragment_profile_landing), KoinComponent{
    private val viewModel: MyProfileViewModel = get()
    private var fragmentProfileLandingBinding: FragmentProfileLandingBinding? = null
    private var postsAdapter: PostsAdapter? = null
    private lateinit var recommendationsAdapter: RecommendationsAdapter
    var recommendationsArrayList: ArrayList<String> = ArrayList()
    var isShowingRecommendations = false
    var count = 0
    private val sharedPreferences: SharedPreferences = get(named("analyticsPrefs"))

    var isOpeningActivity = false

    override fun onResume() {
        super.onResume()
        isOpeningActivity = false
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentProfileLandingBinding.bind(view)
        fragmentProfileLandingBinding = binding

        requireCallback().getCollectionsAction().setOnClickListener {
            if(!isOpeningActivity){
                isOpeningActivity = true
                requireContext().openCollections()
            }
        }
        requireCallback().getCollectionsShare().setOnClickListener {
            if(!viewModel.myProfileData.value?.username.isNullOrEmpty()){
                if(!isOpeningActivity){
                    isOpeningActivity = true
                    requireContext().openShareProfile(viewModel.myProfileData.value?.username!!)
                }
            }
        }

        binding.avatar.displayProfileAvatar(Recipient.self())
        binding.displayName.text = Recipient.self().getDisplayName(requireContext())
        binding.bioText.text = Recipient.self().about

        binding.posts.layoutManager = GridLayoutManager(requireContext(), 3)
        val itemDecoration = ItemOffsetDecoration(requireContext(), R.dimen.item_offset)
        binding.posts.addItemDecoration(itemDecoration)
        binding.followersCount.setOnClickListener {
            if(!isOpeningActivity){
                openFollowers(0, false)
            }
        }
        binding.followersLabel.setOnClickListener {
            if(!isOpeningActivity) {
                openFollowers(0,false)
            }
        }
        binding.followingCount.setOnClickListener {
            if(!isOpeningActivity) {
                openFollowers(1,false)
            }
        }
        binding.followingLabel.setOnClickListener {
            if(!isOpeningActivity) {
                openFollowers(1,false)
            }
        }
        binding.editProfile.setOnClickListener {
            if(!isOpeningActivity) {
                requireContext().openEditProfile()
            }
        }
        binding.seeAll.setOnClickListener {
            if(!isOpeningActivity) {
                openFollowers(2,false)
            }
        }

//        binding.switchWidget.setOnCheckedChangeListener { compoundButton, b ->
//            viewModel.updatePrivacySettings(PrivacySettingsParams(null, b))
//        }
//
//        viewModel.privacySettings.observe(viewLifecycleOwner){
//            binding.switchWidget.isChecked = it?.messengerAccess ?: false
//        }

        binding.showRecommendations.setOnClickListener{
            isShowingRecommendations = !isShowingRecommendations
            binding.recommendationsRecycler.visible = isShowingRecommendations
            binding.suggestionText.visible = isShowingRecommendations
            binding.seeAll.visible = isShowingRecommendations
            if(isShowingRecommendations){
                binding.showRecommendations.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_chevron_up_24_filled)
            }else{
                binding.showRecommendations.icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_chevron_down_24_filled)
            }
        }

        binding.conversationListEmptyStateNewMessage.setOnClickListener {
            AddTabSelectorBottomSheet.create().show(parentFragmentManager, "BOTTOM")
        }
        postsAdapter = PostsAdapter(requireContext(), PostViewType.Grid)

        recommendationsAdapter = RecommendationsAdapter()

        binding.recommendationsRecycler.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.HORIZONTAL
        }

        binding.recommendationsRecycler.adapter = recommendationsAdapter
        binding.recommendationsRecycler.addItemDecoration(PaddingItemDecoration(requireContext(), R.dimen.first_item_padding))
        binding.posts.adapter = postsAdapter

        viewModel.recommendations.observe(viewLifecycleOwner){
            recommendationsAdapter.items.addAll(it!!)
            recommendationsAdapter.notifyItemRangeInserted(0, it.size)
        }

        viewModel.emptyState.observe(viewLifecycleOwner){
            binding.emptyState.visible = it
            binding.posts.visible = !it
        }

//        viewModel.version.observe(viewLifecycleOwner){
//            val currentVersion = BuildConfig.VERSION_CODE
//            val isShowedUpdate = sharedPreferences.getBoolean("isShowedUpdate", false)
//            val isShowedAfterUpdate = sharedPreferences.getBoolean("isShowedAfterUpdate", false)
//
//            if(it != null){
//                if (currentVersion < it.appVersion && !isShowedUpdate){
//                    sharedPreferences.edit().putBoolean("isShowedUpdate", true).apply()
//                    sharedPreferences.edit().putBoolean("isShowedAfterUpdate", false).apply()
//                    UpdateInfoBottomSheet.create(it, false).show(parentFragmentManager, "BOTTOM")
//                }
//                if(currentVersion == it.appVersion && !isShowedAfterUpdate){
//                    sharedPreferences.edit().putBoolean("isShowedUpdate", false).apply()
//                    sharedPreferences.edit().putBoolean("isShowedAfterUpdate", true).apply()
//                    UpdateInfoBottomSheet.create(it, true).show(parentFragmentManager, "BOTTOM")
//                }
//            }
//        }


        binding.nestedScroll.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
                count++
                binding.loading.visibility = View.VISIBLE
                if (count < 20 && !binding.root.isRefreshing) {
                    loadMore()
                }
            }
        })

        binding.root.setOnRefreshListener {
            viewModel.refresh()
        }
        binding.posts.setOnItemClickListener { _, position, _, _ ->
            val postItem = postsAdapter?.items?.get(position)
            postItem?.let {
                if(!isOpeningActivity) {
                    requireContext().openPostsView(it.id, viewModel.lastPostId, MyPost::class.java, null)
                }
            }
        }

        viewModel.myProfileData.observe(requireActivity()){
            binding.root.isRefreshing = false
            if(it?.isVerified==true){
                binding.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getUserBadge(3), null)
            }else{
                binding.displayName.setCompoundDrawablesWithIntrinsicBounds(null, null, requireContext().getUserBadge(it?.type), null)
            }
            binding.displayName.text = it.profileName
            binding.bioText.text = it.description
            binding.postsCount.text = PostUtil.prettyCount(it.postsCount.absoluteValue)
            binding.followersCount.text = PostUtil.prettyCount(it.followersCount.absoluteValue)
            binding.followingCount.text = PostUtil.prettyCount(it.subscriptionsCount.absoluteValue)

            sharedPreferences.edit().putString("username",it.username).apply()

//            binding.showQr.setOnClickListener {
//                if(!viewModel.myProfileData.value?.username.isNullOrEmpty()){
//                    if(!isOpeningActivity){
//                        isOpeningActivity = true
//                        requireContext().openShareProfile(viewModel.myProfileData.value?.username!!)
//                    }
//                }
//            }
            binding.requests.text = if(it.ChatRequestsCount==0){
                requireContext().getString(R.string.requests)
            }else{
                var count = it.ChatRequestsCount
                if(count>99){
                    count=99
                }
                "${requireContext().getString(R.string.requests)}(${count})"
            }

            binding.requests.setOnClickListener { _ ->
                openFollowers(0, true)
            }
        }
        viewModel.posts.observe(viewLifecycleOwner){
            if(postsAdapter!=null){
                if(viewModel.forceOnline && it.isNotEmpty()){
                    viewModel.forceOnline = false
                    postsAdapter!!.items.clear()
                    postsAdapter!!.notifyDataSetChanged()
                }
                val tmpSize = postsAdapter!!.itemCount
                postsAdapter!!.items.addAll(it)
                postsAdapter?.notifyItemRangeInserted(tmpSize, it.size)
            }
        }
        viewModel.loading.observe(requireActivity()){
            binding.loading.visible = it
        }
        viewModel.error.observe(requireActivity()){
            it?.let {
                    it1 -> binding.root.showError(it1)
                    viewModel.error.postValue(null)
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
                        viewModel.unSubscribe(recUser.id)
                        recUser.isSubscribed = false
                    }else{
                        viewModel.subscribe(recUser.id)
                        recUser.isSubscribed = true
                    }
                    recommendationsAdapter.notifyItemChanged(position)
                }
            }else{
                if(!isOpeningActivity){
                    context?.openProfile(recUser?.id, recUser?.username)
                }
            }
        }

        populateRecommendations()

        if(requireParentFragment().parentFragment is MainActivityListHostFragment){
            (requireParentFragment().parentFragment as MainActivityListHostFragment).goToTop = {
                binding.nestedScroll.fullScroll(View.FOCUS_UP)
            }
        }

        viewModel.lastPostId = 0
        viewModel.loadPosts()
        if(viewModel.version.value == null){
            viewModel.getVersion()
        }
        viewModel.loadRecommendations(null)
    }

    private fun openFollowers(type:Int, forRequests: Boolean){
        startActivity(FollowingAndFollowersActivity.newIntent(requireContext(), true, type, -1, forRequests))
    }

    override fun onDestroyView() {
        fragmentProfileLandingBinding = null
        viewModel.posts.value?.clear()
        viewModel.recommendations.value?.clear()
        super.onDestroyView()
    }

    private fun populateRecommendations() {
        var i = 0
        while (i < 12) {
            recommendationsArrayList.add("$i")
            i++
        }
    }

    private fun loadMore() {
        viewModel.loadPosts()
    }

    private fun requireCallback(): Callback {
        return requireParentFragment().parentFragment as Callback
    }

    interface Callback : Material3OnScrollHelperBinder {
        fun getCollectionsAction(): ImageView
        fun getCollectionsShare(): ImageView
    }
}