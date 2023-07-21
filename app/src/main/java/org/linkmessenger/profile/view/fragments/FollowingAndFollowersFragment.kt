package org.linkmessenger.profile.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.linkmessenger.openProfile
import org.linkmessenger.profile.adapters.UsersAdapter
import org.linkmessenger.profile.adapters.UsersListType
import org.linkmessenger.profile.view.activities.FollowingAndFollowersActivity
import org.linkmessenger.profile.viewmodel.FollowersAndFollowingViewModel
import org.linkmessenger.showError
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.FragmentFollowersBinding
import org.thoughtcrime.securesms.util.visible

class FollowingAndFollowersFragment : Fragment(R.layout.fragment_followers), KoinComponent, FollowingAndFollowersActivity.TabChangedListener {
    companion object{
        fun newFragment(type:UsersListType, isMyProfile: Boolean, userId: Int?, forRequests: Boolean):FollowingAndFollowersFragment{
            val params = bundleOf("type" to type, "is_self" to isMyProfile, "user_id" to userId, "for_requests" to forRequests)
            return FollowingAndFollowersFragment().apply {
                arguments = params
            }
        }
    }

    private var fragmentFollowersBinding: FragmentFollowersBinding? = null
    private var usersAdapter: UsersAdapter?=null
    var isLoading = false
    var count = 0
    var type:UsersListType = UsersListType.MessageRequest
    private var isMyProfile:Boolean = false
    var forRequests: Boolean = false
    var userId = 0

    var viewModel: FollowersAndFollowingViewModel = get()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        type = (arguments?.get("type") ?: UsersListType.MessageRequest) as UsersListType
        isMyProfile = arguments?.getBoolean("is_self") ?:false
        forRequests = arguments?.getBoolean("for_requests") ?: false
        userId = arguments?.getInt("user_id") ?:0

//        if(isMyProfile && forRequests){
//            viewModel.loadMessageRequests()
//            viewModel.loadSentMessageRequests()
//        }else{
//            viewModel.loadFollowers(isMyProfile, userId)
//            viewModel.loadSubscriptions(isMyProfile, userId)
//        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentFollowersBinding.bind(view)
        fragmentFollowersBinding = binding

        binding.items.layoutManager = LinearLayoutManager(requireContext())

        usersAdapter = UsersAdapter(type=type, isMyProfile=isMyProfile)
        binding.items.adapter = usersAdapter

