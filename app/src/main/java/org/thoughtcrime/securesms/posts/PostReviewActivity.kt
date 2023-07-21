package org.thoughtcrime.securesms.posts

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.updateLayoutParams
import androidx.viewpager.widget.ViewPager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.relex.circleindicator.CircleIndicator
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.getScreenSize
import org.linkmessenger.posts.viewmodel.AddPostEvent
import org.linkmessenger.posts.viewmodel.AddPostEventType
import org.linkmessenger.posts.viewmodel.PostsViewModel
import org.linkmessenger.request.models.PostData
import org.linkmessenger.showError
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.components.emoji.EmojiEditText
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.mediasend.v2.MediaSelectionActivity
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.MediaUtil
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.views.CircularProgressMaterialButton
import java.util.*
import kotlin.math.roundToInt


class PostReviewActivity(val data: PostData? = null) : AppCompatActivity(), KoinComponent {
    private val postViewModel: PostsViewModel = get()
    private lateinit var viewPagerAdapter: ImageSlideAdapter
    private lateinit var indicator: CircleIndicator
    private lateinit var viewpager: ViewPager
    private lateinit var avatar: AvatarView
    private lateinit var username: TextView
    private lateinit var addMedia:ImageButton
    private lateinit var toolbar: Toolbar
    private lateinit var dimmer:View
    private lateinit var videoPlayer: StyledPlayerView
    private lateinit var player: ExoPlayer
    private lateinit var playPauseButton: ImageButton
    private lateinit var rootView:ConstraintLayout
    private lateinit var descTextEditor:EmojiEditText
    private lateinit var publishButton: CircularProgressMaterialButton
    private lateinit var orientationTypeView: ImageButton
    private lateinit var commentAvailabilitySwitch: SwitchMaterial
    private lateinit var allowDownloadSwitch: SwitchMaterial
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var eventTextView: TextView
    private var closing:Boolean = false

