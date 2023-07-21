package org.linkmessenger.profile.view.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.get
import org.linkmessenger.openProfile
import org.linkmessenger.profile.adapters.UsersAdapter
import org.linkmessenger.profile.adapters.UsersListType
import org.linkmessenger.profile.viewmodel.ProfileViewModel
import org.linkmessenger.showError
import org.linkmessenger.trending.viewmodels.TrendingViewModel
import org.linkmessenger.trending.views.fragments.TrendingFragment
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityPostHiddenUsersBinding
import org.thoughtcrime.securesms.databinding.ActivityTrendingBinding
import org.thoughtcrime.securesms.databinding.FragmentTrendingBinding
import org.thoughtcrime.securesms.util.visible

class PostHiddenUsersActivity : AppCompatActivity() {
    private var binding: ActivityPostHiddenUsersBinding? = null
    private var usersAdapter: UsersAdapter?=null
    var isLoading = false
    var count = 0
    var type: UsersListType = UsersListType.BlockedUsers

    var viewModel: ProfileViewModel = get()

    companion object{
        fun newIntent(context: Context): Intent {
            return Intent(context, PostHiddenUsersActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_hidden_users)

        binding = ActivityPostHiddenUsersBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        supportActionBar?.hide()

        binding?.toolbar?.setNavigationOnClickListener{
            onBackPressed()
        }

        binding?.items?.layoutManager = LinearLayoutManager(this)

        usersAdapter = UsersAdapter(type=type)
        binding?.items?.adapter = usersAdapter

        viewModel.loadBlockedUsers()
        viewModel.blockedUsers.observe(this){
            if(usersAdapter!=null && it != null){
                val temp = usersAdapter!!.items.size
                usersAdapter!!.items.addAll(it)
                usersAdapter!!.notifyItemRangeInserted(temp, it.size)
            }
            isLoading = false
        }

        viewModel.loading.observe(this){
            binding?.progress?.visible = it
        }

        viewModel.error.observe(this){
            it?.let { it1 ->
                binding?.root?.showError(it1)
                viewModel.error.postValue(null)
            }
        }

        binding?.items?.setOnItemClickListener { itemView, position, x, y ->
            val user = usersAdapter?.items?.get(position)
            val actionButton: MaterialButton = itemView.findViewById(R.id.action)

            if(actionButton.left < x && actionButton.measuredWidth + actionButton.left > x &&
                actionButton.top < y && actionButton.measuredHeight + actionButton.top > y){
                if(user != null){
                    showYesNoDialog {
                        viewModel.unblockUser(user.id)
                        usersAdapter?.items?.removeAt(position)
                        usersAdapter?.notifyItemRemoved(position)
                    }
                }
            } else{
                this.openProfile(user!!.id)
            }
        }
    }
    private fun showYesNoDialog(onUnsubscribed:() -> Unit){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(getString(R.string.unblock_title))
        builder.setPositiveButton(R.string.yes) { _, _ -> onUnsubscribed.invoke() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(getString(R.string.unblock_message))
        val dialog = builder.create()
        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.blockedUsers.value?.clear()
    }
}