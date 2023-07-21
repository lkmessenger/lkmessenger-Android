package org.thoughtcrime.securesms.conversation;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import org.thoughtcrime.securesms.R;

public enum AttachmentKeyboardButton {

  GALLERY(R.string.AttachmentKeyboard_gallery, R.drawable.ic_fluent_image_multiple_24_regular),
  FILE(R.string.AttachmentKeyboard_file, R.drawable.ic_fluent_document_24_regular),
  PAYMENT(R.string.AttachmentKeyboard_payment, R.drawable.ic_fluent_payment_24_regular),
  CONTACT(R.string.AttachmentKeyboard_contact, R.drawable.ic_fluent_person_24_regular),
  LOCATION(R.string.AttachmentKeyboard_location, R.drawable.ic_fluent_my_location_24_regular);

  private final int titleRes;
  private final int iconRes;

  AttachmentKeyboardButton(@StringRes int titleRes, @DrawableRes int iconRes) {
    this.titleRes = titleRes;
    this.iconRes = iconRes;
  }

  public @StringRes int getTitleRes() {
    return titleRes;
  }

  public @DrawableRes int getIconRes() {
    return iconRes;
  }
}
