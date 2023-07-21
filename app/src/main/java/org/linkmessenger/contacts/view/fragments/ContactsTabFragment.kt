package org.linkmessenger.contacts.view.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.OnScrollListener
import io.github.g00fy2.quickie.QRResult
import io.github.g00fy2.quickie.ScanQRCode
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.qualifier.named
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.contacts.viewmodel.ContactsViewModel
import org.linkmessenger.data.local.entity.SharePost
import org.linkmessenger.openPostsView
import org.linkmessenger.openProfile
import org.linkmessenger.openShareProfile
import org.linkmessenger.profile.adapters.PostsAdapter
import org.linkmessenger.profile.adapters.UsersAdapter
import org.linkmessenger.profile.adapters.UsersListType
import org.linkmessenger.showError
import org.linkmessenger.stickers.views.activites.StickerActivity
import org.linkmessenger.trending.views.activities.TrendingActivity
import org.linkmessenger.utils.PostUtil
import org.linkmessenger.utils.view.ItemOffsetDecoration
import org.thoughtcrime.securesms.NewConversationActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.conversationlist.ConversationListFragment
import org.thoughtcrime.securesms.databinding.FragmentContactsTabBinding
import org.thoughtcrime.securesms.main.MainActivityListHostFragment
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabsFragment
import org.thoughtcrime.securesms.util.ServiceUtil
import org.thoughtcrime.securesms.util.visible

open class ContactsTabFragment : Fragment(R.layout.fragment_contacts_tab), KoinComponent, ConversationListTabsFragment.OnBottomNavClickListener {
    private var fragmentContactsTabBinding: FragmentContactsTabBinding? = null
    private var usersAdapter: UsersAdapter? = null
    private val viewModel: ContactsViewModel = get()
    var isLoading = false
    var isPostsLoading = false
    var postsAdapter: PostsAdapter? = null
    var count = 0
    var layoutManager: GridLayoutManager? = null
    var isOpeningActivity = false
    private val sharedPreferences: SharedPreferences = get(named("analyticsPrefs"))


    override fun onResume() {
        super.onResume()
        isOpeningActivity = false
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentContactsTabBinding.bind(view)
        fragmentContactsTabBinding = binding

        viewModel.error.observe(viewLifecycleOwner){
            it?.let { it1 ->
                binding.root.showError(it1)
                viewModel.error.postValue(null)
            }
        }
        viewModel.loading.observe(viewLifecycleOwner){
            binding.progress.visible = it
        }

        binding.users.layoutManager = LinearLayoutManager(requireContext())

        usersAdapter = UsersAdapter(type = UsersListType.Search, isMyProfile = false)
        binding.users.adapter = usersAdapter


        binding.showQr.setOnClickListener {
            val un = sharedPreferences.getString("username", null)
            if(!un.isNullOrEmpty()){
                requireContext().openShareProfile(un)
            }
        }

        binding.trends.setOnClickListener {
            startActivity(TrendingActivity.newIntent(requireContext()))
        }

        binding.stickers.setOnClickListener {
            startActivity(StickerActivity.newIntent(requireContext()))
        }

        val scanQrCodeLauncher = registerForActivityResult(ScanQRCode()) { result ->
            when(result){
                is QRResult.QRSuccess -> {
                    PostUtil.handleUserUrl(requireContext(), result.content.rawValue)
                }
                QRResult.QRUserCanceled ->{
//                    binding.root.showError(Exception("User canceled"))
                }

                QRResult.QRMissingPermission ->{
                    binding.root.showError(Exception("Missing permission"))
                }
                is QRResult.QRError ->{
                    binding.root.showError(result.exception)
                }
            }
        }
        binding.qrScanner.setOnClickListener {
            scanQrCodeLauncher.launch(null)
        }
        binding.openContacts.setOnClickListener {
            startActivity(Intent(requireContext(), NewConversationActivity::class.java))
        }


        requireActivity().onBackPressedDispatcher.addCallback(
                viewLifecycleOwner,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (!closeSearchIfOpen()) {
                            requireActivity().finish()
                        }
                    }
                })
        binding.search.setOnFocusChangeListener { _, b ->
            if(b){
                requireCallback().onSearchOpened()
                binding.users.visible = true
                binding.services.visible = false
                binding.posts.visible = false
                showCloseButton()
            }
        }
        binding.closeSearch.setOnClickListener {
            closeSearchIfOpen()
        }
        binding.search.doAfterTextChanged {
            if(it!=null && it.length>1){
                usersAdapter?.items?.clear()
                usersAdapter?.notifyDataSetChanged()
                viewModel.page = 1
                viewModel.searchContacts(it.toString())
            }else{
                usersAdapter?.items?.clear()
                usersAdapter?.notifyDataSetChanged()
                viewModel.page = 1
            }
        }

        binding.users.setOnItemClickListener { _, position, _, _ ->
            val user = usersAdapter?.items?.get(position)
            if(user!=null  && !isOpeningActivity){
                isOpeningActivity = true
                requireContext().openProfile(user.id)
            }
        }
        binding.users.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if ((linearLayoutManager != null) && (usersAdapter != null) && (linearLayoutManager.findLastCompletelyVisibleItemPosition() == (usersAdapter!!.items.size - 1))) {
                        loadMore(binding.search.text.toString())
                        isLoading = true
                    }
                }
            }
        })

        viewModel.users.observe(viewLifecycleOwner){
            if(usersAdapter!=null){
                usersAdapter?.items?.clear()
                usersAdapter?.notifyDataSetChanged()
                usersAdapter!!.items.addAll(it)
                usersAdapter!!.notifyItemRangeInserted(0, it.size)
            }
        }

        layoutManager = GridLayoutManager(requireContext(), 3)


        binding.posts.layoutManager = layoutManager
        val itemDecoration = ItemOffsetDecoration(requireContext(), R.dimen.item_offset)
        binding.posts.addItemDecoration(itemDecoration)
        postsAdapter = PostsAdapter(requireContext(), PostViewType.Grid)
        binding.posts.adapter = postsAdapter
        binding.posts.setOnItemClickListener { _, position, _, _ ->
            val postItem = postsAdapter?.items?.get(position)
            postItem?.let { requireContext().openPostsView(it.id, null, SharePost::class.java, null) }
        }

