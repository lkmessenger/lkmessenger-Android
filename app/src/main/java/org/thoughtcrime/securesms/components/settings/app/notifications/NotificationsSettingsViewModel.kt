package org.thoughtcrime.securesms.components.settings.app.notifications

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.linkmessenger.profile.repository.ProfileRepository
import org.linkmessenger.request.models.NotificationSettingsParams
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.thoughtcrime.securesms.notifications.NotificationChannels
import org.thoughtcrime.securesms.preferences.widgets.NotificationPrivacyPreference
import org.thoughtcrime.securesms.util.TextSecurePreferences
import org.thoughtcrime.securesms.util.livedata.Store

class NotificationsSettingsViewModel(
  private val sharedPreferences: SharedPreferences,
  private val profileRepository: ProfileRepository
) : ViewModel() {
  init {
    if (NotificationChannels.supported()) {
      SignalStore.settings().messageNotificationSound = NotificationChannels.getMessageRingtone(ApplicationDependencies.getApplication())
      SignalStore.settings().isMessageVibrateEnabled = NotificationChannels.getMessageVibrate(ApplicationDependencies.getApplication())
    }
  }

  private val store = Store(getState())

  val state: LiveData<NotificationsSettingsState> = store.stateLiveData

  fun setMessageNotificationsEnabled(enabled: Boolean) {
    SignalStore.settings().isMessageNotificationsEnabled = enabled
    store.update { getState() }
  }

  fun setMessageNotificationsSound(sound: Uri?) {
    val messageSound = sound ?: Uri.EMPTY
    SignalStore.settings().messageNotificationSound = messageSound
    NotificationChannels.updateMessageRingtone(ApplicationDependencies.getApplication(), messageSound)
    store.update { getState() }
  }

  fun setMessageNotificationVibration(enabled: Boolean) {
    SignalStore.settings().isMessageVibrateEnabled = enabled
    NotificationChannels.updateMessageVibrate(ApplicationDependencies.getApplication(), enabled)
    store.update { getState() }
  }

  fun setMessageNotificationLedColor(color: String) {
    SignalStore.settings().messageLedColor = color
    NotificationChannels.updateMessagesLedColor(ApplicationDependencies.getApplication(), color)
    store.update { getState() }
  }

  fun setMessageNotificationLedBlink(blink: String) {
    SignalStore.settings().messageLedBlinkPattern = blink
    store.update { getState() }
  }

  fun setMessageNotificationInChatSoundsEnabled(enabled: Boolean) {
    SignalStore.settings().isMessageNotificationsInChatSoundsEnabled = enabled
    store.update { getState() }
  }

  fun setMessageRepeatAlerts(repeats: Int) {
    SignalStore.settings().messageNotificationsRepeatAlerts = repeats
    store.update { getState() }
  }

  fun setMessageNotificationPrivacy(preference: String) {
    SignalStore.settings().messageNotificationsPrivacy = NotificationPrivacyPreference(preference)
    store.update { getState() }
  }

  fun setMessageNotificationPriority(priority: Int) {
    sharedPreferences.edit().putString(TextSecurePreferences.NOTIFICATION_PRIORITY_PREF, priority.toString()).apply()
    store.update { getState() }
  }
  private fun changeNotificationSettings(){
    val params = NotificationSettingsParams(comment = SignalStore.settings().isPostCommentedNotificationEnabled,
    reaction = SignalStore.settings().isLikeNotificationEnabled,
    follow = SignalStore.settings().isNewFollowerNotificationEnabled)
    CoroutineScope(Dispatchers.IO).launch {
      try {
         profileRepository.changePostNotificationSettings(params)
      }catch (_:Exception){

      }
    }
  }
  fun setNewFollowerNotificationEnabled(enabled: Boolean) {
    SignalStore.settings().isNewFollowerNotificationEnabled = enabled
    store.update { getState() }
    changeNotificationSettings()
  }

  fun setLikeNotificationEnabled(enabled: Boolean) {
    SignalStore.settings().isLikeNotificationEnabled = enabled
    store.update { getState() }
    changeNotificationSettings()
  }

  fun setPostCommentedNotification(enabled: Boolean) {
    SignalStore.settings().isPostCommentedNotificationEnabled = enabled
    store.update { getState() }
    changeNotificationSettings()
  }

  fun setCallNotificationsEnabled(enabled: Boolean) {
    SignalStore.settings().isCallNotificationsEnabled = enabled
    store.update { getState() }
  }

  fun setCallRingtone(ringtone: Uri?) {
    SignalStore.settings().callRingtone = ringtone ?: Uri.EMPTY
    store.update { getState() }
  }

  fun setCallVibrateEnabled(enabled: Boolean) {
    SignalStore.settings().isCallVibrateEnabled = enabled
    store.update { getState() }
  }

  fun setNotifyWhenContactJoinsSignal(enabled: Boolean) {
    SignalStore.settings().isNotifyWhenContactJoinsSignal = enabled
    store.update { getState() }
  }

  private fun getState(): NotificationsSettingsState = NotificationsSettingsState(
    messageNotificationsState = MessageNotificationsState(
      notificationsEnabled = SignalStore.settings().isMessageNotificationsEnabled,
      sound = SignalStore.settings().messageNotificationSound,
      vibrateEnabled = SignalStore.settings().isMessageVibrateEnabled,
      ledColor = SignalStore.settings().messageLedColor,
      ledBlink = SignalStore.settings().messageLedBlinkPattern,
      inChatSoundsEnabled = SignalStore.settings().isMessageNotificationsInChatSoundsEnabled,
      repeatAlerts = SignalStore.settings().messageNotificationsRepeatAlerts,
      messagePrivacy = SignalStore.settings().messageNotificationsPrivacy.toString(),
      priority = TextSecurePreferences.getNotificationPriority(ApplicationDependencies.getApplication())
    ),
    postNotificationsState = PostNotificationsState(
      newFollowerNotificationEnabled = SignalStore.settings().isNewFollowerNotificationEnabled,
            likeNotificationEnabled = SignalStore.settings().isLikeNotificationEnabled,
      postCommentedNotification = SignalStore.settings().isPostCommentedNotificationEnabled
    ),
    callNotificationsState = CallNotificationsState(
      notificationsEnabled = SignalStore.settings().isCallNotificationsEnabled,
      ringtone = SignalStore.settings().callRingtone,
      vibrateEnabled = SignalStore.settings().isCallVibrateEnabled
    ),
    notifyWhenContactJoinsSignal = SignalStore.settings().isNotifyWhenContactJoinsSignal
  )

  class Factory(
    private val sharedPreferences: SharedPreferences,
    private val profileRepository: ProfileRepository
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return requireNotNull(modelClass.cast(NotificationsSettingsViewModel(sharedPreferences, profileRepository)))
    }
  }
}
