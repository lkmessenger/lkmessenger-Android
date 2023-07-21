package org.thoughtcrime.securesms.keyvalue;

import android.content.Context;

import androidx.annotation.NonNull;

import org.thoughtcrime.securesms.util.LocaleFeatureFlags;
import org.thoughtcrime.securesms.util.Util;

import java.util.Collections;
import java.util.List;

public final class OnboardingValues extends SignalStoreValues {

  private static final String SHOW_NEW_GROUP      = "onboarding.new_group";
  private static final String SHOW_INVITE_FRIENDS = "onboarding.invite_friends";
  private static final String SHOW_SMS            = "onboarding.sms";
  private static final String SHOW_APPEARANCE     = "onboarding.appearance";
  private static final String SHOW_ADD_PHOTO      = "onboarding.add_photo";

  OnboardingValues(@NonNull KeyValueStore store) {
    super(store);
  }

  @Override
  void onFirstEverAppLaunch() {
    putBoolean(SHOW_NEW_GROUP, false);
    putBoolean(SHOW_INVITE_FRIENDS, false);
    putBoolean(SHOW_SMS, false);
    putBoolean(SHOW_APPEARANCE, false);
    putBoolean(SHOW_ADD_PHOTO, false);
  }

  @Override
  @NonNull List<String> getKeysToIncludeInBackup() {
    return Collections.emptyList();
  }

  public void clearAll() {
    setShowNewGroup(false);
    setShowInviteFriends(false);
    setShowSms(false);
    setShowAppearance(false);
    setShowAddPhoto(false);
  }

  public boolean hasOnboarding(@NonNull Context context) {
    return shouldShowNewGroup()      ||
           shouldShowInviteFriends() ||
           shouldShowSms()           ||
           shouldShowAppearance()    ||
           shouldShowAddPhoto();
  }

  public void setShowNewGroup(boolean value) {
    putBoolean(SHOW_NEW_GROUP, false);
  }

  public boolean shouldShowNewGroup() {
    return getBoolean(SHOW_NEW_GROUP, false);
  }

  public void setShowInviteFriends(boolean value) {
    putBoolean(SHOW_INVITE_FRIENDS, false);
  }

  public boolean shouldShowInviteFriends() {
    return getBoolean(SHOW_INVITE_FRIENDS, false);
  }

  public void setShowSms(boolean value) {
    putBoolean(SHOW_SMS, false);
  }

  public boolean shouldShowSms() {
    return false;
  }

  public void setShowAppearance(boolean value) {
    putBoolean(SHOW_APPEARANCE, false);
  }

  public boolean shouldShowAppearance() {
    return getBoolean(SHOW_APPEARANCE, false);
  }

  public void setShowAddPhoto(boolean value) {
    putBoolean(SHOW_ADD_PHOTO, false);
  }

  public boolean shouldShowAddPhoto(){
    return getBoolean(SHOW_ADD_PHOTO, false);
  }
}
