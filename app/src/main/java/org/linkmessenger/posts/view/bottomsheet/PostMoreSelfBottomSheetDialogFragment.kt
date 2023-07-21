package org.linkmessenger.posts.view.bottomsheet

import android.content.*
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import org.linkmessenger.request.models.PostData
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.posts.PostReviewActivity
import org.thoughtcrime.securesms.util.AsynchronousCallback
import org.thoughtcrime.securesms.util.BottomSheetUtil.show
import org.thoughtcrime.securesms.util.ThemeUtil

class PostMoreSelfBottomSheetDialogFragment(val callback:(CallBackCommand)->Unit, listener: BottomSheetListener?) : BottomSheetDialogFragment(), KoinComponent {
//    private var report: TextView? = null
    private var sharePost: AppCompatImageView? = null
    private var copyLink: AppCompatImageView? = null
    private var savePost: AppCompatImageView? = null
    private val postsViewModel: PostsViewModel = get()
    private var turnOffCommenting: TextView? = null
    private var editPost: TextView? = null
    private var deletePost: TextView? = null

    private var isSaved = false
    private var position = 0

    private var mBottomSheetListener: BottomSheetListener?=null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            mBottomSheetListener = context as BottomSheetListener?
        }
        catch (_: ClassCastException){

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(
            DialogFragment.STYLE_NORMAL,
            if (ThemeUtil.isDarkTheme(requireContext())) R.style.Theme_Signal_RoundedBottomSheet else R.style.Theme_Signal_RoundedBottomSheet_Light
        )
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.post_more_self_bottom_sheet, container, false)
        sharePost = view.findViewById(R.id.share_button)
        savePost = view.findViewById(R.id.save_button)
        copyLink = view.findViewById(R.id.copy_link)
//        report = view.findViewById(R.id.rbs_report_button)
        turnOffCommenting = view.findViewById(R.id.rbs_turn_off_comment_button)
        editPost = view.findViewById(R.id.rbs_edit_button)
        deletePost = view.findViewById(R.id.rbs_delete_button)
        return view
    }

    override fun show(manager: FragmentManager, tag: String?) {
        show(manager, tag, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val arguments = requireArguments()
        val postId = arguments.getLong("post_id")
        isSaved = arguments.getBoolean("is_saved")
        position = arguments.getInt("position")
        val link = "https://linkm.me/posts/$postId"
        changeSaved(isSaved)
//        report!!.setOnClickListener { v: View? -> reportPost(postId)}
        sharePost!!.setOnClickListener { v: View? -> sharePost(link) }
        copyLink!!.setOnClickListener { v: View? -> copyLinkToClipboard(link) }
        savePost!!.setOnClickListener { v: View? -> saveAction(postId)}
        turnOffCommenting!!.setOnClickListener { v: View? -> }
        editPost!!.setOnClickListener { v: View? -> openEditPost(null) }
        deletePost!!.setOnClickListener { v: View? -> showYesNoDialog() }
    }


    fun changeSaved(savedStatus: Boolean) {
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

    fun saveAction(postId: Long) {
        if(isSaved){
            postsViewModel.deletePostFromCollection(postId)
        }else{
            postsViewModel.addPostToCollection(postId)
        }

        isSaved = !isSaved
        mBottomSheetListener?.onSelfSaveClick(position, isSaved)
        changeSaved(isSaved)

        this.dismiss()
    }
    interface BottomSheetListener {
        fun onSelfSaveClick(position: Int, isSaved: Boolean)
    }

    companion object {
        fun create(callback: (CallBackCommand) -> Unit, post: Post, position: Int, listener: BottomSheetListener?): BottomSheetDialogFragment {
            val args = Bundle()
            val fragment = PostMoreSelfBottomSheetDialogFragment(callback, listener)
            args.putLong("post_id", post.id)
            args.putBoolean("is_saved", post.saved)
            args.putInt("position", position)
            fragment.arguments = args
            return fragment
        }
    }

    private fun sharePost(postLink: String) {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, postLink)
        startActivity(Intent.createChooser(shareIntent, "Share via"))
    }

    private fun copyLinkToClipboard(textToCopy: String) {
        val clipboard =
            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Post link", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(requireContext(), getString(R.string.copy_link_success), Toast.LENGTH_SHORT)
            .show()
    }

    private fun showYesNoDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(getString(R.string.delete_post))
        builder.setMessage(getString(R.string.delete_post_message))
        builder.setPositiveButton(getString(R.string.delete)) { dialog: DialogInterface?, which: Int ->
            callback.invoke(CallBackCommand.Delete)
            dialog?.dismiss()
            this.dismiss()
        }
        builder.setNegativeButton(getString(R.string.conversation_input_panel__cancel)) { dialog: DialogInterface?, which: Int ->dialog?.dismiss() }
        builder.show()
    }

    private fun openEditPost(data: PostData?) {
        val intent = Intent(requireActivity(), PostReviewActivity::class.java)
        intent.putExtra("data", data)
        startActivity(intent)
    }

    enum class CallBackCommand{
        Delete
    }
}