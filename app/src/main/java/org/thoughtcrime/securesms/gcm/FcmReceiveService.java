package org.thoughtcrime.securesms.gcm;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.linkmessenger.notifications.NotificationUtil;
import org.linkmessenger.request.models.NotificationData;
import org.signal.core.util.logging.Log;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies;
import org.thoughtcrime.securesms.jobs.FcmRefreshJob;
import org.thoughtcrime.securesms.jobs.SubmitRateLimitPushChallengeJob;
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.registration.PushChallengeRequest;
import org.thoughtcrime.securesms.util.FeatureFlags;
import org.thoughtcrime.securesms.util.NetworkUtil;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class FcmReceiveService extends FirebaseMessagingService {

//  private Lazy<ProfileJavaPresenter> profileJavaPresenter = inject(ProfileJavaPresenter.class);
  private static final String TAG = Log.tag(FcmReceiveService.class);

  private static final long FCM_FOREGROUND_INTERVAL = TimeUnit.MINUTES.toMillis(3);

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    Log.i(TAG, String.format(Locale.US,
                             "onMessageReceived() ID: %s, Delay: %d, Priority: %d, Original Priority: %d, Network: %s",
                             remoteMessage.getMessageId(),
                             (System.currentTimeMillis() - remoteMessage.getSentTime()),
                             remoteMessage.getPriority(),
                             remoteMessage.getOriginalPriority(),
                             NetworkUtil.getNetworkStatus(this)));

    String registrationChallenge = remoteMessage.getData().get("challenge");
    String rateLimitChallenge    = remoteMessage.getData().get("rateLimitChallenge");

    String notifyType = remoteMessage.getData().get("type");

    android.util.Log.d("NOTIFIAas", "getData: " + remoteMessage.getData());
    android.util.Log.d("NOTIFIAas", "getSenderId: " + remoteMessage.getSenderId());
    android.util.Log.d("NOTIFIAas", "getMessageId: " + remoteMessage.getMessageId());
    android.util.Log.d("NOTIFIAas", "getPriority: " + remoteMessage.getPriority());
    android.util.Log.d("NOTIFIAas", "getMessageType: " + remoteMessage.getMessageType());
    android.util.Log.d("NOTIFIAas", "getNotification: " + remoteMessage.getNotification());
    android.util.Log.d("NOTIFIAas", "getTo: " + remoteMessage.getTo());
    android.util.Log.d("NOTIFIAas", "getFrom: " + remoteMessage.getFrom());
    android.util.Log.d("NOTIFIAas", "getTtl: " + remoteMessage.getTtl());

    if (registrationChallenge != null) {
      handleRegistrationPushChallenge(registrationChallenge);
    } else if (rateLimitChallenge != null) {
      handleRateLimitPushChallenge(rateLimitChallenge);
    } else {

      if(Objects.equals(notifyType, "REACTION") ||
              Objects.equals(notifyType, "FOLLOW") ||
              Objects.equals(notifyType, "COMMENT") ||
              Objects.equals(notifyType, "ACCEPT_CHAT_REQUEST") ||
              Objects.equals(notifyType, "CHAT_REQUEST")){
        try {
          final NotificationData data = new NotificationData(
                  remoteMessage.getData().get("id"),
                  remoteMessage.getData().get("avatar"),
                  remoteMessage.getData().get("user_id"),
                  remoteMessage.getData().get("post_id"),
                  remoteMessage.getData().get("username"),
                  remoteMessage.getData().get("type"),
                  remoteMessage.getData().get("profileName"),
                  remoteMessage.getData().get("comment"),
                  remoteMessage.getData().get("comment_id")
          );

          String title = "";
          String text = "";
          if(data.getUsername() == null || Objects.requireNonNull(data.getUsername()).isEmpty()){
            text = data.getProfileName();
          }else{
            text = data.getUsername();
          }

          switch (notifyType) {
            case "REACTION":
              title = text;
              text = getApplication().getApplicationContext().getString(R.string.notification_liked_post);
              break;
            case "FOLLOW":
              title = text;
              text = getApplication().getApplicationContext().getString(R.string.notification_started_following);
              break;
            case "COMMENT":
              title = text;
              text = getApplication().getApplicationContext().getString(R.string.notification_commented) + "\"" + data.getComment() + "\"";
              break;
            case "CHAT_REQUEST":
              title = text;
              text = getApplication().getApplicationContext().getString(R.string.ThreadRecord_message_request);
              break;
            case "ACCEPT_CHAT_REQUEST":
              title = text;
              text = getApplication().getApplicationContext().getString(R.string.answered_to_request);
              break;
          }

          NotificationUtil.INSTANCE.
                  showNotification(ApplicationDependencies.getApplication(), Objects.requireNonNull(title), Objects.requireNonNull(text), notifyType, data);

        }catch (Exception e){
          FirebaseCrashlytics.getInstance().log(e.getMessage()!=null?e.getMessage():"Notification error");
        }
      }else{
        handleReceivedNotification(ApplicationDependencies.getApplication(), remoteMessage);
      }
    }
  }

  @Override
  public void onDeletedMessages() {
    Log.w(TAG, "onDeleteMessages() -- Messages may have been dropped. Doing a normal message fetch.");
    handleReceivedNotification(ApplicationDependencies.getApplication(), null);
  }

  @Override
  public void onNewToken(String token) {
    Log.i(TAG, "onNewToken()");

    if (!SignalStore.account().isRegistered()) {
      Log.i(TAG, "Got a new FCM token, but the user isn't registered.");
      return;
    }
//    try {
//      profileJavaPresenter.getValue().repository.updateToken(token);
//    }catch (Exception ignored){
//
//    }
    ApplicationDependencies.getJobManager().add(new FcmRefreshJob());
  }

  @Override
  public void onMessageSent(@NonNull String s) {
    Log.i(TAG, "onMessageSent()" + s);
  }

  @Override
  public void onSendError(@NonNull String s, @NonNull Exception e) {
    Log.w(TAG, "onSendError()", e);
  }

  private static void handleReceivedNotification(Context context, @Nullable RemoteMessage remoteMessage) {
    boolean enqueueSuccessful = false;

    try {
      long timeSinceLastRefresh = System.currentTimeMillis() - SignalStore.misc().getLastFcmForegroundServiceTime();
      Log.d(TAG, String.format(Locale.US, "[handleReceivedNotification] API: %s, FeatureFlag: %s, RemoteMessagePriority: %s, TimeSinceLastRefresh: %s ms", Build.VERSION.SDK_INT, FeatureFlags.useFcmForegroundService(), remoteMessage != null ? remoteMessage.getPriority() : "n/a", timeSinceLastRefresh));

      if (FeatureFlags.useFcmForegroundService() && Build.VERSION.SDK_INT >= 31 && remoteMessage != null && remoteMessage.getPriority() == RemoteMessage.PRIORITY_HIGH && timeSinceLastRefresh > FCM_FOREGROUND_INTERVAL) {
        enqueueSuccessful = FcmFetchManager.enqueue(context, true);
        SignalStore.misc().setLastFcmForegroundServiceTime(System.currentTimeMillis());
      } else if (Build.VERSION.SDK_INT < 26 || remoteMessage == null || remoteMessage.getPriority() == RemoteMessage.PRIORITY_HIGH) {
        enqueueSuccessful = FcmFetchManager.enqueue(context, false);
      }
    } catch (Exception e) {
      Log.w(TAG, "Failed to start service.", e);
      enqueueSuccessful = false;
    }

    if (!enqueueSuccessful) {
      Log.w(TAG, "Unable to start service. Falling back to legacy approach.");
      FcmFetchManager.retrieveMessages(context);
    }
  }

  private static void handleRegistrationPushChallenge(@NonNull String challenge) {
    Log.d(TAG, "Got a registration push challenge.");
    PushChallengeRequest.postChallengeResponse(challenge);
  }

  private static void handleRateLimitPushChallenge(@NonNull String challenge) {
    Log.d(TAG, "Got a rate limit push challenge.");
    ApplicationDependencies.getJobManager().add(new SubmitRateLimitPushChallengeJob(challenge));
  }
}