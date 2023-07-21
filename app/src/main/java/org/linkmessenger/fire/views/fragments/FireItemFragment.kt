package org.linkmessenger.fire.views.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import org.linkmessenger.request.models.PostData
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.FragmentFireItemBinding

class FireItemFragment(val post:PostData): Fragment(R.layout.fragment_fire_item) {
    var binding: FragmentFireItemBinding?=null
    var player: ExoPlayer?=null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFireItemBinding.bind(view)

        val md = post.medias!![0]
        if(md.type==2){
            player = ExoPlayer.Builder(requireContext()).build()
            binding?.playerView?.player = player
            player?.setMediaItem(MediaItem.fromUri(md.url))
            player?.prepare()
        }
    }

    override fun onResume() {
        super.onResume()
        player?.play()
    }

    override fun onPause() {
        player?.pause()
        super.onPause()
    }

    override fun onDestroyView() {
        binding = null
        player = null
        super.onDestroyView()
    }
}