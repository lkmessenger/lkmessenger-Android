package org.linkmessenger.updateinfo.views

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.viewpager.widget.ViewPager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import me.relex.circleindicator.CircleIndicator
import org.linkmessenger.request.models.VersionResponse
import org.linkmessenger.updateinfo.adapters.UpdateInfoAdapter
import org.linkmessenger.updateinfo.models.UpdateInfo
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.FixedRoundedCornerBottomSheetDialogFragment
import org.thoughtcrime.securesms.util.BottomSheetUtil.show
import org.thoughtcrime.securesms.util.PlayStoreUtil
import org.thoughtcrime.securesms.util.TextSecurePreferences
import org.thoughtcrime.securesms.util.ThemeUtil


class UpdateInfoBottomSheet(val infos: VersionResponse, val isCloseButton: Boolean) : FixedRoundedCornerBottomSheetDialogFragment() {
    var updateNow: MaterialButton? = null
    var root: LinearLayout? = null
    var close: ImageButton? = null
    var adapter: UpdateInfoAdapter? = null
    var indicatorView: CircleIndicator? = null

    override val peekHeightPercentage: Float = 0.67f

//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = BottomSheetDialog(requireContext(), theme)
//        dialog.setOnShowListener {
//
//            val bottomSheetDialog = it as BottomSheetDialog
//            val parentLayout =
//                    bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
//            parentLayout?.let { it ->
//                val behaviour = BottomSheetBehavior.from(it)
//                setupFullHeight(it)
//                behaviour.state = BottomSheetBehavior.STATE_EXPANDED
//            }
//        }
//        return dialog
//    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.update_info_bottomsheet, container, false)
//        view.setBackgroundResource(R.drawable.bottom_sheet_corner_shape)
        val viewPager: ViewPager = view.findViewById(R.id.viewPager)
        updateNow = view.findViewById(R.id.update_now)
        close = view.findViewById(R.id.close)
        root = view.findViewById(R.id.rootContainer)
        indicatorView = view.findViewById(R.id.indicator_view)

        if (isCloseButton){
            updateNow?.text = context?.getString(R.string.close)
        }else{
            updateNow?.text = context?.getString(R.string.update_now)
        }

        updateNow?.setOnClickListener {
            if (isCloseButton){
                this.dismiss()
            }else{
                TextSecurePreferences.setRatingEnabled(context, false)
                PlayStoreUtil.openPlayStoreOrOurApkDownloadPage(requireContext())
            }
        }

//        infos.add(UpdateInfo("https://images.pexels.com/photos/2882668/pexels-photo-2882668.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
//                "New \"Quick Reply\" feature!","Simply click on it whenever your friend asks the question. "))
//        infos.add(UpdateInfo("https://images.pexels.com/photos/7947957/pexels-photo-7947957.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
//                "Improved Group Chat Management!","You can easily remove someone who no longer works with the company."))
//        infos.add(UpdateInfo("https://images.pexels.com/photos/7948036/pexels-photo-7948036.jpeg?auto=compress&cs=tinysrgb&w=1260&h=750&dpr=2",
//                "Introducing \"Dark Mode\"","Dark Mode is a you to switch the app's color scheme to a darker palette"))

        adapter = UpdateInfoAdapter(requireContext(), infos.data)
        viewPager.adapter = adapter

        indicatorView?.setViewPager(viewPager)

        close?.setOnClickListener{
            this.dismiss()
        }

        return view
    }
//    private fun setupFullHeight(bottomSheetDialog: BottomSheetDialog) {
//        val bottomSheet = bottomSheetDialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout?
////        val behavior: BottomSheetBehavior<*> = BottomSheetBehavior.from(bottomSheet)
//        val layoutParams = bottomSheet!!.layoutParams
//        val windowHeight = getWindowHeight()
//        if (layoutParams != null) {
//            layoutParams.height = windowHeight
//        }
//        bottomSheet.layoutParams = layoutParams
////        behavior.state = BottomSheetBehavior.STATE_EXPANDED
//    }
//
//    private fun getWindowHeight(): Int {
//        // Calculate window height for fullscreen use
//        val displayMetrics = DisplayMetrics()
//        requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
//        return displayMetrics.heightPixels
//    }

    override fun show(manager: FragmentManager, tag: String?) {
        show(manager, tag, this)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter = null
        infos.data.clear()
    }


    companion object {
        fun create(infos: VersionResponse, isCloseButton: Boolean): BottomSheetDialogFragment {
            return UpdateInfoBottomSheet(infos, isCloseButton)
        }
    }
}