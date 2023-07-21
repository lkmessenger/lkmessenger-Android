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
import org.linkmessenger.openPremiumActivity
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.sendEmail
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.BottomsheetVerifiedAccountBinding
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.BottomSheetUtil
import org.thoughtcrime.securesms.util.ThemeUtil

class VerifiedAccountBottomSheet(val listener: BottomSheetListener): BottomSheetDialogFragment(), KoinComponent {
    private lateinit var binding:BottomsheetVerifiedAccountBinding
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
        binding = BottomsheetVerifiedAccountBinding.inflate(layoutInflater)

        val user = arguments?.getParcelable<ProfileData>("user")
        if(user==null){
            dismiss()
        }

        val phone = try {
            Recipient.self().requireE164()
        }catch (e:Exception){
            ""
        }
        binding.requestButton.setOnClickListener {
            if (user!!.isVerified){
                requireContext().sendEmail("apps@linkmessenger.me", "Need Verified account", "Phone: $phone\n")
            }else if(user.type==2){
                requireContext().sendEmail("apps@linkmessenger.me", "Need Business account", "Phone: $phone\n")
            }else if(user.type==1){
                //requireContext().sendEmail("apps@linkmessenger.me", "Need Premium account", "Phone: $phone\n")
                requireContext().openPremiumActivity()
            }
            Handler(Looper.getMainLooper()).postDelayed({
                this.dismiss()
            }, 300)
        }
        if(user!!.type==1){
            binding.title.text = getString(R.string.premium_account)
            binding.description.text = getString(R.string.premium_account_description)
            binding.sellDescription.text = getString(R.string.premium_account_sell_description)
            binding.requestButton.text = getString(R.string.buy_premium)
            binding.requestButton.setIconResource(R.drawable.ic_fluent_premium_24_filled)

            val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_gold_28)
            binding.description.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
        }else if(user.type==2){
            binding.title.text = getString(R.string.business_account)
            binding.description.text = getString(R.string.business_account_description)
            binding.sellDescription.text = getString(R.string.business_account_sell_description)
            binding.requestButton.text = getString(R.string.open_business)
            binding.requestButton.setIconResource(R.drawable.ic_fluent_building_shop_24_filled)

            val icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_biz_badge_28)
            binding.description.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null)
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
        fun create(user:ProfileData, listener: BottomSheetListener): BottomSheetDialogFragment {
            val args = Bundle()
            val fragment = VerifiedAccountBottomSheet(listener)
            args.putParcelable("user", user)
            fragment.arguments = args
            return fragment
        }
    }
}