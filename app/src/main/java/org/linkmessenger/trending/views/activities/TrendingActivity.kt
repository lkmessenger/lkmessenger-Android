package org.linkmessenger.trending.views.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import org.koin.android.ext.android.get
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.profile.adapters.FollowingAndFollowersAdapter
import org.linkmessenger.profile.view.activities.FollowingAndFollowersActivity
import org.linkmessenger.profile.viewmodel.ProfileViewModel
import org.linkmessenger.trending.adapters.TrendingAdapter
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityFollowingAndFollowersBinding
import org.thoughtcrime.securesms.databinding.ActivityTrendingBinding
import org.thoughtcrime.securesms.recipients.Recipient

class TrendingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTrendingBinding

    companion object{
        fun newIntent(context: Context): Intent {
            return Intent(context, TrendingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrendingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener{
            onBackPressed()
        }


        val tabTitles: ArrayList<Int> =
            arrayListOf(R.string.today, R.string.week, R.string.monthly, R.string.all)


        for (it in tabTitles) {
            binding.tabLayout.addTab(binding.tabLayout.newTab())
        }

        binding.viewpager.adapter = TrendingAdapter(supportFragmentManager, lifecycle, tabTitles.size)

        TabLayoutMediator(binding.tabLayout, binding.viewpager) { tab, position ->
            tab.text = getString(tabTitles[position])
        }.attach()

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {}
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })
    }

}