//        binding.po.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
//                if (scrollY == v.getChildAt(0).measuredHeight - v.measuredHeight) {
//                    count++
//                    binding.progress.visibility = View.VISIBLE
//                    if (count < 21 && !binding.root.isRefreshing) {
//                        viewModel.loadPosts()
//                    }
//                }
//            })

        binding.posts.addOnScrollListener(object : OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0 ) {
                    val totalItem: Int = layoutManager!!.itemCount
                    val lastVisibleItem: Int = layoutManager!!.findLastVisibleItemPosition()
                    if (!isPostsLoading && lastVisibleItem == totalItem - 1) {
                        isPostsLoading = true
                        viewModel.loadPosts()
                    }
                }
            }
        })
        viewModel.posts.observe(viewLifecycleOwner){
            if(postsAdapter!=null){
                if(viewModel.forceOnline){
                    viewModel.forceOnline = false
                    postsAdapter!!.items.clear()
                    postsAdapter!!.notifyDataSetChanged()
                }
                val tmpSize = postsAdapter!!.itemCount
                postsAdapter!!.items.addAll(it)
                postsAdapter?.notifyItemRangeInserted(tmpSize, it.size)
            }
            isPostsLoading = false
        }

        if(requireParentFragment().parentFragment is MainActivityListHostFragment){
            (requireParentFragment().parentFragment as MainActivityListHostFragment).goToTop = {
                layoutManager?.scrollToPosition(0)
            }
        }

        if(viewModel.lastPostId == 0L){
            viewModel.loadPosts()
        }

        binding.refresh.setOnRefreshListener {
            binding.refresh.isRefreshing = false
            viewModel.refresh()
        }

        if (viewModel.stateInitialized()) {
            binding.posts.layoutManager?.onRestoreInstanceState(
                    viewModel.restoreRecyclerViewState()
            )
        }
    }

    private fun isSearchUserOpen():Boolean{
        return fragmentContactsTabBinding?.users?.visible == true
    }
    private fun closeSearchIfOpen():Boolean{
        if(isSearchUserOpen()){
            requireCallback().onSearchClosed()
            fragmentContactsTabBinding?.search?.clearFocus()
            hideKeyboard()
            fragmentContactsTabBinding?.search?.text = null
            fragmentContactsTabBinding?.users?.visible = false
            fragmentContactsTabBinding?.posts?.visible = true
            fragmentContactsTabBinding?.services?.visible = true
            hideCloseButton()
            return true
        }
        return false
    }

    protected open fun requireCallback(): ConversationListFragment.Callback {
        return requireParentFragment().parentFragment as ConversationListFragment.Callback
    }
    private fun showCloseButton() {
        val amin = AnimationUtils.loadAnimation(requireContext(), R.anim.animation_toggle_in)
        fragmentContactsTabBinding?.closeSearch?.startAnimation(amin)
        fragmentContactsTabBinding?.closeSearch?.isClickable = true
        fragmentContactsTabBinding?.closeSearch?.visible = true
    }
    private fun hideCloseButton() {
        val amin = AnimationUtils.loadAnimation(requireContext(), R.anim.animation_toggle_out)
        fragmentContactsTabBinding?.closeSearch?.startAnimation(amin)
        fragmentContactsTabBinding?.closeSearch?.isClickable = false
        fragmentContactsTabBinding?.closeSearch?.visible = false
    }
    private fun hideKeyboard() {
        val imm = ServiceUtil.getInputMethodManager(requireContext())
        imm.hideSoftInputFromWindow(requireView().windowToken, 0)
    }


    override fun onDestroyView() {
        viewModel.onDestroy(postsAdapter!!.items)
        fragmentContactsTabBinding?.posts?.layoutManager?.onSaveInstanceState()?.let { viewModel.saveRecyclerViewState(it) }
        fragmentContactsTabBinding = null
        super.onDestroyView()
    }

    private fun loadMore(q: String) {
        viewModel.searchContacts(q)
    }

    override fun onContactsClick() {
        layoutManager?.scrollToPosition(0)
    }
}