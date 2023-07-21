package org.thoughtcrime.securesms.conversation

import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.request.models.ChangeProfileParams
import org.thoughtcrime.securesms.PassphraseRequiredActivity
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.HidingLinearLayout
import org.thoughtcrime.securesms.components.reminder.ReminderView
import org.thoughtcrime.securesms.components.settings.app.subscription.DonationPaymentComponent
import org.thoughtcrime.securesms.components.settings.app.subscription.DonationPaymentRepository
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme
import org.thoughtcrime.securesms.util.DynamicTheme
import org.thoughtcrime.securesms.util.views.Stub

open class ConversationActivity : PassphraseRequiredActivity(), ConversationParentFragment.Callback, DonationPaymentComponent, KoinComponent {

  companion object {
    private const val STATE_WATERMARK = "share_data_watermark"
  }

  private lateinit var fragment: ConversationParentFragment
  private var shareDataTimestamp: Long = -1L

  private val profileRepository:ProfileRepository = get()

  private val dynamicTheme: DynamicTheme = DynamicNoActionBarTheme()
  override fun onPreCreate() {
    dynamicTheme.onCreate(this)
  }

  override fun onCreate(savedInstanceState: Bundle?, ready: Boolean) {
    shareDataTimestamp = savedInstanceState?.getLong(STATE_WATERMARK, -1L) ?: -1L

    setContentView(R.layout.conversation_parent_fragment_container)

    if (savedInstanceState == null) {
      replaceFragment(intent!!)
    } else {
      fragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as ConversationParentFragment
    }

    if(!SignalStore.account().socialRegistred){
      try {
        profileRepository.uploadProfile(ChangeProfileParams(Recipient.self().profileName.joinName, "", Recipient.self().about), null)
      }catch (_:Exception){

      }
    }
  }

  override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    outState.putLong(STATE_WATERMARK, shareDataTimestamp)
  }

  override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)

    setIntent(intent)
    replaceFragment(intent!!)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    googlePayResultPublisher.onNext(DonationPaymentComponent.GooglePayResult(requestCode, resultCode, data))
  }

  private fun replaceFragment(intent: Intent) {
    fragment = ConversationParentFragment.create(intent)
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .disallowAddToBackStack()
      .commitNowAllowingStateLoss()
  }

  override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    return fragment.dispatchTouchEvent(ev) || super.dispatchTouchEvent(ev)
  }

  override fun onResume() {
    super.onResume()
    dynamicTheme.onResume(this)
  }

  override fun getShareDataTimestamp(): Long {
    return shareDataTimestamp
  }

  override fun setShareDataTimestamp(timestamp: Long) {
    shareDataTimestamp = timestamp
  }

  override fun onInitializeToolbar(toolbar: Toolbar) {
    toolbar.navigationIcon = AppCompatResources.getDrawable(this, R.drawable.ic_fluent_chevron_left_24_regular)
    toolbar.setNavigationOnClickListener { finish() }
  }

  fun getRecipient(): Recipient {
    return fragment.recipient
  }

  fun getTitleView(): View {
    return fragment.titleView
  }

  fun getComposeText(): View {
    return fragment.composeText
  }

  fun getQuickAttachmentToggle(): HidingLinearLayout {
    return fragment.quickAttachmentToggle
  }

  fun getReminderView(): Stub<ReminderView> {
    return fragment.reminderView
  }

  override val donationPaymentRepository: DonationPaymentRepository by lazy { DonationPaymentRepository(this) }
  override val googlePayResultPublisher: Subject<DonationPaymentComponent.GooglePayResult> = PublishSubject.create()
}
