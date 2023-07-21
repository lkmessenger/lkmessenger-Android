package org.linkmessenger.fire.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import org.linkmessenger.fire.views.fragments.FireItemFragment
import org.linkmessenger.request.models.PostData

class FireItemPagerAdapter(val posts:ArrayList<PostData> = arrayListOf(), fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun createFragment(position: Int): Fragment = FireItemFragment(posts[position])
    override fun getItemCount(): Int {
        return posts.size
    }
}