package org.linkmessenger.base.ui.components.post

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Animatable2
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieDrawable.RepeatMode
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.MainScope
import org.linkmessenger.base.ui.AndroidUtilities
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.base.ui.cells.DescLinkCell
import org.linkmessenger.base.ui.components.BottomPagesView
import org.linkmessenger.base.ui.components.SimpleTextView
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.data.local.entity.getSmallPhoto
import org.linkmessenger.data.local.entity.getThumbnail
import org.linkmessenger.profile.adapters.PostMediasAdapter
import org.linkmessenger.utils.ContextUtils.getUserBadge
import org.linkmessenger.utils.PostUtil
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.contacts.avatars.GeneratedContactPhoto
import org.thoughtcrime.securesms.contacts.avatars.ResourceContactPhoto
import org.thoughtcrime.securesms.conversation.colors.AvatarColor
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.util.BlurTransformation
import org.thoughtcrime.securesms.util.DateUtils
import org.thoughtcrime.securesms.util.visible
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.roundToInt


@SuppressLint("ViewConstructor")
class PostItemCell(context: Context, private val openType: PostOpenType=PostOpenType.List): FrameLayout(context) {
    var data:Post?=null
    private var viewType = PostViewType.Grid
    var avatarView:ImageView
    var headerView:SimpleTextView
    var moreActionButton:ImageView
    var followButton: Button
    var sliderView: ViewPager
    var sliderOneView: ImageView
    var indicator: BottomPagesView
    var likeButton:ImageView

    var playerView: StyledPlayerView
    var player: ExoPlayer?=null
    var playButton: ImageView
    var progressBar: CircularProgressIndicator

    var likeAnimation:ImageView
    var animatedVectorDrawable: AnimatedVectorDrawable? = null
    var animatedVectorDrawableCompat: AnimatedVectorDrawableCompat? = null
    var likeAnimationDrawable: Drawable? = null

//    var waveAnimation: LottieAnimationView


    var countView:SimpleTextView
    var allCommentsView:SimpleTextView
    var dateView:SimpleTextView
    var infoLabel:SimpleTextView
    var infoText:SimpleTextView

    var commentButton:ImageView
    var shareButton:ImageView
    var saveButton:ImageView
    var infoBackground:View
    var downloadButton:ImageView
    var descCell:DescLinkCell
    private var pOr = 0
    private var l:Float= 16f/9f
    private var p:Float= 4f/5f
    private var sliderHeight:Int=1

    var postType = 1

//    private lateinit var dateView:SimpleTextView
    init {

//        var rippleDrawable = Theme.createSelectorDrawable(
//            ContextCompat.getColor(context, R.color.signal_colorSurface3),
//            Theme.RIPPLE_MASK_CIRCLE_20DP,
//            AndroidUtilities.dp(20f)
//        ) as RippleDrawable
//        Theme.setRippleDrawableForceSoftware(rippleDrawable)

        avatarView = ImageView(context)
        avatarView.scaleType = ImageView.ScaleType.CENTER
        avatarView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_fluent_person_circle_32_filled))

        avatarView.isFocusable = true
        addView(avatarView)

        headerView = SimpleTextView(context)
        headerView.textColor = ContextCompat.getColor(context, R.color.signal_colorNeutralInverse)
        headerView.setTextSize(16)
        headerView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"))
        addView(headerView)

        countView = SimpleTextView(context)
        countView.textColor = ContextCompat.getColor(context, R.color.signal_colorNeutralInverse)
        countView.setTextSize(16)
        countView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"))
        addView(countView)

        allCommentsView = SimpleTextView(context)
        allCommentsView.textColor = ContextCompat.getColor(context, R.color.signal_colorSecondary)
        allCommentsView.setTextSize(16)
//        allCommentsView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"))
        addView(allCommentsView)

        dateView = SimpleTextView(context)
        dateView.textColor = ContextCompat.getColor(context, R.color.signal_colorSecondary)
        dateView.setTextSize(12)
