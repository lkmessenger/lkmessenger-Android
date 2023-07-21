package org.thoughtcrime.securesms.components.settings.app

import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.firebase.crashlytics.FirebaseCrashlytics
import org.linkmessenger.profile.view.activities.ProfileActivity
import org.signal.core.util.concurrent.SimpleTask
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.BuildConfig
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.badges.BadgeImageView
import org.thoughtcrime.securesms.components.AvatarImageView
import org.thoughtcrime.securesms.components.settings.*
import org.thoughtcrime.securesms.contacts.sync.ContactDiscovery
import org.thoughtcrime.securesms.conversation.ConversationIntents
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.jobmanager.impl.NetworkConstraint
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.phonenumbers.PhoneNumberFormatter
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.Util
import org.thoughtcrime.securesms.util.adapter.mapping.LayoutFactory
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.adapter.mapping.MappingViewHolder
import org.thoughtcrime.securesms.util.navigation.safeNavigate
import org.thoughtcrime.securesms.util.views.SimpleProgressDialog
import java.io.IOException


class AppSettingsFragment : DSLSettingsFragment(R.string.text_secure_normal__menu_settings) {

  private val viewModel: AppSettingsViewModel by viewModels()

  override fun bindAdapter(adapter: MappingAdapter) {
    adapter.registerFactory(SubscriptionPreference::class.java, LayoutFactory(::SubscriptionPreferenceViewHolder, R.layout.dsl_preference_item))
    adapter.registerFactory(BioPreference::class.java, LayoutFactory(::BioPreferenceViewHolder, R.layout.bio_preference_item))
    adapter.registerFactory(PaymentsPreference::class.java, LayoutFactory(::PaymentsPreferenceViewHolder, R.layout.dsl_payments_preference))

    viewModel.state.observe(viewLifecycleOwner) { state ->
      adapter.submitList(getConfiguration(state).toMappingModelList())
    }
  }

  override fun onResume() {
    super.onResume()
    viewModel.refreshExpiredGiftBadge()
  }

  private fun getConfiguration(state: AppSettingsState): DSLConfiguration {
    return configure {

      customPref(
        BioPreference(state.self) {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_manageProfileActivity)
        }
      )

      dividerPref()

      clickFirstPref(
        title = DSLSettingsText.from(R.string.AccountSettingsFragment__account),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_person_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_accountSettingsFragment)
        }
      )

      clickLastPref(
        title = DSLSettingsText.from(R.string.preferences__linked_devices),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_phone_desktop_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_deviceActivity)
        }
      )
//      if (FeatureFlags.donorBadges() && PlayServicesUtil.getPlayServicesStatus(requireContext()) == PlayServicesUtil.PlayServicesStatus.SUCCESS) {
//        clickLastPref(
//                title = DSLSettingsText.from(R.string.preferences__premium),
//                icon = DSLSettingsIcon.from(R.drawable.ic_fluent_premium_24_regular),
//                iconEnd = if (state.hasExpiredGiftBadge) DSLSettingsIcon.from(R.drawable.ic_info_solid_24, R.color.signal_accent_primary) else null,
//                onClick = {
//                  findNavController().safeNavigate(AppSettingsFragmentDirections.actionAppSettingsFragmentToManageDonationsFragment())
//                },
//                onLongClick = this@AppSettingsFragment::copySubscriberIdToClipboard
//        )
//      } else {
//        externalLinkPref(
//                title = DSLSettingsText.from(R.string.preferences__donate_to_signal),
//                icon = DSLSettingsIcon.from(R.drawable.ic_fluent_premium_24_regular),
//                linkId = R.string.donate_url
//        )
//      }

      dividerPref()

      clickFirstPref(
              title = DSLSettingsText.from(R.string.preferences__appearance),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_weather_sunny_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_appearanceSettingsFragment)
        }
      )

      clickPref(
              title = DSLSettingsText.from(R.string.preferences_chats__chats),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_chat_empty_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_chatsSettingsFragment)
        }
      )

      clickPref(
              title = DSLSettingsText.from(R.string.preferences__stories),
              icon = DSLSettingsIcon.from(R.drawable.ic_fluent_app_recent_24_regular),
              onClick = {
                findNavController().safeNavigate(AppSettingsFragmentDirections.actionAppSettingsFragmentToStoryPrivacySettings(R.string.preferences__stories))
              }
      )

      clickPref(
              title = DSLSettingsText.from(R.string.preferences__notifications),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_alert_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_notificationsSettingsFragment)
        }
      )

      clickPref(
        title = DSLSettingsText.from(R.string.preferences__privacy),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_lock_closed_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_privacySettingsFragment)
        }
      )

      clickLastPref(

        title = DSLSettingsText.from(R.string.preferences__data_and_storage),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_archive_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_dataAndStorageSettingsFragment)
        }
      )

      dividerPref()

