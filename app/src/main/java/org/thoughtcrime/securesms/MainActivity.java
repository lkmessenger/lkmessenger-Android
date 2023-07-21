package org.thoughtcrime.securesms;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import org.linkmessenger.base.ui.AndroidUtilities;
import org.linkmessenger.notifications.NotificationUtil;
import org.linkmessenger.utils.PostUtil;
import org.thoughtcrime.securesms.components.voice.VoiceNoteMediaController;
import org.thoughtcrime.securesms.components.voice.VoiceNoteMediaControllerOwner;
import org.thoughtcrime.securesms.devicetransfer.olddevice.OldDeviceTransferLockedDialog;
import org.thoughtcrime.securesms.groups.ui.creategroup.CreateGroupActivity;
import org.thoughtcrime.securesms.keyvalue.PhoneNumberPrivacyValues;
import org.thoughtcrime.securesms.keyvalue.SignalStore;
import org.thoughtcrime.securesms.storage.StorageSyncHelper;
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabRepository;
import org.thoughtcrime.securesms.stories.tabs.ConversationListTabsViewModel;
import org.thoughtcrime.securesms.util.AppStartup;
import org.thoughtcrime.securesms.util.CachedInflater;
import org.thoughtcrime.securesms.util.CommunicationActions;
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme;
import org.thoughtcrime.securesms.util.DynamicTheme;
import org.thoughtcrime.securesms.util.SplashScreenUtil;
import org.thoughtcrime.securesms.util.WindowUtil;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.onesignal.OneSignal;

public class MainActivity extends PassphraseRequiredActivity implements VoiceNoteMediaControllerOwner, ContactSelectionListFragment.ListCallback {

  public static final int RESULT_CONFIG_CHANGED = Activity.RESULT_FIRST_USER + 901;

  private static final String ONESIGNAL_APP_ID = BuildConfig.ONESIGNAL_APP_ID;

  private final DynamicTheme  dynamicTheme = new DynamicNoActionBarTheme();
  private final MainNavigator navigator    = new MainNavigator(this);

  private VoiceNoteMediaController      mediaController;
  private ConversationListTabsViewModel conversationListTabsViewModel;

  SharedPreferences sharedPref;


  public static @NonNull Intent clearTop(@NonNull Context context) {
    Intent intent = new Intent(context, MainActivity.class);

    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);

    return intent;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    AppStartup.getInstance().onCriticalRenderEventStart();

    super.onCreate(savedInstanceState, ready);


    // Enable verbose OneSignal logging to debug issues if needed.
    OneSignal.setLogLevel(OneSignal.LOG_LEVEL.VERBOSE, OneSignal.LOG_LEVEL.NONE);

    // OneSignal Initialization
    OneSignal.initWithContext(this);
    OneSignal.setAppId(ONESIGNAL_APP_ID);

    // promptForPushNotifications will show the native Android notification permission prompt.
    // We recommend removing the following code and instead using an In-App Message to prompt for notification permission (See step 7)
    OneSignal.promptForPushNotifications();

    setContentView(R.layout.main_activity);

    mediaController = new VoiceNoteMediaController(this);

    ConversationListTabRepository         repository = new ConversationListTabRepository();
    ConversationListTabsViewModel.Factory factory    = new ConversationListTabsViewModel.Factory(repository);

    handleGroupLinkInIntent(getIntent());
    handleProxyInIntent(getIntent());
    handleSignalMeIntent(getIntent());
    handlePostLinkInIntent(getIntent());

    CachedInflater.from(this).clear();

    conversationListTabsViewModel = new ViewModelProvider(this, factory).get(ConversationListTabsViewModel.class);
    updateTabVisibility();

    try {
      NotificationUtil.INSTANCE.clearNotifications(this);
    }catch (Exception ignored){

    }