//      dateView.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"))
        addView(dateView)



        moreActionButton = ImageView(context)
        moreActionButton.scaleType = ImageView.ScaleType.CENTER
        val moreIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_more_vertical_24_regular)
        Theme.setDrawableColor(moreIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
        moreActionButton.setImageDrawable(moreIcon)
        addView(moreActionButton)

        sliderView = ViewPager(context)

        likeAnimation = ImageView(context)
        likeAnimationDrawable = ContextCompat.getDrawable(context, R.drawable.like_animation)
        likeAnimation.setImageDrawable(likeAnimationDrawable)
        likeAnimation.alpha = 0f
        likeAnimation.isVisible = false

        addView(sliderView)

//        waveAnimation = LottieAnimationView(context)
//        waveAnimation.setAnimation(R.raw.playing)
//        waveAnimation.repeatMode = LottieDrawable.RESTART
//        waveAnimation.loop(true)
//        waveAnimation.alpha = 0.7f
//        waveAnimation.visibility = View.GONE

        sliderOneView = ImageView(context)

        likeButton = ImageView(context)
        likeButton.scaleType = ImageView.ScaleType.CENTER
        val likeIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_heart_28_regular)
        Theme.setDrawableColor(likeIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
        likeButton.setImageDrawable(likeIcon)
        likeButton.isFocusable = true
        addView(likeButton)

        commentButton = ImageView(context)
        commentButton.scaleType = ImageView.ScaleType.CENTER
        val commentIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_chat_empty_28_regular)
        Theme.setDrawableColor(commentIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
        commentButton.setImageDrawable(commentIcon)
        addView(commentButton)

        shareButton = ImageView(context)
        shareButton.scaleType = ImageView.ScaleType.CENTER
        val shareIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_send_28_regular)
        Theme.setDrawableColor(shareIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
        shareButton.setImageDrawable(shareIcon)
        shareButton.isFocusable = true
        addView(shareButton)

        indicator = BottomPagesView(context, sliderView, 0)
        sliderView.addOnPageChangeListener(object :OnPageChangeListener{
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                indicator.setPageOffset(position, positionOffset)
            }

            override fun onPageSelected(position: Int) {

            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        addView(indicator)

        saveButton = ImageView(context)
        saveButton.scaleType = ImageView.ScaleType.CENTER
        val saveIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_bookmark_28_regular)
        Theme.setDrawableColor(saveIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
        saveButton.setImageDrawable(saveIcon)
        saveButton.isFocusable = true
        addView(saveButton)

        downloadButton = ImageView(context)
        downloadButton.scaleType = ImageView.ScaleType.CENTER
        val downloadIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_arrow_download_24_regular)
        Theme.setDrawableColor(downloadIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
        downloadButton.setImageDrawable(downloadIcon)
        downloadButton.isFocusable = true
        downloadButton.visibility = GONE
        addView(downloadButton)



        descCell = object : DescLinkCell(context){
            override fun didPressUrl(url: String?) {
                super.didPressUrl(url)
            }

            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
                invalidate()
            }
        }
        addView(descCell)

        playerView = StyledPlayerView(context)
        addView(playerView)

        addView(sliderOneView)

        playButton = ImageView(context)
        playButton.scaleType = ImageView.ScaleType.CENTER
        var playIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_play_48_filled)
        Theme.setDrawableColor(playIcon, ContextCompat.getColor(context, R.color.signal_play_pause))
        playButton.setImageDrawable(playIcon)
        addView(playButton)

        progressBar = CircularProgressIndicator(context)
        progressBar.isIndeterminate = true
        progressBar.trackCornerRadius = 2
        progressBar.indicatorSize = AndroidUtilities.dp(50f)
        progressBar.trackThickness = AndroidUtilities.dp(1f)
        progressBar.trackColor = ContextCompat.getColor(context, R.color.signal_colorTransparentInverse1)
        progressBar.setIndicatorColor(ContextCompat.getColor(context, R.color.signal_light_colorSecondaryBackground))
        progressBar.visibility = GONE
        addView(progressBar)

        followButton = Button(ContextThemeWrapper(context, R.style.Signal_Widget_Button_Medium_OutlinedButton), null, R.style.Signal_Widget_Button_Medium_OutlinedButton)
        followButton.isClickable = false
        followButton.setTextColor(ContextCompat.getColor(context, R.color.signal_colorPrimary))
        followButton.text = context.getString(R.string.follow)
        addView(followButton)

        if(openType==PostOpenType.Single){
            avatarView.visibility = GONE
            headerView.visibility = GONE
            moreActionButton.visibility = GONE
            likeButton.visibility = GONE
            commentButton.visibility = GONE
            shareButton.visibility = GONE
            saveButton.visibility = GONE
            downloadButton.visibility = GONE
            playerView.visibility = GONE
            playButton.visibility = GONE
        }

        addView(likeAnimation)

//        addView(waveAnimation)

        infoBackground = View(context)
        infoBackground.setBackgroundColor(ContextCompat.getColor(context, R.color.signal_colorPrimary))
        addView(infoBackground)
        infoBackground.visibility = GONE

        infoLabel = SimpleTextView(context)
        infoLabel.textColor = ContextCompat.getColor(context, R.color.white)
        infoLabel.setTextSize(16)
        infoLabel.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"))
        addView(infoLabel)
        infoLabel.visibility = GONE

        infoText = SimpleTextView(context)
        infoText.textColor = ContextCompat.getColor(context, R.color.white)
        infoText.setTextSize(14)
        infoText.setGravity(Gravity.CENTER_VERTICAL and Gravity.END)
        addView(infoText)
        infoText.visibility = GONE
    }

    fun animateLiked(){
        if(data!=null){
            likeAnimation.alpha = 0.8f
            likeAnimation.isVisible = true
            if(likeAnimationDrawable is AnimatedVectorDrawable){
                animatedVectorDrawable = likeAnimationDrawable as AnimatedVectorDrawable
                animatedVectorDrawable?.start()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    animatedVectorDrawable?.registerAnimationCallback(
                        object :
                            Animatable2.AnimationCallback() {
                            override fun onAnimationEnd(drawable: Drawable?) {
                                likeAnimation.isVisible = false
                                super.onAnimationEnd(drawable)
                            }
                        })
                }
            }else if(likeAnimationDrawable is AnimatedVectorDrawableCompat){
                animatedVectorDrawableCompat = likeAnimationDrawable as AnimatedVectorDrawableCompat
                animatedVectorDrawableCompat?.registerAnimationCallback(
                    object :
                        androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback() {
                        override fun onAnimationEnd(drawable: Drawable?) {
                            likeAnimation.isVisible = false
                            super.onAnimationEnd(drawable)
                        }
                    })
                animatedVectorDrawableCompat?.start()
            }
        }
    }
    fun changeSaved(savedStatus:Boolean) {
        if(!savedStatus){
            val saveIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_bookmark_28_regular)
            Theme.setDrawableColor(saveIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
            saveButton.setImageDrawable(saveIcon)
        }else{
            val saveIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_bookmark_28_filled)
            Theme.setDrawableColor(saveIcon, ContextCompat.getColor(context, R.color.signal_colorPrimary))
            saveButton.setImageDrawable(saveIcon)
        }
    }
    fun changeLiked(likedStatus:Boolean) {
        if(!likedStatus){
            val likeIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_heart_28_regular)
            Theme.setDrawableColor(likeIcon, ContextCompat.getColor(context, R.color.signal_colorNeutralInverse))
            likeButton.setImageDrawable(likeIcon)
        }else{
            val likeIcon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_heart_28_filled)
            Theme.setDrawableColor(likeIcon, ContextCompat.getColor(context, R.color.red))
            likeButton.setImageDrawable(likeIcon)
        }
    }

    fun postLikeEvent(isLike: Boolean){
        if(data==null) return
        if(isLike){
            data!!.likesCount++
        }else{
            data!!.likesCount--
        }
        initCountView()
    }
    fun hideInfoView(){
        infoBackground.visibility = GONE
        infoLabel.visibility = GONE
        infoText.visibility = GONE
    }
    fun setInfoText(label:String, text:String){
        if(infoBackground.visibility!= VISIBLE){
            infoBackground.visibility = VISIBLE
            infoLabel.visibility = VISIBLE
            infoText.visibility = VISIBLE
        }
        infoLabel.text = label
        infoText.text = text
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)

        sliderHeight = when (pOr) {
            2 -> {
                (width/l).roundToInt()
            }
            3 -> {
                (width/p).roundToInt()
            }
            else -> {
                width
            }
        }
        val indicatorWidth = (data?.medias?.size ?: 0) * AndroidUtilities.dp(11f)

        val height = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50 + 40f) + sliderHeight
        }else{
            if(data?.medias?.size==1){
                sliderHeight
            }else{
                AndroidUtilities.dp(40f) + sliderHeight
            }
        }

        avatarView.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(30f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(30f), MeasureSpec.EXACTLY))
        headerView.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(40 + 40 +8f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
            AndroidUtilities.dp(20f), MeasureSpec.EXACTLY))
        moreActionButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        sliderView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(sliderHeight, MeasureSpec.EXACTLY))
        sliderOneView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(sliderHeight, MeasureSpec.EXACTLY))
        playerView.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(sliderHeight, MeasureSpec.EXACTLY))
        likeAnimation.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(120f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(120f), MeasureSpec.EXACTLY))
