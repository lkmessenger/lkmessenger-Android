package org.linkmessenger.profile.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.profile.adapters.FollowingAndFollowersAdapter
import org.linkmessenger.profile.view.fragments.FollowingAndFollowersFragment
import org.linkmessenger.profile.viewmodel.ProfileViewModel
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityFollowingAndFollowersBinding
import org.thoughtcrime.securesms.recipients.Recipient

class FollowingAndFollowersActivity : AppCompatActivity(), KoinComponent {
    private lateinit var binding: ActivityFollowingAndFollowersBinding
    private var isMyProfile:Boolean = true
    private var selectedPosition:Int = 0
    private var forRequests: Boolean = false
    private var userId: Int? = null
    private var listener: TabChangedListener? = null
    private var viewModel: ProfileViewModel = get()
    companion object{
        fun newIntent(context: Context, isMyProfile:Boolean, selectedPosition:Int, userId:Int, forRequests: Boolean):Intent{
            return Intent(context, FollowingAndFollowersActivity::class.java).apply {
                putExtra("is_my_profile", isMyProfile)
                putExtra("selected_position", selectedPosition)
                putExtra("user_id", userId)
                putExtra("for_requsts", forRequests)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFollowingAndFollowersBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        this.changeStatusBarColor()
        supportActionBar?.hide()

        isMyProfile = intent.getBooleanExtra("is_my_profile", true)
        selectedPosition = intent.getIntExtra("selected_position", 0)
        forRequests = intent.getBooleanExtra("for_requsts", false)
        userId = intent.getIntExtra("user_id", -1)
        if (!isMyProfile){
            viewModel.loadProfile(userId)
            viewModel.profileData.observe(this){
                binding.title.text = it?.username.toString().ifEmpty {
                    it?.profileName
                }
                if(it?.isVerified == true){
                    binding.title.setCompoundDrawablesWithIntrinsicBounds(null, null, this.getUserBadge(3), null)
                }else{
                    binding.title.setCompoundDrawablesWithIntrinsicBounds(null, null, this.getUserBadge(it?.type), null)
                }
            }
        }else{
            binding.title.text = Recipient.self().getDisplayName(this)
        }


        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }


        val tabTitles: ArrayList<Int>
        if(isMyProfile && forRequests){
            tabTitles = arrayListOf(R.string.chat_requests, R.string.sent_chat_requests)
        }else {
            tabTitles = arrayListOf(R.string.followers, R.string.following, R.string.link_recommendations)
        }

        for (it in tabTitles) {
            binding.tabLayout.addTab(binding.tabLayout.newTab())
        }

        binding.viewpager.adapter = FollowingAndFollowersAdapter(supportFragmentManager, lifecycle, tabTitles.size, isMyProfile, userId, forRequests)

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = getString(tabTitles[position])
        }.attach()
        binding.tabLayout.selectTab(binding.tabLayout.getTabAt(selectedPosition))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                listener?.onTabChanged()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

//    fun setActivityListener(activityListener: FollowingAndFollowersFragment) {
//        listener = activityListener
//    }
    interface TabChangedListener {
        fun onTabChanged()
    }
}