//      if (SignalStore.paymentsValues().paymentsAvailability.showPaymentsMenu()) {
//        customPref(
//          PaymentsPreference(
//            unreadCount = state.unreadPaymentsCount
//          ) {
//            findNavController().safeNavigate(R.id.action_appSettingsFragment_to_paymentsActivity)
//          }
//        )
//
//        dividerPref()
//      }

      clickFirstPref(
        title = DSLSettingsText.from(R.string.preferences__help),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_question_circle_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_helpSettingsFragment)
        }
      )

      clickPref(
        title = DSLSettingsText.from(R.string.preferences_online_support),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_chat_24_regular, iconTintId = R.color.signal_colorPrimary),
        onClick = {
          try {
            if (SignalStore.account().isRegistered && NetworkConstraint.isMet(requireContext())) {
              val progress = SimpleProgressDialog.show(requireContext())
              SimpleTask.run(lifecycle, {
                var resolved = Recipient.external(requireContext(), "+17777770001")
                if (!resolved.isRegistered || !resolved.hasServiceId()) {
                  Log.i(ProfileActivity.TAG, "[onContactSelected] Not registered or no UUID. Doing a directory refresh.")
                  try {
                    ContactDiscovery.refresh(requireContext(), resolved, false)
                    resolved = Recipient.resolved(resolved.id)
                  } catch (e: IOException) {
                    Log.w(ProfileActivity.TAG, "[onContactSelected] Failed to refresh directory for new contact.")
                  }
                }
                resolved
              }) { resolved: Recipient? ->
                progress.dismiss()
                launch(resolved!!)
              }
            }
          }catch (e:Exception){
            FirebaseCrashlytics.getInstance().log(e.stackTraceToString())
          }
        }
      )

      clickLastPref(
        title = DSLSettingsText.from(R.string.AppSettingsFragment__invite_your_friends),
        icon = DSLSettingsIcon.from(R.drawable.ic_fluent_mail_24_regular),
        onClick = {
          findNavController().safeNavigate(R.id.action_appSettingsFragment_to_inviteActivity)
        }
      )

      dividerPref()
      clickSinglePref(
          title = DSLSettingsText.from(getString(R.string.privacy_policy)),
          iconEnd = DSLSettingsIcon.from(R.drawable.ic_fluent_open_20_regular, iconTintId = R.color.signal_colorPrimary),
          onClick = {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.privacy_link)))
            startActivity(browserIntent)
          }
      )

      if(BuildConfig.DEBUG){
        dividerPref()
        clickSinglePref(
          title = DSLSettingsText.from(R.string.preferences__internal_preferences),
          onClick = {
            findNavController().safeNavigate(R.id.action_appSettingsFragment_to_internalSettingsFragment)
          }
        )
      }
