package org.linkmessenger.posts.view.bottomsheet

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.core.component.KoinComponent
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.mediasend.v2.MediaSelectionActivity
import org.thoughtcrime.securesms.permissions.Permissions
import org.thoughtcrime.securesms.posts.PostReviewActivity
import org.thoughtcrime.securesms.util.BottomSheetUtil.show
import org.thoughtcrime.securesms.util.ThemeUtil


class AddTabSelectorBottomSheet : BottomSheetDialogFragment(), KoinComponent {
    private var addPost: TextView? = null
    private var addStory: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(DialogFragment.STYLE_NORMAL,
                if (ThemeUtil.isDarkTheme(requireContext())) R.style.Theme_Signal_RoundedBottomSheet else R.style.Theme_Signal_RoundedBottomSheet_Light)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.add_tab_selector_bottom_sheet, container, false)
        addPost = view.findViewById(R.id.add_post)
        addStory = view.findViewById(R.id.add_story)

        return view
    }

    override fun show(manager: FragmentManager, tag: String?) {
        show(manager, tag, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        addStory!!.setOnClickListener { v: View? -> openAddStory() }
        addPost!!.setOnClickListener { v: View? -> openAddPost()}
    }

    private val addPostResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {

        }
    }

    private fun openAddPost() {
        val intent = Intent(requireActivity(), PostReviewActivity::class.java)
        addPostResultLauncher.launch(intent)
        this.dismiss()
    }

    private fun openAddStory() {
        Permissions.with(requireActivity())
                .request(Manifest.permission.CAMERA)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.ConversationActivity_to_capture_photos_and_video_allow_signal_access_to_the_camera), R.drawable.ic_fluent_camera_24_regular)
                .withPermanentDenialDialog(getString(R.string.ConversationActivity_signal_needs_the_camera_permission_to_take_photos_or_video))
                .onAllGranted {
                    startActivity(MediaSelectionActivity.camera(requireContext(), isStory = true))
                }
                .onAnyDenied {
                    context?.let {
                        Toast.makeText(it, R.string.ConversationActivity_signal_needs_camera_permissions_to_take_photos_or_video, Toast.LENGTH_LONG).show()
                    }
                }
                .execute()
        this.dismiss()
    }


    companion object {
        fun create(): BottomSheetDialogFragment {
            return AddTabSelectorBottomSheet()
        }
    }
}