//        waveAnimation.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(24f),MeasureSpec.EXACTLY))
        likeButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        commentButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        shareButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        indicator.measure(MeasureSpec.makeMeasureSpec(indicatorWidth, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(5f), MeasureSpec.EXACTLY))
        saveButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        downloadButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        infoBackground.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        infoLabel.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(16f) - AndroidUtilities.dp(60f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
            AndroidUtilities.dp(20f), MeasureSpec.EXACTLY))
        infoText.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(60f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
            AndroidUtilities.dp(18f), MeasureSpec.EXACTLY))
        playButton.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))
        progressBar.measure(MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(50f), MeasureSpec.EXACTLY))
        countView.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(32f), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
            AndroidUtilities.dp(20f), MeasureSpec.EXACTLY))
        allCommentsView.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(32f), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
            AndroidUtilities.dp(20f), MeasureSpec.EXACTLY))
        dateView.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(32f), MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
            AndroidUtilities.dp(20f), MeasureSpec.EXACTLY))

        followButton.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST),MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(40f), MeasureSpec.EXACTLY))


        descCell.checkTextLayout(
            width - AndroidUtilities.dp((8 + 8).toFloat()),
            false
        )
        val dHeight: Int = descCell.updateHeight()

        descCell.measure(MeasureSpec.makeMeasureSpec(width - AndroidUtilities.dp(16f), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(
            dHeight, MeasureSpec.EXACTLY))

        val cvHeight = if(data!=null && data?.likesCount!=0){
            AndroidUtilities.dp(28f)
        }else{
            AndroidUtilities.dp(10f)
        }

        val commentHeight = if(data!=null && data!!.commentsCount!=0){
            AndroidUtilities.dp(20f)
        }else{
            AndroidUtilities.dp(0f)
        }
        val dateHeight = AndroidUtilities.dp(20f)


        setMeasuredDimension(width, height + dHeight + cvHeight + commentHeight + dateHeight)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val height = bottom - top
        val width = right - left

        var viewTop = AndroidUtilities.dp(10f)
        var viewLeft = AndroidUtilities.dp(5f)

        if(openType==PostOpenType.List){
            avatarView.layout(viewLeft, viewTop,viewLeft + avatarView.measuredWidth,viewTop + avatarView.measuredHeight)

            viewTop = (AndroidUtilities.dp(50f) - headerView.textHeight) / 2
            viewLeft = AndroidUtilities.dp(40f)
            headerView.layout(viewLeft, viewTop,viewLeft + headerView.measuredWidth, viewTop + headerView.measuredHeight)

            viewTop = AndroidUtilities.dp(5f)
            viewLeft = width - AndroidUtilities.dp(40 + 8f)
            moreActionButton.layout(viewLeft, viewTop, viewLeft + moreActionButton.measuredWidth,viewTop + moreActionButton.measuredHeight)
        }

        viewLeft = moreActionButton.left - followButton.measuredWidth

        followButton.layout(viewLeft, viewTop,viewLeft + followButton.measuredWidth, viewTop + followButton.measuredHeight)


        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50f)
        }else{
            AndroidUtilities.dp(0f)
        }

        viewLeft = 0
        sliderView.layout(viewLeft, viewTop, width,viewTop + sliderHeight)
        sliderOneView.layout(viewLeft, viewTop, width,viewTop + sliderHeight)
        playerView.layout(viewLeft, viewTop, width,viewTop + sliderHeight)

        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50f) + sliderHeight - AndroidUtilities.dp(40f)
        }else{
            sliderHeight - AndroidUtilities.dp(40f)
        }
        infoBackground.layout(viewLeft, viewTop, width, viewTop + AndroidUtilities.dp(40f))

        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50f) + sliderHeight - AndroidUtilities.dp(30f)
        }else{
            sliderHeight - AndroidUtilities.dp(30f)
        }
        viewLeft = AndroidUtilities.dp(16f)
        infoLabel.layout(viewLeft, viewTop, viewLeft + infoLabel.measuredWidth, viewTop + infoLabel.measuredHeight)

        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50f) + sliderHeight - AndroidUtilities.dp(28f)
        }else{
            sliderHeight - AndroidUtilities.dp(28f)
        }
        viewLeft = width - infoText.measuredWidth - AndroidUtilities.dp(16f)
        infoText.layout(viewLeft, viewTop, viewLeft + infoText.measuredWidth, viewTop + infoText.measuredHeight)

        if(openType==PostOpenType.List){
            viewTop = AndroidUtilities.dp(50f) + sliderHeight
            viewLeft = AndroidUtilities.dp(8f)
            likeButton.layout(viewLeft, viewTop, viewLeft + likeButton.measuredWidth, viewTop + likeButton.measuredHeight)

            viewLeft = AndroidUtilities.dp(8f) + likeButton.measuredWidth
            commentButton.layout(viewLeft, viewTop, viewLeft + commentButton.measuredWidth, viewTop + commentButton.measuredHeight)

            viewLeft = AndroidUtilities.dp(8f) + likeButton.measuredWidth + commentButton.measuredWidth
            shareButton.layout(viewLeft, viewTop, viewLeft + shareButton.measuredWidth, viewTop + shareButton.measuredHeight)

            viewTop = AndroidUtilities.dp(50f) + sliderHeight
            viewLeft = width - AndroidUtilities.dp(40 + 8f)
            saveButton.layout(viewLeft, viewTop, viewLeft + saveButton.measuredWidth, viewTop + saveButton.measuredHeight)

            viewTop = AndroidUtilities.dp(51f) + sliderHeight
            viewLeft = width - AndroidUtilities.dp(80 + 16f)
            downloadButton.layout(viewLeft, viewTop, viewLeft + downloadButton.measuredWidth, viewTop + downloadButton.measuredHeight)
        }

        viewTop = if(openType==PostOpenType.List){
            (AndroidUtilities.dp(50f) + sliderView.measuredHeight) + ((AndroidUtilities.dp(40f) - indicator.measuredHeight)/2)
        }else{
            sliderView.measuredHeight - AndroidUtilities.dp(16f)
        }
        viewLeft = (width - indicator.measuredWidth)/2
        indicator.layout(viewLeft, viewTop, viewLeft + indicator.measuredWidth, viewTop + indicator.measuredHeight)


        val cvHeight = if(data!=null && data?.likesCount!=0){
            AndroidUtilities.dp(20f)
        }else{
            AndroidUtilities.dp(0f)
        }
        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50+40f) + sliderHeight + cvHeight
        }else{
            AndroidUtilities.dp(16f) + sliderHeight + cvHeight
        }
        viewLeft = AndroidUtilities.dp(8f)
        descCell.layout(viewLeft, viewTop,viewLeft + descCell.measuredWidth, viewTop + descCell.measuredHeight)

        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50+40f) + sliderHeight
        }else{
            AndroidUtilities.dp(16f) + sliderHeight
        }
        viewLeft = AndroidUtilities.dp(16f)
        countView.layout(viewLeft, viewTop,viewLeft + countView.measuredWidth, viewTop + countView.measuredHeight)

        if(data!=null && data!!.commentsCount!=0 && viewType == PostViewType.List){
            viewTop = if(openType==PostOpenType.List){
                AndroidUtilities.dp(50+40f) + sliderHeight + cvHeight + descCell.measuredHeight
            }else{
                AndroidUtilities.dp(16f) + sliderHeight + sliderHeight + cvHeight + descCell.measuredHeight
            }
            viewLeft = AndroidUtilities.dp(16f)
            allCommentsView.layout(viewLeft, viewTop,viewLeft + allCommentsView.measuredWidth, viewTop + allCommentsView.measuredHeight)

            viewTop += AndroidUtilities.dp(20f)
            viewLeft = AndroidUtilities.dp(16f)
            dateView.layout(viewLeft, viewTop,viewLeft + dateView.measuredWidth, viewTop + dateView.measuredHeight)
        }else{
            viewTop = if(openType==PostOpenType.List){
                AndroidUtilities.dp(50+40f) + sliderHeight + cvHeight + descCell.measuredHeight
            }else{
                if(data?.likesCount!=0){
                    AndroidUtilities.dp(16f) + sliderHeight + cvHeight + descCell.measuredHeight
                }else{
                    AndroidUtilities.dp(16f) + sliderHeight + descCell.measuredHeight
                }
            }
            viewLeft = AndroidUtilities.dp(16f)
            dateView.layout(viewLeft, viewTop,viewLeft + dateView.measuredWidth, viewTop + dateView.measuredHeight)
        }

        viewTop = sliderHeight / 2
        viewLeft = (width / 2) - (likeAnimation.measuredWidth / 2)
        likeAnimation.layout(viewLeft, viewTop, viewLeft + likeAnimation.measuredWidth, viewTop + likeAnimation.measuredHeight)

        viewTop = sliderHeight + AndroidUtilities.dp(16f)
