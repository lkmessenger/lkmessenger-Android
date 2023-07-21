package org.thoughtcrime.securesms.deeplinks;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

import org.thoughtcrime.securesms.MainActivity;
import org.thoughtcrime.securesms.PassphraseRequiredActivity;

public class DeepLinkEntryActivity extends PassphraseRequiredActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState, boolean ready) {
    FirebaseDynamicLinks.getInstance()
            .getDynamicLink(getIntent())
            .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>() {
              @Override
              public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData) {
                // Get deep link from result (may be null if no link is found)
                Uri deepLink = null;
                if (pendingDynamicLinkData != null) {
                  deepLink = pendingDynamicLinkData.getLink();
                }

                Intent intent = MainActivity.clearTop(DeepLinkEntryActivity.this);
//                Uri    data   = getIntent().getData();
                intent.setData(deepLink);
                startActivity(intent);
              }
            })
            .addOnFailureListener(this, new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {

              }
            });

  }
}
