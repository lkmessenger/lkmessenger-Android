package org.thoughtcrime.securesms.components.settings.app.privacy

import org.thoughtcrime.securesms.keyvalue.PhoneNumberPrivacyValues
import org.whispersystems.signalservice.internal.push.OnlineStatusPrivacy

data class PrivacySettingsState(
  val blockedCount: Int,
  val seeMyPhoneNumber: PhoneNumberPrivacyValues.PhoneNumberSharingMode,
  val findMeByPhoneNumber: PhoneNumberPrivacyValues.PhoneNumberListingMode,
  val readReceipts: Boolean,
  val typingIndicators: Boolean,
  val screenLock: Boolean,
  val screenLockActivityTimeout: Long,
  val screenSecurity: Boolean,
  val incognitoKeyboard: Boolean,
  val paymentLock: Boolean,
  val isObsoletePasswordEnabled: Boolean,
  val isObsoletePasswordTimeoutEnabled: Boolean,
  val obsoletePasswordTimeout: Int,
  val universalExpireTimer: Int,
  val showPhoneNumber: Boolean,
  val messengerAccess: Boolean,
  val onlineStatusPrivacy:Boolean
)
