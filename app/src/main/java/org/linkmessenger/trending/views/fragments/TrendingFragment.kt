package org.linkmessenger.trending.views.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageButton
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.linkmessenger.openProfile
import org.linkmessenger.profile.adapters.UsersAdapter
import org.linkmessenger.profile.adapters.UsersListType
import org.linkmessenger.profile.view.fragments.FollowingAndFollowersFragment
import org.linkmessenger.profile.viewmodel.FollowersAndFollowingViewModel
import org.linkmessenger.showError
import org.linkmessenger.trending.viewmodels.TrendingViewModel
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.FragmentTrendingBinding
import org.thoughtcrime.securesms.util.visible

class TrendingFragment : Fragment(R.layout.fragment_trending), KoinComponent {

    private var fragmentTrendingBinding: FragmentTrendingBinding? = null
    private var usersAdapter: UsersAdapter?=null
    var isLoading = false
    var count = 0
    var type: UsersListType = UsersListType.TrendToday

    var viewModel: TrendingViewModel = get()

    companion object{
        fun newFragment(type: UsersListType): TrendingFragment {
            val params = bundleOf("type" to type)
            return TrendingFragment().apply {
                arguments = params
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        type = (arguments?.get("type") ?: UsersListType.TrendToday) as UsersListType
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentTrendingBinding.bind(view)
        fragmentTrendingBinding = binding

        binding.items.layoutManager = LinearLayoutManager(requireContext())

        usersAdapter = UsersAdapter(type=type)
        binding.items.adapter = usersAdapter

        when (type){
            UsersListType.TrendToday -> {
                viewModel.loadToday()
                viewModel.trendToday.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
                        val temp = usersAdapter!!.items.size
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
                }
            }
            UsersListType.TrendWeek -> {
                viewModel.loadWeek()
                viewModel.trendWeek.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
//                        usersAdapter?.items?.clear()
//                        usersAdapter?.notifyDataSetChanged()
                        val temp = usersAdapter!!.items.size
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
                }
            }
            UsersListType.TrendMonth -> {
                viewModel.loadMonth()

                viewModel.trendMonth.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
                        val temp = usersAdapter!!.items.size
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
                }
            }
            // Message Request
            UsersListType.TrendAll -> {
                viewModel.loadAll()

                viewModel.trendAll.observe(viewLifecycleOwner){
                    if(usersAdapter!=null){
                        val temp = usersAdapter!!.items.size
                        usersAdapter!!.items.addAll(it)
                        usersAdapter!!.notifyItemRangeInserted(temp, it.size)
                    }
                    isLoading = false
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

            if(actionButton.left < x && actionButton.measuredWidth + actionButton.left > x &&
                actionButton.top < y && actionButton.measuredHeight + actionButton.top > y){
                if(user != null){
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
            } else{
                requireContext().openProfile(user!!.id)
            }
        }

        binding.items.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager?
                if (!isLoading) {
                    if ((linearLayoutManager != null) && (usersAdapter != null) && (linearLayoutManager.findLastCompletelyVisibleItemPosition() == (usersAdapter!!.items.size - 1))) {
                        loadMore(type)
                        isLoading = true
                    }
                }
            }
        })

    }
    override fun onDestroyView() {
        fragmentTrendingBinding = null
        viewModel.trendAll.value?.clear()
        viewModel.trendWeek.value?.clear()
        viewModel.trendMonth.value?.clear()
        viewModel.trendToday.value?.clear()
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

    @SuppressLint("NotifyDataSetChanged")
    private fun loadMore(type: UsersListType) {
        when (type){
            UsersListType.TrendAll -> { viewModel.loadAll() }
            UsersListType.TrendMonth -> {viewModel.loadMonth()}
            UsersListType.TrendWeek -> { viewModel.loadWeek()}
            UsersListType.TrendToday -> {viewModel.loadToday()}
            else -> {}
        }
    }
}