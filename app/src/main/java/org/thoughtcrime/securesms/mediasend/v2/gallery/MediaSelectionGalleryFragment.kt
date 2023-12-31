package org.thoughtcrime.securesms.mediasend.v2.gallery

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import app.cash.exhaustive.Exhaustive
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.posts.viewmodel.PostsViewModel
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.mediasend.Media
import org.thoughtcrime.securesms.mediasend.v2.MediaSelectionNavigator
import org.thoughtcrime.securesms.mediasend.v2.MediaSelectionNavigator.Companion.requestPermissionsForCamera
import org.thoughtcrime.securesms.mediasend.v2.MediaSelectionViewModel
import org.thoughtcrime.securesms.mediasend.v2.MediaValidator
import org.thoughtcrime.securesms.mediasend.v2.review.MediaSelectionItemTouchHelper
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.util.MediaUtil

private const val MEDIA_GALLERY_TAG = "MEDIA_GALLERY"

class MediaSelectionGalleryFragment : Fragment(R.layout.fragment_container), MediaGalleryFragment.Callbacks, KoinComponent {
  private val postViewModel: PostsViewModel = get()
  private lateinit var mediaGalleryFragment: MediaGalleryFragment

  private val navigator = MediaSelectionNavigator(
    toCamera = R.id.action_mediaGalleryFragment_to_mediaCaptureFragment
  )

  private val sharedViewModel: MediaSelectionViewModel by viewModels(
    ownerProducer = { requireActivity() }
  )

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    mediaGalleryFragment = ensureMediaGalleryFragment()

    mediaGalleryFragment.bindSelectedMediaItemDragHelper(ItemTouchHelper(MediaSelectionItemTouchHelper(sharedViewModel)))

    postViewModel.medias.value?.let { sharedViewModel.addMedia(it) }
    sharedViewModel.state.observe(viewLifecycleOwner) { state ->
      if(sharedViewModel.isAddingNewPost()){

//        PostManager.selectedMedias = state.selectedMedia.toMutableList()

//        postViewModel.postMedias(ArrayList(state.selectedMedia))
        mediaGalleryFragment.onViewStateUpdated(MediaGalleryFragment.ViewState(state.selectedMedia))
      }else{
        mediaGalleryFragment.onViewStateUpdated(MediaGalleryFragment.ViewState(state.selectedMedia))
      }
    }

    if (arguments?.containsKey("first") == true) {
      requireActivity().onBackPressedDispatcher.addCallback(
        viewLifecycleOwner,
        object : OnBackPressedCallback(true) {
          override fun handleOnBackPressed() {
            requireActivity().finish()
          }
        }
      )
    }

    sharedViewModel.mediaErrors.observe(viewLifecycleOwner, this::handleError)
  }

  private fun handleError(error: MediaValidator.FilterError) {
    @Exhaustive
    when (error) {
      MediaValidator.FilterError.ItemTooLarge -> Toast.makeText(requireContext(), R.string.MediaReviewFragment__one_or_more_items_were_too_large, Toast.LENGTH_SHORT).show()
      MediaValidator.FilterError.ItemInvalidType -> Toast.makeText(requireContext(), R.string.MediaReviewFragment__one_or_more_items_were_invalid, Toast.LENGTH_SHORT).show()
      MediaValidator.FilterError.TooManyItems -> Toast.makeText(requireContext(), R.string.MediaReviewFragment__too_many_items_selected, Toast.LENGTH_SHORT).show()
      is MediaValidator.FilterError.NoItems -> {
        if (error.cause != null) {
          handleError(error.cause)
        }
      }
    }
  }

  private fun ensureMediaGalleryFragment(): MediaGalleryFragment {
    val fragmentInManager: MediaGalleryFragment? = childFragmentManager.findFragmentByTag(MEDIA_GALLERY_TAG) as? MediaGalleryFragment

    return if (fragmentInManager != null) {
      fragmentInManager
    } else {
      val mediaGalleryFragment = MediaGalleryFragment()

      childFragmentManager.beginTransaction()
        .replace(
          R.id.fragment_container,
          mediaGalleryFragment,
          MEDIA_GALLERY_TAG
        )
        .commitNowAllowingStateLoss()

      mediaGalleryFragment
    }
  }

  @Deprecated("Deprecated in Java")
  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
    Permissions.onRequestPermissionsResult(this, requestCode, permissions, grantResults)
  }

  override fun isMultiselectEnabled(): Boolean {
    return true
  }

  override fun onMediaSelected(media: Media) {
    sharedViewModel.addMedia(media)
    if(sharedViewModel.isAddingNewPost() && MediaUtil.isVideo(media.mimeType)){
      onSubmit()
    }
  }

  override fun onMediaUnselected(media: Media) {
    sharedViewModel.removeMedia(media)
  }

  override fun onSelectedMediaClicked(media: Media) {
    sharedViewModel.setFocusedMedia(media)
    navigator.goToReview(findNavController())
  }

  override fun onNavigateToCamera() {
    val controller = findNavController()
    requestPermissionsForCamera {
      navigator.goToCamera(controller)
    }
  }

  override fun onSubmit() {
    navigator.goToReview(findNavController())
  }

  override fun onToolbarNavigationClicked() {
    requireActivity().onBackPressed()
  }
}
