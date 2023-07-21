package org.thoughtcrime.securesms.posts

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import org.thoughtcrime.securesms.R

class ImageSlideAdapter(private val context: Context, var imageList: ArrayList<Uri>,var onItemRemove: (index: Int) -> Unit) : PagerAdapter() {
    override fun getCount(): Int {
        return imageList.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View =  (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.image_slider_item, null)
        val sliderImage = view.findViewById<ImageView>(R.id.iv_images)
        val removeButton = view.findViewById<ImageButton>(R.id.remove)

        imageList[position].let {
            Glide.with(context)
                    .load(it)
                    .into(sliderImage);
        }
        removeButton.setOnClickListener {
            onItemRemove.invoke(position)
        }

        val vp = container as ViewPager
        vp.addView(view, 0)
        return view
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
}