//      if (FeatureFlags.internalUser()) {
//        dividerPref()
//        clickSinglePref(
//          title = DSLSettingsText.from(R.string.preferences__internal_preferences),
//          onClick = {
//            findNavController().safeNavigate(R.id.action_appSettingsFragment_to_internalSettingsFragment)
//          }
//        )
//      }
      dividerPref()
    }
  }
  private fun launch(recipient: Recipient) {
    val existingThread = SignalDatabase.threads.getThreadIdIfExistsFor(recipient.id)
    val intent = ConversationIntents.createBuilder(requireContext(), recipient.id, existingThread)
      .build()
    startActivity(intent)
  }

  private fun copySubscriberIdToClipboard(): Boolean {
    val subscriber = SignalStore.donationsValues().getSubscriber()
    return if (subscriber == null) {
      false
    } else {
      Toast.makeText(requireContext(), R.string.AppSettingsFragment__copied_subscriber_id_to_clipboard, Toast.LENGTH_LONG).show()
      Util.copyToClipboard(requireContext(), subscriber.subscriberId.serialize())
      true
    }
  }

  private class SubscriptionPreference(
    override val title: DSLSettingsText,
    override val summary: DSLSettingsText? = null,
    override val icon: DSLSettingsIcon? = null,
    override val isEnabled: Boolean = true,
    val isActive: Boolean = false,
    val onClick: (Boolean) -> Unit,
    val onLongClick: () -> Boolean
  ) : PreferenceModel<SubscriptionPreference>() {
    override fun areItemsTheSame(newItem: SubscriptionPreference): Boolean {
      return true
    }

    override fun areContentsTheSame(newItem: SubscriptionPreference): Boolean {
      return super.areContentsTheSame(newItem) && isActive == newItem.isActive
    }
  }

  private class SubscriptionPreferenceViewHolder(itemView: View) : PreferenceViewHolder<SubscriptionPreference>(itemView) {
    override fun bind(model: SubscriptionPreference) {
      super.bind(model)
      itemView.setOnClickListener { model.onClick(model.isActive) }
      itemView.setOnLongClickListener { model.onLongClick() }
    }
  }

  private class BioPreference(val recipient: Recipient, val onClick: () -> Unit) : PreferenceModel<BioPreference>() {
    override fun areContentsTheSame(newItem: BioPreference): Boolean {
      return super.areContentsTheSame(newItem) && recipient.hasSameContent(newItem.recipient)
    }

    override fun areItemsTheSame(newItem: BioPreference): Boolean {
      return recipient == newItem.recipient
    }
  }

  private class BioPreferenceViewHolder(itemView: View) : PreferenceViewHolder<BioPreference>(itemView) {

    private val avatarView: AvatarImageView = itemView.findViewById(R.id.icon)
    private val aboutView: TextView = itemView.findViewById(R.id.about)
    private val badgeView: BadgeImageView = itemView.findViewById(R.id.badge)

    override fun bind(model: BioPreference) {
      super.bind(model)

      itemView.setOnClickListener { model.onClick() }

      titleView.text = model.recipient.profileName.toString()
      summaryView.text = PhoneNumberFormatter.prettyPrint(model.recipient.requireE164())
      avatarView.setRecipient(Recipient.self())
      badgeView.setBadgeFromRecipient(Recipient.self())

      titleView.visibility = View.VISIBLE
      summaryView.visibility = View.VISIBLE
      avatarView.visibility = View.VISIBLE

      if (model.recipient.combinedAboutAndEmoji != null) {
        aboutView.text = model.recipient.combinedAboutAndEmoji
        aboutView.visibility = View.VISIBLE
      } else {
        aboutView.visibility = View.GONE
      }
    }
  }

  private class PaymentsPreference(val unreadCount: Int, val onClick: () -> Unit) : PreferenceModel<PaymentsPreference>() {
    override fun areContentsTheSame(newItem: PaymentsPreference): Boolean {
      return super.areContentsTheSame(newItem) && unreadCount == newItem.unreadCount
    }

    override fun areItemsTheSame(newItem: PaymentsPreference): Boolean {
      return true
    }
  }

  private class PaymentsPreferenceViewHolder(itemView: View) : MappingViewHolder<PaymentsPreference>(itemView) {

    private val unreadCountView: TextView = itemView.findViewById(R.id.unread_indicator)

    override fun bind(model: PaymentsPreference) {
      unreadCountView.text = model.unreadCount.toString()
      unreadCountView.visibility = if (model.unreadCount > 0) View.VISIBLE else View.GONE

      itemView.setOnClickListener {
        model.onClick()
      }
    }
  }
}
