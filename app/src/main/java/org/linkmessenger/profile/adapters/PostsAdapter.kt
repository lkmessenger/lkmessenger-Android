package org.linkmessenger.profile.adapters

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.base.ui.cells.DescLinkCell
import org.linkmessenger.base.ui.components.post.PostItemCell
import org.linkmessenger.base.ui.components.post.PostOpenType
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.data.local.entity.getSmallPhoto
import org.linkmessenger.data.local.entity.getThumbnail
import org.linkmessenger.request.models.PostData
import org.linkmessenger.request.models.getSmallPhoto
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.SquareImageView
import org.thoughtcrime.securesms.util.BlurTransformation
import org.thoughtcrime.securesms.util.visible


class PostsAdapter(val context: Context, private val postViewType: PostViewType) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val items:ArrayList<Post> = ArrayList()
    companion object{
//        private const val VIEW_TYPE_ITEM = 0
        private const val VIEW_TYPE_LOADING = -1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_LOADING){
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_item_loading, parent, false)
            ItemLoadingViewHolder(view)
        }else{
            if(postViewType==PostViewType.Grid){
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.post_mini_item, parent, false)

                ItemViewHolder(view)
            }else{
                val view = PostItemCell(context, PostOpenType.List)
                ItemViewHolder(view)
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            populateItemRows(holder, position)
        } else if (holder is ItemLoadingViewHolder) {
            showLoadingView(holder, position)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        if(holder.itemView is PostItemCell) {
            if (holder.itemView.data != null && holder.itemView.data?.medias!!.size == 1) {
                val md = holder.itemView.data!!.medias!![0]
                if (md!!.type == 2) {
                    holder.itemView.player?.release()
                }
            }
            holder.itemView.hideInfoView()
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        if(holder.itemView is PostItemCell) {
            try {
                if (holder.itemView.data != null && holder.itemView.data?.medias!!.size == 1) {
                    val md = holder.itemView.data!!.medias!![0]
                    if (md!!.type == 2) {
                        holder.itemView.player = ExoPlayer.Builder(context).build()
                        holder.itemView.player?.addListener(object: Player.Listener{
                            override fun onPlaybackStateChanged(playbackState: Int) {
                                when (playbackState) {
                                    ExoPlayer.STATE_ENDED -> {
                                        holder.itemView.player?.seekTo(0)
                                        holder.itemView.player?.pause()
                                        Handler(Looper.getMainLooper()).postDelayed({
                                            holder.itemView.sliderOneView.visibility = View.VISIBLE
                                            holder.itemView.playerView.visibility = GONE
                                            holder.itemView.onLoading(false)
                                            val icon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_arrow_clockwise_48_filled)
                                            Theme.setDrawableColor(icon, ContextCompat.getColor(context, R.color.signal_play_pause))
                                            holder.itemView.playButton.setImageDrawable(icon)
                                        }, 200)
                                    }
                                    ExoPlayer.STATE_BUFFERING -> {
                                        holder.itemView.onLoading(true)
                                    }
                                    else -> {
                                        holder.itemView.onLoading(false)
                                    }
                                }
                            }

                            override fun onIsPlayingChanged(isPlaying: Boolean) {
                                if(isPlaying){
                                    holder.itemView.onVideoPlay()
                                }else{
                                    holder.itemView.onVideoPause()
                                }
                            }

                            override fun onPlayerError(error: PlaybackException) {
                                super.onPlayerError(error)
                            }

                            override fun onPlayerErrorChanged(error: PlaybackException?) {
                                super.onPlayerErrorChanged(error)
                            }
                        })
                        holder.itemView.playerView.useController = false
                        holder.itemView.playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                        holder.itemView.playerView.player = holder.itemView.player
                        holder.itemView.player?.setMediaItem(MediaItem.fromUri(md.url))
                        holder.itemView.player?.prepare()
                    }
                }
            }catch (e:Exception){
                FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if(holder.itemView is PostItemCell) {
            Glide.with(context).clear(holder.itemView.sliderOneView)
            Glide.with(context).clear(holder.itemView.avatarView)
            holder.itemView.sliderView.adapter = null
            holder.itemView.player?.release()
            holder.itemView.allCommentsView.text = null
            holder.itemView.dateView.text = null
            holder.itemView.countView.text = null
            holder.itemView.headerView.text = null
            holder.itemView.infoLabel.text = null
            holder.itemView.infoText.text = null
        }else if(holder is ItemViewHolder){
            holder.imageView?.let { Glide.with(context).clear(it) }
            holder.multiplyView?.setImageDrawable(null)
        }
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: SquareImageView? = itemView.findViewById(R.id.image)
        val multiplyView: ImageView? = itemView.findViewById(R.id.multiply)
    }
    class ItemLoadingViewHolder(ItemView: View) : RecyclerView.ViewHolder(ItemView) {
        val progressBar: CircularProgressIndicator = itemView.findViewById(R.id.progressBar)
    }

    private fun showLoadingView(viewHolder: ItemLoadingViewHolder, position: Int) {
        //ProgressBar would be displayed
    }

    private fun populateItemRows(viewHolder: ItemViewHolder, position: Int) {
        val context = viewHolder.itemView.context
        val item = items[position]
        if(postViewType==PostViewType.Grid){
            if(item.medias?.isNotEmpty() == true){
                val md = item.medias!![0]
                viewHolder.imageView?.let {
                    if(md!!.type==2){
                        Glide.with(context)
                            .load(md.preload_photo)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .downsample(DownsampleStrategy.CENTER_INSIDE)
                            .into(it)
                    }else{
                        val thumbnailRequest = Glide.with(context)
                            .load(md.getThumbnail())
                            .transform(BlurTransformation(context, 0.12f, BlurTransformation.MAX_RADIUS))

                        Glide.with(context)
                            .load(md.getSmallPhoto(postViewType))
                            .thumbnail(thumbnailRequest)
                            .dontAnimate()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .downsample(DownsampleStrategy.CENTER_INSIDE)
                            .into(it)
                    }
                }
            }

            if(item.medias?.size!=1){
                viewHolder.multiplyView?.visible = true
                viewHolder.multiplyView?.setImageResource(R.drawable.ic_fluent_square_multiple_20_filled)
            }else{
                val v = item.medias?.get(0)
                if(v!=null && v.type==2){
                    viewHolder.multiplyView?.visible = true
                    viewHolder.multiplyView?.setImageResource(R.drawable.ic_fluent_video_20_filled)
                }else{
                    viewHolder.multiplyView?.visible = false
                }
            }
        }else{
            if(viewHolder.itemView is PostItemCell){
                viewHolder.itemView.setData(item, postViewType)
            }
        }
    }
}
enum class PostItemClick{
    Root, Like, Comment, Share, Save
}