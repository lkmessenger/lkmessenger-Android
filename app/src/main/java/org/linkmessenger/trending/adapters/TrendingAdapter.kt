package org.linkmessenger.trending.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import org.linkmessenger.profile.adapters.UsersListType
import org.linkmessenger.profile.view.fragments.FollowingAndFollowersFragment
import org.linkmessenger.trending.views.fragments.TrendingFragment

class TrendingAdapter(manager: FragmentManager, lifecycle: Lifecycle, val pageCount: Int) : FragmentStateAdapter(manager, lifecycle) {
    override fun getItemCount(): Int {
        return pageCount
    }

    override fun createFragment(position: Int): Fragment {
        val usersListType: UsersListType

        usersListType = when (position) {
            0 -> {UsersListType.TrendToday}
            1 -> {
                UsersListType.TrendWeek
            }
            2 -> {
                UsersListType.TrendMonth
            }
            3 -> {
                UsersListType.TrendAll
            }
            else -> {UsersListType.TrendAll}
        }

        return TrendingFragment.newFragment(usersListType)
    }
}