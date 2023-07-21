package org.thoughtcrime.securesms.devicetransfer.newdevice;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.fragment.NavHostFragment;

import org.thoughtcrime.securesms.LoggingFragment;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.navigation.SafeNavigation;

/**
 * Shown after the new device successfully completes receiving a backup from the old device.
 */
public final class NewDeviceTransferCompleteFragment extends LoggingFragment {
  public NewDeviceTransferCompleteFragment() {
    super(R.layout.new_device_transfer_complete_fragment);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    view.findViewById(R.id.new_device_transfer_complete_fragment_continue_registration)
        .setOnClickListener(v -> SafeNavigation.safeNavigate(NavHostFragment.findNavController(this),
                                                             R.id.action_newDeviceTransferComplete_to_enterPhoneNumberFragment));

    // Status bar color change
    Window window = requireActivity().getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(ContextCompat.getColor(requireActivity(),R.color.white));
    }
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() { }
    });
  }
}
