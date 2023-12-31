package org.thoughtcrime.securesms.profiles.manage

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.profile.repository.ProfileRepository
import org.signal.core.util.DimensionUnit
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.DSLConfiguration
import org.thoughtcrime.securesms.components.settings.DSLSettingsAdapter
import org.thoughtcrime.securesms.components.settings.DSLSettingsBottomSheetFragment
import org.thoughtcrime.securesms.components.settings.DSLSettingsText
import org.thoughtcrime.securesms.components.settings.configure
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.FragmentResultContract
import org.thoughtcrime.securesms.util.LifecycleDisposable
import org.thoughtcrime.securesms.util.Util

/**
 * Allows the user to either share their username directly or to copy it to their clipboard.
 */
class UsernameShareBottomSheet : DSLSettingsBottomSheetFragment(), KoinComponent {
  private val profileRepository: ProfileRepository = get()
  companion object {
    private const val REQUEST_KEY = "copy_username"
  }

  private val lifecycleDisposable = LifecycleDisposable()

  override fun bindAdapter(adapter: DSLSettingsAdapter) {
    CopyButton.register(adapter)
    ShareButton.register(adapter)

    profileRepository.getUsernameAsync { username ->
      CoroutineScope(Dispatchers.Main).launch {
        adapter.submitList(getConfiguration(username).toMappingModelList())
//        lifecycleDisposable += Recipient.observable(Recipient.self().id).subscribe {
//
//        }
      }
      return@getUsernameAsync null
    }
//    lifecycleDisposable += Recipient.observable(Recipient.self().id).subscribe {
//      adapter.submitList(getConfiguration(it).toMappingModelList())
//    }
  }

  private fun getConfiguration(username1: String?): DSLConfiguration {
    return configure {
      noPadTextPref(
        title = DSLSettingsText.from(
          R.string.UsernameShareBottomSheet__copy_or_share_a_username_link,
          DSLSettingsText.TextAppearanceModifier(R.style.Signal_Text_BodyMedium),
          DSLSettingsText.CenterModifier,
          DSLSettingsText.ColorModifier(
            ContextCompat.getColor(requireContext(), R.color.signal_colorOnSurfaceVariant),
          )
        )
      )

      space(DimensionUnit.DP.toPixels(32f).toInt())

//      val username = recipient.username.get()
      val username = username1 ?: ""
      customPref(
        CopyButton.Model(
          text = username,
          onClick = {
            copyToClipboard(it)
          }
        )
      )

      space(DimensionUnit.DP.toPixels(20f).toInt())

      customPref(
        CopyButton.Model(
          text = getString(R.string.signal_me_url, username),
          onClick = {
            copyToClipboard(it)
          }
        )
      )

      space(DimensionUnit.DP.toPixels(24f).toInt())

      customPref(
        ShareButton.Model(
          text = getString(R.string.signal_me_url, username),
          onClick = {
            openShareSheet(it.text)
          }
        )
      )

      space(DimensionUnit.DP.toPixels(18f).toInt())
    }
  }

  private fun copyToClipboard(model: CopyButton.Model) {
    Util.copyToClipboard(requireContext(), model.text)
    setFragmentResult(REQUEST_KEY, Bundle().apply { putBoolean(REQUEST_KEY, true) })
    findNavController().popBackStack()
  }

  private fun openShareSheet(charSequence: CharSequence) {
    val mimeType = Intent.normalizeMimeType("text/plain")
    val shareIntent = ShareCompat.IntentBuilder(requireContext())
      .setText(charSequence)
      .setType(mimeType)
      .createChooserIntent()
      .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

    try {
      startActivity(shareIntent)
    } catch (e: ActivityNotFoundException) {
      Toast.makeText(requireContext(), R.string.MediaPreviewActivity_cant_find_an_app_able_to_share_this_media, Toast.LENGTH_LONG).show()
    }
  }

  object ResultContract : FragmentResultContract<Boolean>(REQUEST_KEY) {
    override fun getResult(bundle: Bundle): Boolean {
      return bundle.getBoolean(REQUEST_KEY, false)
    }
  }
}
