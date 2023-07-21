package org.linkmessenger.profile.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import io.realm.RealmList
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.data.local.entity.Media
import org.linkmessenger.data.local.entity.getSmallPhoto
import org.linkmessenger.data.local.entity.getThumbnail
import org.thoughtcrime.securesms.conversation.colors.AvatarColor
import org.thoughtcrime.securesms.util.BlurTransformation

class PostMediasAdapter(val context: Context, val postViewType: PostViewType, val items: RealmList<Media>): PagerAdapter() {
    override fun getCount(): Int {
        return items.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object`
    }
    @SuppressLint("InflateParams")
    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(context)
        imageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
        val md = items[position]
        val thumbnailRequest = Glide.with(context)
            .load(md!!.getThumbnail())
            .transform(BlurTransformation(context, 0.12f, BlurTransformation.MAX_RADIUS))
        Glide.with(context)
            .load(md.getSmallPhoto(postViewType))
            .thumbnail(thumbnailRequest)
            .dontAnimate()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .downsample(DownsampleStrategy.CENTER_INSIDE)
            .into(imageView)

        val vp = container as ViewPager
        vp.addView(imageView, 0)
        return imageView
    }
    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }
}