//        viewLeft = width - waveAnimation.measuredWidth - AndroidUtilities.dp(8f)
//        waveAnimation.layout(viewLeft, viewTop, viewLeft + waveAnimation.measuredWidth, viewTop + waveAnimation.measuredHeight)

        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50f) + ((sliderHeight - playButton.measuredWidth)/2)
        }else{
            AndroidUtilities.dp(8f) + ((sliderHeight - playButton.measuredWidth)/2)
        }
        viewLeft = (width - playButton.measuredWidth)/2
        playButton.layout(viewLeft, viewTop, viewLeft + playButton.measuredWidth, viewTop + playButton.measuredHeight)

        viewTop = if(openType==PostOpenType.List){
            AndroidUtilities.dp(50f) + ((sliderHeight - progressBar.measuredWidth)/2)
        }else{
            AndroidUtilities.dp(8f) + ((sliderHeight - progressBar.measuredWidth)/2)
        }
        viewLeft = (width - progressBar.measuredWidth)/2
        progressBar.layout(viewLeft, viewTop, viewLeft + progressBar.measuredWidth, viewTop + progressBar.measuredHeight)
    }
    fun setData(post:Post, viewType: PostViewType){
        this.viewType= viewType
        data = post
        var name = if(!post.user?.username.isNullOrEmpty()){
            post.user?.username
        }else if(!post.user?.profileName.isNullOrEmpty()){
            post.user?.profileName
        }else{
            "Unknown"
        }

        name = if (name!!.startsWith("u_")){
            if(!post.user?.profileName.isNullOrEmpty()){ post.user?.profileName }else{ "Unknown" }
            }else{ name }

        if(name!=null){
            if (name.length > 20) {
                name = name.substring(0, 20) + "..."
            }
        }


        headerView.text = name
        if(data?.user?.isVerified==true){
            headerView.rightDrawable = context.getUserBadge(3)
        }else{
            headerView.rightDrawable = context.getUserBadge(data?.user?.type)
        }
        changeLiked(data!!.isLiked)
        changeSaved(data!!.saved)

        followButton.visible = data?.user?.isSubscribed!=true

        initCountView()

        if(data!!.commentsCount!=0 && viewType == PostViewType.List){
            allCommentsView.visibility = VISIBLE
            allCommentsView.text = "${context.getString(R.string.see_all_comment)}(${data!!.commentsCount})"
        }else{
            allCommentsView.visibility = GONE
        }

        data!!.createAt?.let {
            dateView.text =
                DateUtils.getBriefRelativeTimeSpanString(context, Locale.getDefault(), it)
        }

        val adapter = data?.medias?.let {
            PostMediasAdapter(context, viewType, it)
        }
        val fallbackPhotoDrawable = if(post.user?.username!=null || post.user?.profileName!=null){
            val avatarColor = AvatarColor.valueOf(SignalStore.settings().getUserAvatarIfExist(post.user?.id.toString()))

            GeneratedContactPhoto(
                headerView.text.toString(),
                R.drawable.ic_profile_outline_40,
                AndroidUtilities.dp(30f)
            ).asSmallDrawable(context, avatarColor, false)
        }else{
            ResourceContactPhoto(
                R.drawable.ic_profile_outline_40,
                R.drawable.ic_profile_outline_20
            ).asDrawable(context, AvatarColor.UNKNOWN, false)
        }

        if(data?.user?.avatar!=null){
            Glide.with(context)
                .load(data?.user?.avatar)
                .transform(CircleCrop())
                .dontAnimate()
                .fallback(fallbackPhotoDrawable)
                .error(fallbackPhotoDrawable)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .downsample(DownsampleStrategy.CENTER_INSIDE)
                .into(avatarView)
        }else{
            Glide.with(context)
                .load(fallbackPhotoDrawable)
                .transform(CircleCrop())
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .downsample(DownsampleStrategy.CENTER_INSIDE)
                .into(avatarView)
        }
        if(!data?.medias.isNullOrEmpty()){
            val md = data?.medias!![0]
            pOr = md!!.orientation

            if(data!!.medias!!.size==1){
                sliderView.visibility = GONE
                indicator.visibility = GONE
                sliderOneView.visibility = VISIBLE
                playerView.visibility = GONE
                if(md.type==2){
                    postType = 2
                    Glide.with(context)
                        .load(md.preload_photo)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .into(sliderOneView)
                    playButton.visibility = VISIBLE
                }else{
                    playButton.visibility = GONE
                    postType = 1
                    val u1 = md.getSmallPhoto(viewType)
                    val thumbnailRequest = Glide.with(context)
                        .load(md.getThumbnail())
                        .transform(BlurTransformation(context, 0.12f, BlurTransformation.MAX_RADIUS))
                    Glide.with(context)
                        .load(u1)
                        .thumbnail(thumbnailRequest)
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .downsample(DownsampleStrategy.CENTER_INSIDE)
                        .into(sliderOneView)
                }
            }else{
                postType = 1
                indicator.pagesCount = data!!.medias!!.size
                sliderView.visibility = VISIBLE
                indicator.visibility = VISIBLE
                sliderOneView.visibility = GONE
                playerView.visibility = GONE
                playButton.visibility = GONE
            }
        }

        if(data?.allowDownload==1){
            downloadButton.visibility = VISIBLE
        }else{
            downloadButton.visibility = GONE
        }

        sliderView.adapter = adapter
        var desc = post.description
        var isEmpty = false
        desc = if(desc.isNullOrEmpty()){
            isEmpty = true
            if(!post.user?.username.isNullOrEmpty()){
                "@${post.user?.username}"
            }else{
                ""
            }
        }else{
            if(!post.user?.username.isNullOrEmpty()){
                "@${post.user?.username} $desc"
            }else{
                "$desc"
            }
        }
        descCell.visibility = VISIBLE
        descCell.setTextAndValue(desc, "tt", true, isEmpty)

        if(openType==PostOpenType.Single){
            descCell.onClick()
        }
    }

    fun follow(follow:Boolean){
        followButton.isVisible = !follow
    }

    private fun initCountView(){
        if(data!!.likesCount!=0){
            countView.visibility = VISIBLE
            countView.text = data?.likesCount?.let { "${context.getString(R.string.likes)}: ${PostUtil.prettyCount(it.absoluteValue)}" }
        }else{
            countView.visibility = GONE
        }
    }

    fun onLoading(status:Boolean) {
        progressBar.visibility = if(status){
            VISIBLE
        }else{
            GONE
        }
    }

    fun play(){
//        sliderOneView.visibility = GONE
//        playerView.visibility = VISIBLE
        player?.play()
//        waveAnimation.playAnimation()
    }
    fun stop(){
//        sliderOneView.visibility = VISIBLE
//        playerView.visibility = GONE
        player?.pause()
//        waveAnimation.pauseAnimation()
    }
    fun onVideoPlay(){
        playButton.visibility = View.GONE
//        waveAnimation.visibility = View.VISIBLE
        if(player!=null && player!!.currentPosition<1000L){
            val anim = AnimationUtils.loadAnimation(context, R.anim.fade_outdelay)
            anim.setAnimationListener(object :AnimationListener{
                override fun onAnimationStart(p0: Animation?) {

                }

                override fun onAnimationEnd(p0: Animation?) {
                    sliderOneView.visibility = View.GONE
                }

                override fun onAnimationRepeat(p0: Animation?) {

                }

            })
            sliderOneView.startAnimation(anim)
        }else{
            sliderOneView.visibility = View.GONE
        }
        playerView.visibility = VISIBLE
//        val icon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_pause_48_filled)
//        Theme.setDrawableColor(icon, ContextCompat.getColor(context, R.color.signal_play_pause))
//        playButton.setImageDrawable(icon)
    }
    fun onVideoPause(){
        playButton.visibility = View.VISIBLE
//        waveAnimation.visibility = View.GONE
//        sliderOneView.visibility = View.VISIBLE
//        playerView.visibility = GONE
        val icon = ContextCompat.getDrawable(context, R.drawable.ic_fluent_play_48_filled)
        Theme.setDrawableColor(icon, ContextCompat.getColor(context, R.color.signal_play_pause))
        playButton.setImageDrawable(icon)
    }

}
enum class PostOpenType{
    List, Single
}
enum class PostViewType{
    Grid, List
}