        when (type){
            UsersListType.Followers -> {
                viewModel.loadFollowers(isMyProfile, userId)
                viewModel.followers.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
//                        usersAdapter?.items?.clear()
//                        usersAdapter?.notifyDataSetChanged()
                        val temp = usersAdapter!!.items.size
//                        usersAdapter?.notifyDataSetChanged()
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
                }

                viewModel.followersEmptyState.observe(viewLifecycleOwner){
                    binding.emptyState.visible = it
                    binding.items.visible = !it
                }
            }
            UsersListType.Subscriptions -> {
                viewModel.loadSubscriptions(isMyProfile, userId)
                viewModel.subscriptions.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
//                        usersAdapter?.items?.clear()
//                        usersAdapter?.notifyDataSetChanged()
                        val temp = usersAdapter!!.items.size
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
                }
                viewModel.subscriptionsEmptyState.observe(viewLifecycleOwner){
                    binding.emptyState.visible = it
                    binding.items.visible = !it
                }
            }
            UsersListType.Recommendations -> {
                viewModel.loadRecommendations(userId)

                viewModel.recommendations.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
                        usersAdapter!!.items.addAll(it!!)
                        usersAdapter!!.notifyItemRangeInserted(0, it.size)
                    }
                    isLoading = false
                }
                viewModel.recommendationsEmptyState.observe(viewLifecycleOwner){
                    binding.emptyState.visible = it
                    binding.items.visible = !it
                }
            }
            // Message Request
            UsersListType.MessageRequest -> {
                viewModel.loadMessageRequests()

                viewModel.messageRequests.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
                        val temp = usersAdapter!!.items.size
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
                }
                viewModel.messageRequestsEmptyState.observe(viewLifecycleOwner){
                    binding.emptyState.visible = it
                    binding.items.visible = !it
                }
            }
            // Sent Message Request
            UsersListType.SentMessageRequest -> {
                viewModel.loadSentMessageRequests()

                viewModel.sentMessageRequests.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
                        val temp = usersAdapter!!.items.size
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
                }
                viewModel.sentMessageRequestsEmptyState.observe(viewLifecycleOwner){
                    binding.emptyState.visible = it
                    binding.items.visible = !it
                }
            }
            else -> {}
        }

        viewModel.error.observe(viewLifecycleOwner){
            it?.let { it1 ->
                binding.root.showError(it1)
                viewModel.error.postValue(null)
            }
        }

        viewModel.loading.observe(viewLifecycleOwner){
            binding.progress.visible = it
        }

        binding.items.setOnItemClickListener { itemView, position, x, y ->
            val user = usersAdapter?.items?.get(position)
            val actionButton: MaterialButton = itemView.findViewById(R.id.action)
            val denyButton: ImageButton = itemView.findViewById(R.id.deny)
            val acceptButton: ImageButton = itemView.findViewById(R.id.accept)

            if(actionButton.left < x && actionButton.measuredWidth + actionButton.left > x &&
                    actionButton.top < y && actionButton.measuredHeight + actionButton.top > y){
                if(user != null && type != UsersListType.SentMessageRequest && type != UsersListType.Followers){
                    if(user.isSubscribed == true){
                        showYesNoDialog{
                            viewModel.unSubscribe(user.id)
                            user.isSubscribed = false
                            usersAdapter!!.notifyItemChanged(position)
                        }
                    }else{
                        viewModel.subscribe(user.id)
                        user.isSubscribed = true
                        usersAdapter!!.notifyItemChanged(position)
                    }
                }else if(user != null && type == UsersListType.Followers){
                    if(isMyProfile){
                        showYesNoDialogRemoveUser {
                            viewModel.deleteFollower(user.id)
                            user.isFollower = false
                            usersAdapter?.items?.removeAt(position)
                            usersAdapter?.notifyItemRemoved(position)
                        }
                    }else{
                        if(user.isSubscribed == true){
                            showYesNoDialog{
                                viewModel.unSubscribe(user.id)
                                user.isSubscribed = false
                                usersAdapter!!.notifyItemChanged(position)
                            }
                        }else{
                            viewModel.subscribe(user.id)
                            user.isSubscribed = true
                            usersAdapter!!.notifyItemChanged(position)
                        }
                    }
                }
            }else if(denyButton.left<x && denyButton.measuredWidth + denyButton.left>x &&
                    denyButton.top<y && denyButton.measuredHeight + denyButton.top>y){
                viewModel.denyRequest(usersAdapter!!.items[position]!!.messageRequestId)
                usersAdapter!!.items.removeAt(position)
                usersAdapter!!.notifyItemRemoved(position)
            }else if(acceptButton.left < x && acceptButton.measuredWidth + acceptButton.left > x &&
                            acceptButton.top < y && acceptButton.measuredHeight + acceptButton.top > y){
                viewModel.acceptRequest(usersAdapter!!.items[position]!!.messageRequestId)
                usersAdapter!!.items.removeAt(position)
                usersAdapter!!.notifyItemRemoved(position)
            } else{
                requireContext().openProfile(user!!.id)
            }
        }
        if(type != UsersListType.Recommendations){
            binding.items.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                    if (!isLoading) {
                        if ((linearLayoutManager != null) && (usersAdapter != null) && (linearLayoutManager.findLastCompletelyVisibleItemPosition() == (usersAdapter!!.items.size - 1))) {
                            loadMore(type, userId)
                            isLoading = true
                        }
                    }
                }
            })
        }
    }
    override fun onDestroyView() {
        fragmentFollowersBinding = null
        viewModel.followers.value?.clear()
        viewModel.subscriptions.value?.clear()
        viewModel.messageRequests.value?.clear()
        super.onDestroyView()
    }

    private fun showYesNoDialog(onUnsubscribed:() -> Unit){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.unfollow_title))
        builder.setPositiveButton(R.string.yes) { _, _ -> onUnsubscribed.invoke() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(getString(R.string.unfollow_message))
        val dialog = builder.create()
        dialog.show()
    }

    private fun showYesNoDialogRemoveUser(onUnsubscribed:() -> Unit){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.remove_user))
        builder.setPositiveButton(R.string.yes) { _, _ -> onUnsubscribed.invoke() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(getString(R.string.remove_user_message))
        val dialog = builder.create()
        dialog.show()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMore(type: UsersListType, userId: Int?) {
        when (type){
            UsersListType.Followers -> { viewModel.loadFollowers(isMyProfile, userId) }
            UsersListType.Subscriptions -> {viewModel.loadSubscriptions(isMyProfile, userId)}
            UsersListType.Recommendations -> { }
            UsersListType.MessageRequest -> {viewModel.loadMessageRequests()}
            UsersListType.SentMessageRequest -> {viewModel.loadSentMessageRequests()}
            else -> {}
        }
    }

    override fun onTabChanged() {
//        val tmpSize = usersAdapter!!.items.size
//        usersAdapter!!.items.clear()
//        usersAdapter!!.notifyItemRangeRemoved(0, tmpSize)
    }
}