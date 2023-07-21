package org.linkmessenger.updateinfo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import org.linkmessenger.updateinfo.models.UpdateInfo
import org.thoughtcrime.securesms.R


class UpdateInfoAdapter(val mContext:Context, val infos: ArrayList<UpdateInfo>) : PagerAdapter() {
    override fun getCount(): Int {
        return infos.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(mContext)
        val view = inflater.inflate(R.layout.fragment_update_info_item, container, false) as ViewGroup
        val title: TextView = view.findViewById(R.id.update_title)
        val desc: TextView = view.findViewById(R.id.update_desc)
        val image: ImageView = view.findViewById(R.id.image)
        val info = infos.get(position)

        title.text = info.title
        desc.text = info.description

        image.clipToOutline = true

        mContext.let {
            Glide.with(it)
                    .load(info.image)
                    .into(image)
        }

        container.addView(view)
        return view
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
}