package org.thoughtcrime.securesms;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;

import org.thoughtcrime.securesms.conversationlist.model.ConversationSet;
import org.thoughtcrime.securesms.database.model.ThreadRecord;
import org.thoughtcrime.securesms.mms.GlideRequests;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface BindableConversationListItem extends Unbindable {

  void bind(@NonNull LifecycleOwner lifecycleOwner,
            @NonNull ThreadRecord thread,
            @NonNull GlideRequests glideRequests, @NonNull Locale locale,
            @NonNull Set<Long> typingThreads,
            @NonNull Set<Long> onlineThreads,
            @NonNull Map<Long, Integer> verifiedThreads,
            @NonNull ConversationSet selectedConversations);

  void setSelectedConversations(@NonNull ConversationSet conversations);
  void updateTypingIndicator(@NonNull Set<Long> typingThreads);
  void updateOnlineIndicator(@NonNull Set<Long> onlineThreads);
  void updateVerifiedIndicator(@NonNull Map<Long, Integer> verifiedThreads);
}