    initFireBaseRemoteConfig();
//
//    try {
//      sharedPref = getSharedPreferences("new_settings", Context.MODE_PRIVATE);
//      if(!sharedPref.getBoolean("hide_number", false)){
//        SignalStore.phoneNumberPrivacy().setPhoneNumberSharingMode(PhoneNumberPrivacyValues.PhoneNumberSharingMode.NOBODY);
//        StorageSyncHelper.scheduleSyncForDataChange();
//        sharedPref.edit().putBoolean("hide_number", true).apply();
//      }
//    }catch (Exception ignored){
//
//    }
  }

  private void initFireBaseRemoteConfig() {
    FirebaseRemoteConfig mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
    FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build();
    mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings);

    mFirebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_defaults);

    mFirebaseRemoteConfig.fetchAndActivate()
            .addOnCompleteListener(this, task -> {
              if (task.isSuccessful()) {
                boolean updated = task.getResult();
                if(updated){
                  mFirebaseRemoteConfig.getBoolean("android_pnp");
                }
              }
            });
  }

  @Override
  public Intent getIntent() {
    return super.getIntent().setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                                      Intent.FLAG_ACTIVITY_NEW_TASK |
                                      Intent.FLAG_ACTIVITY_SINGLE_TOP);
  }

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    handlePostLinkInIntent(intent);
    handleGroupLinkInIntent(intent);
    handleProxyInIntent(intent);
    handleSignalMeIntent(intent);
  }

  @Override
  protected void onPreCreate() {
    super.onPreCreate();
    dynamicTheme.onCreate(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    try {
      AndroidUtilities.checkDisplaySize(this, getResources().getConfiguration());
    }catch (Exception e){
      FirebaseCrashlytics.getInstance().log(e.getMessage()!=null?e.getMessage():"checkDisplaySize error");
    }
    dynamicTheme.onResume(this);
    if (SignalStore.misc().isOldDeviceTransferLocked()) {
      OldDeviceTransferLockedDialog.show(getSupportFragmentManager());
    }

    updateTabVisibility();
  }

  @Override
  protected void onStop() {
    super.onStop();
    SplashScreenUtil.setSplashScreenThemeIfNecessary(this, SignalStore.settings().getTheme());
  }

  @Override
  public void onBackPressed() {
    if (!navigator.onBackPressed()) {
      super.onBackPressed();
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == MainNavigator.REQUEST_CONFIG_CHANGES && resultCode == RESULT_CONFIG_CHANGED) {
      recreate();
    }
  }

  private void updateTabVisibility() {
    findViewById(R.id.conversation_list_tabs).setVisibility(View.VISIBLE);
    WindowUtil.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.signal_colorBackground));
//    if (Stories.isFeatureEnabled()) {
//      findViewById(R.id.conversation_list_tabs).setVisibility(View.VISIBLE);
//      WindowUtil.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.signal_colorBackground));
//    } else {
//      findViewById(R.id.conversation_list_tabs).setVisibility(View.GONE);
//      WindowUtil.setNavigationBarColor(this, ContextCompat.getColor(this, R.color.signal_colorBackground));
//      conversationListTabsViewModel.onChatsSelected();
//    }
  }

  public @NonNull MainNavigator getNavigator() {
    return navigator;
  }

  private void handlePostLinkInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      PostUtil.INSTANCE.handlePostUrl(this, data.toString());
      PostUtil.INSTANCE.handleUserUrl(this, data.toString());
    }
  }
  private void handleGroupLinkInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialGroupLinkUrl(this, data.toString());
    }
  }

  private void handleProxyInIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialProxyLinkUrl(this, data.toString());
    }
  }

  private void handleSignalMeIntent(Intent intent) {
    Uri data = intent.getData();
    if (data != null) {
      CommunicationActions.handlePotentialSignalMeUrl(this, data.toString());
    }
  }

  @Override
  public @NonNull VoiceNoteMediaController getVoiceNoteMediaController() {
    return mediaController;
  }

  @Override
  public void onInvite() {
    handleInvite();
  }

  @Override
  public void onNewGroup(boolean forceV1) {
    handleCreateGroup();
  }

  private void handleCreateGroup() {
    startActivity(CreateGroupActivity.newIntent(this));
  }

  private void handleInvite() {
    startActivity(new Intent(this, InviteActivity.class));
  }
}
