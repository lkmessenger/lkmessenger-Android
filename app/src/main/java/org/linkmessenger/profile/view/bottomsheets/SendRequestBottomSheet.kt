package org.linkmessenger.profile.view.bottomsheets

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.koin.core.component.KoinComponent
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.LayoutSendRequestBottomsheetBinding
import org.thoughtcrime.securesms.util.BottomSheetUtil
import org.thoughtcrime.securesms.util.ThemeUtil
import org.thoughtcrime.securesms.util.ThemedFragment.themeResId

class SendRequestBottomSheet(val listener: BottomSheetListener): BottomSheetDialogFragment(), KoinComponent {
    private lateinit var binding:LayoutSendRequestBottomsheetBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(
            DialogFragment.STYLE_NORMAL,
            if (ThemeUtil.isDarkTheme(requireContext())) R.style.Theme_Signal_RoundedBottomSheet else R.style.Theme_Signal_RoundedBottomSheet_Light)

        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LayoutSendRequestBottomsheetBinding.inflate(layoutInflater)
        binding.requestButton.setOnClickListener {
            listener.onSent()
            Handler(Looper.getMainLooper()).postDelayed({
                this.dismiss()
            }, 300)
        }
        val requested = arguments?.getBoolean("requested")?:false
        if(requested){
            binding.requestTitle.setText(R.string.request_already_to_message_title)
//            binding.requestButton.setText(R.string.resend_request)
            binding.requestButton.isEnabled = false
        }else{
            binding.requestTitle.setText(R.string.request_to_message_title)
            binding.requestButton.setText(R.string.send_request)
            binding.requestButton.isEnabled = true
        }
        return binding.root
    }


    override fun show(manager: FragmentManager, tag: String?) {
        BottomSheetUtil.show(manager, tag, this)
    }
    interface BottomSheetListener {
        fun onSent()
    }
    companion object {
        fun create(requested:Boolean, listener: BottomSheetListener): BottomSheetDialogFragment {
            val args = Bundle()
            val fragment = SendRequestBottomSheet(listener)
            args.putBoolean("requested", requested)
            fragment.arguments = args
            return fragment
        }
    }
}