    private var l:Float= 16f/9f
    private var p:Float= 4f/5f

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_review)

        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.signal_colorSecondaryBackground)

        viewpager = findViewById(R.id.viewpager)
        avatar = findViewById(R.id.avatar)
        username = findViewById(R.id.username)
        addMedia = findViewById(R.id.add_media)
        toolbar = findViewById(R.id.toolbar)
        dimmer = findViewById(R.id.dimmer)
        videoPlayer = findViewById(R.id.video_player)
        playPauseButton = findViewById(R.id.play_pause)
        rootView = findViewById(R.id.root)
        publishButton = findViewById(R.id.publish)
        descTextEditor = findViewById(R.id.post_description_edit_text)
        orientationTypeView = findViewById(R.id.orientation)
        commentAvailabilitySwitch = findViewById(R.id.switch_widget)
        eventTextView = findViewById(R.id.event_text)
        progressBar = findViewById(R.id.progress_bar)
        allowDownloadSwitch = findViewById(R.id.download_switch)

        username.text = Recipient.self().profileName.toString()
        avatar.displayProfileAvatar(Recipient.self())

        toolbar.setNavigationOnClickListener{
            onBackPressed()
        }
        publishButton.setOnClickListener {
            player.pause()
            postViewModel.setDescription(descTextEditor.text.toString())
            postViewModel.publishPost{
                CoroutineScope(Dispatchers.Main).launch {
                    handleAddPostCallback(it)
                }
            }
        }

        player = ExoPlayer.Builder(this).build()
        videoPlayer.player = player
        player.addListener(object:Player.Listener{
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if(isPlaying){
                    onVideoPlay()
                }else{
                    onVideoPause()
                }
            }
        })
        playPauseButton.setOnClickListener {
            if(player.isPlaying){
                player.pause()
            }else{
                player.play()
            }
        }
        videoPlayer.setOnClickListener {
            playPauseButton.performClick()
        }


        postViewModel.orientationType.observe(this){
            val size = getScreenSize()
            when (it) {
                0 -> {
                    orientationTypeView.setImageResource(R.drawable.ic_fluent_square_24_regular)
                    viewpager.updateLayoutParams {
                        height = size.width
                    }
                    videoPlayer.updateLayoutParams {
                        height = size.width
                    }
                    viewPagerAdapter.notifyDataSetChanged()
                }
                1 -> {
                    orientationTypeView.setImageResource(R.drawable.ic_fluent_rectangle_landscape_24_regular)
                    orientationTypeView.animate().rotation(0f)
                    viewpager.updateLayoutParams {
                        val d = (size.width/l).roundToInt()
                        height = d
                    }
                    videoPlayer.updateLayoutParams {
                        val d = (size.width/l).roundToInt()
                        height = d
                    }
                    viewPagerAdapter.notifyDataSetChanged()
                }
                else -> {
                    orientationTypeView.setImageResource(R.drawable.ic_fluent_rectangle_landscape_24_regular)
                    orientationTypeView.animate().rotation(90f)
                    viewpager.updateLayoutParams {
                        val d = (size.width/p).roundToInt()
                        height = d
                    }
                    videoPlayer.updateLayoutParams {
                        val d = (size.width/p).roundToInt()
                        height = d
                    }
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }
        }
        orientationTypeView.setOnClickListener {
            postViewModel.changeOrientation()
        }

        commentAvailabilitySwitch.setOnCheckedChangeListener { compoundButton, b ->
            postViewModel.setCommentAvailability(b)
        }
        allowDownloadSwitch.setOnCheckedChangeListener{_,b->
            postViewModel.setAllowDownload(b)
        }

        viewPagerAdapter = ImageSlideAdapter(this, arrayListOf()) {
            postViewModel.getByIndex(viewpager.currentItem)?.let {
                postViewModel.removeMedia(it)
            }
            indicator.dataSetObserver.onChanged()
            viewPagerAdapter.notifyDataSetChanged()
        }
        viewpager.adapter = viewPagerAdapter

        indicator = findViewById(R.id.indicator)
        indicator.setViewPager(viewpager)

        postViewModel.error.observe(this){
            it?.let { it1 -> rootView.showError(it1) }
            postViewModel.error.postValue(null)
        }

        postViewModel.medias.observe(this){
            val v = it.find { it1 -> MediaUtil.isVideo(it1.mimeType) }
            if(v!=null){
                switchToVideo()

                val mediaItem = MediaItem.fromUri(Objects.requireNonNull(v.uri))
                player.setMediaItem(mediaItem)
                player.prepare()
                player.play()
            }else{
                switchToImages()
                viewPagerAdapter.imageList = it.map { m: Media -> m.uri } as ArrayList<Uri>
                viewPagerAdapter.notifyDataSetChanged()
                indicator.dataSetObserver.onChanged()
            }
            if(it.isEmpty() && !closing){
                openSelectMedias()
            }
        }
        postViewModel.loading.observe(this){
            if(it){
                publishButton.visibility = View.GONE
                dimmer.setOnClickListener {  }
                dimmer.visibility = View.VISIBLE
                ViewUtil.hideKeyboard(this, publishButton)
                descTextEditor.clearFocus()

                eventTextView.visibility = View.VISIBLE
                progressBar.visibility = View.VISIBLE
            }else{
                publishButton.visibility = View.VISIBLE
                dimmer.visibility = View.GONE
                addMedia.isEnabled = true

                eventTextView.visibility = View.GONE
                progressBar.visibility = View.GONE
            }
        }

        addMedia.setOnClickListener {
            openSelectMedias()
        }
    }
    private fun handleAddPostCallback(callback:AddPostEvent){
        when(callback.type){
            AddPostEventType.Compress ->{
                eventTextView.setText(R.string.compress_video)
                progressBar.isIndeterminate = true
            }
            AddPostEventType.Check ->{
                eventTextView.setText(R.string.check_upload_post)
                progressBar.isIndeterminate = true
            }
            AddPostEventType.Uploading ->{
                eventTextView.setText(R.string.upload_media)
                progressBar.isIndeterminate = false
                progressBar.setProgressCompat(callback.progress ?: 0, true)
            }
            AddPostEventType.Error ->{
                eventTextView.text = null
                progressBar.isIndeterminate = true
            }
            AddPostEventType.Complete ->{
                closing = true
                postViewModel.clearMedias()
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private val selectMediaResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            if(postViewModel.medias.value?.isEmpty() == true){
                finish()
            }
        }
    }
    private fun onVideoPlay(){
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_fluent_pause_48_filled)
        Theme.setDrawableColor(icon, ContextCompat.getColor(this, R.color.signal_play_pause))
        playPauseButton.setImageDrawable(icon)
    }
    private fun onVideoPause(){
        val icon = ContextCompat.getDrawable(this, R.drawable.ic_fluent_play_48_filled)
        Theme.setDrawableColor(icon, ContextCompat.getColor(this, R.color.signal_play_pause))
        playPauseButton.setImageDrawable(icon)
    }
    private fun switchToVideo(){
        videoPlayer.visibility = View.VISIBLE
        playPauseButton.visibility = View.VISIBLE
        viewpager.visibility = View.GONE
        indicator.visibility = View.GONE
    }
    private fun switchToImages(){
        videoPlayer.visibility = View.GONE
        playPauseButton.visibility = View.GONE
        viewpager.visibility = View.VISIBLE
        indicator.visibility = View.VISIBLE
    }

    private fun openSelectMedias() {
        Permissions.with(this)
                .request(Manifest.permission.READ_EXTERNAL_STORAGE)
                .ifNecessary()
                .withPermanentDenialDialog(getString(R.string.AttachmentManager_signal_requires_the_external_storage_permission_in_order_to_attach_photos_videos_or_audio))
                .onAllGranted {
                    selectMediaResultLauncher.launch(MediaSelectionActivity.galleryForPost(
                        this,
                        Collections.emptyList()
                    ))
                }
                .execute()
    }

    override fun onBackPressed() {
        if(postViewModel.loading.value!=true){
            showYesNoDialog()
        }
    }

    private fun showYesNoDialog(){
        val builder = MaterialAlertDialogBuilder(this)
        builder.setTitle(R.string.PostReviewActivity__dialog_title)
        builder.setPositiveButton(R.string.yes) { _, _ -> goToHome() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(R.string.PostReviewActivity__Dialog_description)
        val dialog = builder.create()
        dialog.show()
    }

    private fun goToHome(){
        closeActivity(PostActivityResult.Close)
    }

    private fun closeActivity(type:PostActivityResult){
        player.release()
        if (type==PostActivityResult.Close){
            closing = true
            postViewModel.clearMedias()
            setResult(RESULT_CANCELED, intent)
            finish()
        }else{
            val intent = Intent().apply { putExtras(bundleOf("type" to type)) }
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}
enum class PostActivityResult{
    Close, AddMedia, Submit
}