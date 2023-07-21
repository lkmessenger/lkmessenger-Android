package org.linkmessenger.notifications.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.linkmessenger.notifications.views.fragments.NotificationFragment

class NotificationTabAdapter(manager: FragmentManager, lifecycle: Lifecycle, val pageCount: Int): FragmentStateAdapter(manager, lifecycle) {
    override fun getItemCount(): Int {
        return pageCount
    }

    override fun createFragment(position: Int): Fragment {
        val type: NotificationTabType

        type = when (position) {
            0 -> {
                NotificationTabType.All}
            1 -> {
                NotificationTabType.Followers}
            2 -> {NotificationTabType.Comments}
            3 -> {NotificationTabType.Likes}
            else -> {
                NotificationTabType.All}
        }

        return NotificationFragment.newFragment(type)
    }
}