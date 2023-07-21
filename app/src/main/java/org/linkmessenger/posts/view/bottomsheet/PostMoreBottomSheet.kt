package org.linkmessenger.posts.view.bottomsheet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.android.ext.android.get
import org.koin.core.component.KoinComponent
import org.linkmessenger.base.ui.Theme
import org.linkmessenger.data.local.entity.Post
import org.linkmessenger.openReportView
import org.linkmessenger.posts.viewmodel.PostsViewModel
import org.linkmessenger.profile.viewmodel.ProfileViewModel
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.util.BottomSheetUtil.show
import org.thoughtcrime.securesms.util.ThemeUtil


class PostMoreBottomSheet(listener: BottomSheetListener?) : BottomSheetDialogFragment(), KoinComponent {
    private var report: TextView? = null
    private var sharePost: AppCompatImageView? = null
    private var copyLink: AppCompatImageView? = null
    private var savePost: AppCompatImageView? = null
    private var blockUser: TextView? = null
    private val postsViewModel: PostsViewModel = get()
    private val profileViewModel: ProfileViewModel = get()

    private var isSaved = false
    private var position = 0

    private var mBottomSheetListener: BottomSheetListener?=null
    companion object {
        fun create(post: Post, position: Int, listener: BottomSheetListener?): BottomSheetDialogFragment {
            val args = Bundle()
            val fragment = PostMoreBottomSheet(listener)
            args.putLong("post_id", post.id)
            args.putBoolean("is_saved", post.saved)
            args.putInt("position", position)
            args.putInt("user_id", post.user?.id ?: 0)
            fragment.arguments = args
            return fragment
        }
    }
    init {
        this.mBottomSheetListener = listener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(DialogFragment.STYLE_NORMAL,
                if (ThemeUtil.isDarkTheme(requireContext())) R.style.Theme_Signal_RoundedBottomSheet else R.style.Theme_Signal_RoundedBottomSheet_Light)

        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mBottomSheetListener = context as BottomSheetListener?
        }
        catch (_: ClassCastException){ }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.post_more_bottom_sheet, container, false)
        sharePost = view.findViewById(R.id.share_button)
        savePost = view.findViewById(R.id.save_button)
        copyLink = view.findViewById(R.id.copy_link)
        report = view.findViewById(R.id.rbs_report_button)
        blockUser = view.findViewById(R.id.rbs_block_user)
        return view
    }

    override fun show(manager: FragmentManager, tag: String?) {
        show(manager, tag, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val arguments = requireArguments()
        val postId = arguments.getLong("post_id")
        val userId = arguments.getInt("user_id")
        isSaved = arguments.getBoolean("is_saved")
        position = arguments.getInt("position")
        val link = "https://linkm.me/posts/$postId"
        changeSaved(isSaved)
        report!!.setOnClickListener { reportPost(postId)}
        sharePost!!.setOnClickListener { sharePost(link) }
        copyLink!!.setOnClickListener { copyLinkToClipboard(link) }
        savePost!!.setOnClickListener { saveAction(postId)}
        blockUser!!.setOnClickListener{ blockAction(userId) }
    }

    private fun blockAction(userId: Int) {
        showYesNoDialog {
            profileViewModel.blockUser(userId)
            this.dismiss()
        }
    }

    private fun reportPost(postId: Long) {
        context?.openReportView(postId)
        this.dismiss()
    }

    private fun sharePost(postLink: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, postLink)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
        this.dismiss()
    }

    private fun copyLinkToClipboard(textToCopy: String) {
        val clipboard = requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Post link", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), getString(R.string.copy_link_success), Toast.LENGTH_SHORT)
                .show()
        this.dismiss()
    }

    private fun changeSaved(savedStatus: Boolean) {
        val saveIcon: Drawable?
        if (!savedStatus) {
            saveIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_bookmark_28_regular)
            Theme.setDrawableColor(saveIcon, ContextCompat.getColor(requireContext(), R.color.signal_colorNeutralInverse))
        } else {
            saveIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_bookmark_28_filled)
            Theme.setDrawableColor(saveIcon, ContextCompat.getColor(requireContext(), R.color.signal_colorPrimary))
        }
        savePost!!.setImageDrawable(saveIcon)
    }

    private fun saveAction(postId: Long) {
        if(isSaved){
            postsViewModel.deletePostFromCollection(postId)
        }else{
            postsViewModel.addPostToCollection(postId)
        }

        isSaved = !isSaved
        mBottomSheetListener?.onSaveClick(position, isSaved)
        changeSaved(isSaved)

        this.dismiss()
    }
    interface BottomSheetListener {
        fun onSaveClick(position: Int, isSaved: Boolean)
    }
    private fun showYesNoDialog(onYes:() -> Unit){
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.hide_user_post_title))
        builder.setPositiveButton(R.string.yes) { _, _ -> onYes.invoke() }
        builder.setNegativeButton(R.string.no) { _, _ -> }
        builder.setMessage(getString(R.string.hide_user_post_message))
        val dialog = builder.create()
        dialog.show()
    }

}