package org.linkmessenger.notifications.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.mms.layout.LayoutManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.notifications.adapters.NotificationTabType
import org.linkmessenger.notifications.adapters.NotificationTabType.*
import org.linkmessenger.notifications.adapters.NotificationsAdapter
import org.linkmessenger.notifications.viewmodels.NotificationsViewModel
import org.linkmessenger.openProfile
import org.linkmessenger.posts.view.activities.SinglePostViewerActivity
import org.linkmessenger.profile.view.fragments.FollowingAndFollowersFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.FragmentNotificationBinding
import org.thoughtcrime.securesms.util.visible

class NotificationFragment : Fragment(R.layout.fragment_notification), KoinComponent {
    private lateinit var fragmentNotificationBinding: FragmentNotificationBinding
    val viewModel: NotificationsViewModel = get()
    var isNotificationLoading = false
    var type:NotificationTabType = All
    var typeString: String = "ALL"
    val adapter = NotificationsAdapter()

    companion object{
        fun newFragment(type: NotificationTabType): NotificationFragment {
            val params = bundleOf("type" to type)
            return NotificationFragment().apply {
                arguments = params
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = (arguments?.get("type") ?: All) as NotificationTabType

        typeString = when(type){
            All -> "ALL"
            Followers -> "FOLLOW"
            Comments -> "COMMENT"
            Likes -> "REACTION"
        }

        when(type){
            All -> viewModel.loadNotification(typeString)
            Followers -> viewModel.loadNotificationFollowers(typeString)
            Comments -> viewModel.loadNotificationComments(typeString)
            Likes -> viewModel.loadNotificationLikes(typeString)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentNotificationBinding.bind(view)
        fragmentNotificationBinding = binding

        val layoutManager = LinearLayoutManager(requireContext())
        binding.items.layoutManager = layoutManager

        binding.items.adapter  = adapter

        observeAll(layoutManager)
    }

    private fun observeAll(layoutManager: LinearLayoutManager) {
        viewModel.loading.observe(viewLifecycleOwner){
            fragmentNotificationBinding.loading.visible = it
        }

        viewModel.error.observe(viewLifecycleOwner){

        }

        fragmentNotificationBinding.items.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItem: Int = layoutManager.itemCount
                    val lastVisibleItem: Int = layoutManager.findLastVisibleItemPosition()
                    if (!isNotificationLoading && lastVisibleItem == totalItem - 1) {
                        isNotificationLoading = true
                        when(type){
                            All -> viewModel.loadNotification(typeString)
                            Followers -> viewModel.loadNotificationFollowers(typeString)
                            Comments -> viewModel.loadNotificationComments(typeString)
                            Likes -> viewModel.loadNotificationLikes(typeString)
                        }
                    }
                }
            }
        })

        fragmentNotificationBinding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh(type)
            fragmentNotificationBinding.swipeRefresh.isRefreshing = false
            val tmpSize = adapter.items.size
            adapter.items.clear()
            adapter.notifyItemRangeRemoved(0, tmpSize)
        }

        fragmentNotificationBinding.items.setOnItemClickListener { view, position, x, y ->
            val avatar: ImageView = view.findViewById(R.id.avatar)
            if(avatar.left<x && avatar.measuredWidth + avatar.left>x &&
                avatar.top<y && avatar.measuredHeight + avatar.top>y){
                requireContext().openProfile(adapter.items[position].userId)
            }else{
                when (adapter.items[position].type) {
                    "COMMENT" -> {
                        startActivity(
                            SinglePostViewerActivity.newIntent(
                                requireContext(),
                                adapter.items[position].comment?.postId?.toLong() ?: 0))
                    }
                    "FOLLOW" -> {
                        requireContext().openProfile(adapter.items[position].userId)
                    }
                    "REACTION" -> {
                        startActivity(
                            SinglePostViewerActivity.newIntent(
                                requireContext(),
                                adapter.items[position].reaction?.postId?.toLong() ?: 0))
                    }
                }
            }
        }



        when(type){
            All -> viewModel.notifications.observe(viewLifecycleOwner){
                fragmentNotificationBinding.swipeRefresh.isRefreshing = false
                if(it != null){
                    val tmpSize = adapter.items.size
                    adapter.items.addAll(it)
                    adapter.notifyItemRangeInserted(tmpSize, it.size)
                }
                isNotificationLoading = false
            }
            Followers -> viewModel.notificationsFollowers.observe(viewLifecycleOwner){
                fragmentNotificationBinding.swipeRefresh.isRefreshing = false
                if(it != null){
                    val tmpSize = adapter.items.size
                    adapter.items.addAll(it)
                    adapter.notifyItemRangeInserted(tmpSize, it.size)
                }
                isNotificationLoading = false
            }
            Comments -> viewModel.notificationsComment.observe(viewLifecycleOwner){
                fragmentNotificationBinding.swipeRefresh.isRefreshing = false
                if(it != null){
                    val tmpSize = adapter.items.size
                    adapter.items.addAll(it)
                    adapter.notifyItemRangeInserted(tmpSize, it.size)
                }
                isNotificationLoading = false
            }
            Likes -> viewModel.notificationsReaction.observe(viewLifecycleOwner){
                fragmentNotificationBinding.swipeRefresh.isRefreshing = false
                if(it != null){
                    val tmpSize = adapter.items.size
                    adapter.items.addAll(it)
                    adapter.notifyItemRangeInserted(tmpSize, it.size)
                }
                isNotificationLoading = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.lastIdAll = 0
        viewModel.lastIdReaction = 0
        viewModel.lastIdComment = 0
        viewModel.lastIdFollow = 0
        adapter.items.clear()
        viewModel.notifications.value?.clear()
    }
}