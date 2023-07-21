package org.thoughtcrime.securesms.devicetransfer.newdevice;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;

import org.greenrobot.eventbus.EventBus;
import org.signal.devicetransfer.TransferStatus;
import org.thoughtcrime.securesms.LoggingFragment;
import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.util.navigation.SafeNavigation;

/**
 * Shows instructions for new device to being transfer.
 */
public final class NewDeviceTransferInstructionsFragment extends LoggingFragment {
  public NewDeviceTransferInstructionsFragment() {
    super(R.layout.new_device_transfer_instructions_fragment);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    // Status bar color change
    Window window = requireActivity().getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(ContextCompat.getColor(requireActivity(),R.color.white));
    }

    view.findViewById(R.id.new_device_transfer_instructions_fragment_continue)
        .setOnClickListener(v -> SafeNavigation.safeNavigate(Navigation.findNavController(v), R.id.action_device_transfer_setup));
  }

  @Override
  public void onResume() {
    super.onResume();
    EventBus.getDefault().removeStickyEvent(TransferStatus.class);
  }
}
