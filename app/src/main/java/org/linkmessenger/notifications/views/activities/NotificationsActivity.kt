package org.linkmessenger.notifications.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.linkmessenger.notifications.adapters.NotificationTabAdapter
import org.linkmessenger.profile.view.activities.FollowingAndFollowersActivity
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityNotificationsBinding

class NotificationsActivity : PassphraseRequiredActivity() {
    private lateinit var binding: ActivityNotificationsBinding
    private var listener: FollowingAndFollowersActivity.TabChangedListener? = null

    companion object{
        fun newIntent(context: Context): Intent {
            return Intent(context, FollowingAndFollowersActivity::class.java).apply {}
        }
    }

    override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
        super.onCreate(savedInstanceState, ready)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener{ onBackPressed() }

        val tabTitles: ArrayList<Int> = arrayListOf(R.string.MediaOverviewActivity_All, R.string.new_followers, R.string.comments, R.string.likes)

        for (it in tabTitles) {
            binding.tabLayout.addTab(binding.tabLayout.newTab())
        }

        binding.viewpager.adapter = NotificationTabAdapter(supportFragmentManager, lifecycle, tabTitles.size)

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = getString(tabTitles[position])
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                listener?.onTabChanged()
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }
}