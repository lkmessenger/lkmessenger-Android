package org.linkmessenger.profile.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.firebase.crashlytics.internal.model.CrashlyticsReport.Session.User
import org.linkmessenger.profile.view.fragments.FollowingAndFollowersFragment

class FollowingAndFollowersAdapter(manager: FragmentManager, lifecycle: Lifecycle,val pageCount: Int,val isMyProfile: Boolean, val userId: Int?, val forRequests: Boolean) : FragmentStateAdapter(manager, lifecycle) {
    override fun getItemCount(): Int {
        return pageCount
    }

    override fun createFragment(position: Int): Fragment {
        val usersListType: UsersListType
        if(this.isMyProfile && this.forRequests){
            usersListType = when (position) {
                0 -> {UsersListType.MessageRequest}
                1 -> {UsersListType.SentMessageRequest}
                else -> {UsersListType.Followers}
            }
        }else{
            usersListType = when (position) {
                0 -> {UsersListType.Followers}
                1 -> {
                    UsersListType.Subscriptions
                }
                2 -> {
                    UsersListType.Recommendations
                }
                else -> {UsersListType.Followers}
            }
        }

        return FollowingAndFollowersFragment.newFragment(usersListType, isMyProfile, userId, forRequests)
    }
}