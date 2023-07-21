package org.thoughtcrime.securesms.registration.fragments;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import org.thoughtcrime.securesms.R;
import org.thoughtcrime.securesms.registration.viewmodel.BaseRegistrationViewModel;
import org.thoughtcrime.securesms.registration.viewmodel.RegistrationViewModel;

public class AccountLockedFragment extends BaseAccountLockedFragment {

  public AccountLockedFragment() {
    super(R.layout.account_locked_fragment);
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    // Status bar color change
    Window window = requireActivity().getWindow();
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
      window.setStatusBarColor(ContextCompat.getColor(requireActivity(),R.color.white));
    }
    super.onCreate(savedInstanceState);
  }

  @Override
  protected BaseRegistrationViewModel getViewModel() {
    return new ViewModelProvider(requireActivity()).get(RegistrationViewModel.class);
  }

  @Override
  protected void onNext() {
    requireActivity().finish();
  }
}
