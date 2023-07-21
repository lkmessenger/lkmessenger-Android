package org.thoughtcrime.securesms.megaphone;

import org.thoughtcrime.securesms.keyvalue.SignalStore;

final class SignalPinReminderSchedule implements MegaphoneSchedule {

  @Override
  public boolean shouldDisplay(int seenCount, long lastSeen, long firstVisible, long currentTime) {
    return false;
  }
}
