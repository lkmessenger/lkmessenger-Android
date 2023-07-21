package org.thoughtcrime.securesms.components.settings

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.CallSuper
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.settings.models.AsyncSwitch
import org.thoughtcrime.securesms.components.settings.models.Button
import org.thoughtcrime.securesms.components.settings.models.Space
import org.thoughtcrime.securesms.components.settings.models.Text
import org.thoughtcrime.securesms.util.CommunicationActions
import org.thoughtcrime.securesms.util.ViewUtil
import org.thoughtcrime.securesms.util.adapter.mapping.LayoutFactory
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.adapter.mapping.MappingViewHolder
import org.thoughtcrime.securesms.util.views.LearnMoreTextView
import org.thoughtcrime.securesms.util.visible

class DSLSettingsAdapter : MappingAdapter() {
  init {
    registerFactory(ClickPreference::class.java, LayoutFactory(::ClickPreferenceViewHolder, R.layout.dsl_preference_item))
    registerFactory(ClickFirstPreference::class.java, LayoutFactory(::ClickFirstPreferenceViewHolder, R.layout.dsl_preference_first_item))
    registerFactory(ClickSinglePreference::class.java, LayoutFactory(::ClickSinglePreferenceViewHolder, R.layout.dsl_preference_item_single))
    registerFactory(ClickLastPreference::class.java, LayoutFactory(::ClickLastPreferenceViewHolder, R.layout.dsl_preference_last_item))
    registerFactory(ClickStoryPrivacyPreference::class.java, LayoutFactory(::ClickStoryPrivacyPreferenceViewHolder, R.layout.dsl_preference_item_single))
    registerFactory(LongClickPreference::class.java, LayoutFactory(::LongClickPreferenceViewHolder, R.layout.dsl_preference_item))
    registerFactory(TextPreference::class.java, LayoutFactory(::TextPreferenceViewHolder, R.layout.dsl_preference_item))
    registerFactory(TextFirstPreference::class.java, LayoutFactory(::TextFirstPreferenceViewHolder, R.layout.dsl_preference_first_item))
    registerFactory(TextLastPreference::class.java, LayoutFactory(::TextLastPreferenceViewHolder, R.layout.dsl_preference_last_item))
    registerFactory(LearnMoreTextPreference::class.java, LayoutFactory(::LearnMoreTextPreferenceViewHolder, R.layout.dsl_learn_more_preference_item))
    registerFactory(RadioListPreference::class.java, LayoutFactory(::RadioListPreferenceViewHolder, R.layout.dsl_preference_item))
    registerFactory(RadioListSinglePreference::class.java, LayoutFactory(::RadioListSinglePreferenceViewHolder, R.layout.dsl_preference_item_single))
    registerFactory(RadioListLastPreference::class.java, LayoutFactory(::RadioListLastPreferenceViewHolder, R.layout.dsl_preference_last_item))
    registerFactory(RadioListFirstPreference::class.java, LayoutFactory(::RadioListFirstPreferenceViewHolder, R.layout.dsl_preference_first_item))
    registerFactory(MultiSelectListPreference::class.java, LayoutFactory(::MultiSelectListPreferenceViewHolder, R.layout.dsl_preference_item))
    registerFactory(MultiSelectListLastPreference::class.java, LayoutFactory(::MultiSelectListLastPreferenceViewHolder, R.layout.dsl_preference_last_item))
    registerFactory(MultiSelectListFirstPreference::class.java, LayoutFactory(::MultiSelectListFirstPreferenceViewHolder, R.layout.dsl_preference_first_item))
    registerFactory(ExternalLinkFirstPreference::class.java, LayoutFactory(::ExternalLinkFirstPreferenceViewHolder, R.layout.dsl_preference_first_item))
    registerFactory(ExternalLinkPreference::class.java, LayoutFactory(::ExternalLinkPreferenceViewHolder, R.layout.dsl_preference_item))
    registerFactory(DividerPreference::class.java, LayoutFactory(::DividerPreferenceViewHolder, R.layout.dsl_divider_item))
    registerFactory(SectionHeaderPreference::class.java, LayoutFactory(::SectionHeaderPreferenceViewHolder, R.layout.dsl_section_header))
    registerFactory(SwitchPreference::class.java, LayoutFactory(::SwitchPreferenceViewHolder, R.layout.dsl_switch_preference_item))
    registerFactory(RadioPreference::class.java, LayoutFactory(::RadioPreferenceViewHolder, R.layout.dsl_radio_preference_item))
    registerFactory(RadioLastPreference::class.java, LayoutFactory(::RadioLastPreferenceViewHolder, R.layout.dsl_radio_preference_last_item))
    registerFactory(SwitchLastPreference::class.java, LayoutFactory(::SwitchPreferenceLastViewHolder, R.layout.dsl_switch_preference_last_item))
    registerFactory(SwitchFirstPreference::class.java, LayoutFactory(::SwitchPreferenceFirstViewHolder, R.layout.dsl_switch_preference_first_item))
    registerFactory(SwitchSinglePreference::class.java, LayoutFactory(::SwitchPreferenceSingleViewHolder, R.layout.dsl_switch_preference_single_item))

    Text.register(this)
    Space.register(this)
    Button.register(this)
    AsyncSwitch.register(this)
  }
}

abstract class PreferenceViewHolder<T : PreferenceModel<T>>(itemView: View) : MappingViewHolder<T>(itemView) {
  protected val iconView: ImageView = itemView.findViewById(R.id.icon)
  private val iconEndView: ImageView? = itemView.findViewById(R.id.icon_end)
  protected val titleView: TextView = itemView.findViewById(R.id.title)
  protected val summaryView: TextView = itemView.findViewById(R.id.summary)

  @CallSuper
  override fun bind(model: T) {
    listOf(itemView, titleView, summaryView).forEach {
      it.isEnabled = model.isEnabled
    }

    val icon = model.icon?.resolve(context)
    iconView.setImageDrawable(icon)
    iconView.visible = icon != null

    val iconEnd = model.iconEnd?.resolve(context)
    iconEndView?.setImageDrawable(iconEnd)
    iconEndView?.visible = iconEnd != null

    val title = model.title?.resolve(context)
    if (title != null) {
      titleView.text = model.title?.resolve(context)
      titleView.visibility = View.VISIBLE
    } else {
      titleView.visibility = View.GONE
    }

    val summary = model.summary?.resolve(context)
    if (summary != null) {
      summaryView.text = summary
      summaryView.visibility = View.VISIBLE

      val spans = (summaryView.text as? Spanned)?.getSpans(0, summaryView.text.length, ClickableSpan::class.java)
      if (spans?.isEmpty() == false) {
        summaryView.movementMethod = LinkMovementMethod.getInstance()
      } else {
        summaryView.movementMethod = null
      }
    } else {
      summaryView.visibility = View.GONE
      summaryView.movementMethod = null
    }
  }
}

class TextFirstPreferenceViewHolder(itemView: View) : PreferenceViewHolder<TextFirstPreference>(itemView)
class TextLastPreferenceViewHolder(itemView: View) : PreferenceViewHolder<TextLastPreference>(itemView)

class TextPreferenceViewHolder(itemView: View) : PreferenceViewHolder<TextPreference>(itemView)

class LearnMoreTextPreferenceViewHolder(itemView: View) : PreferenceViewHolder<LearnMoreTextPreference>(itemView) {
  override fun bind(model: LearnMoreTextPreference) {
    super.bind(model)
    (titleView as LearnMoreTextView).setOnLinkClickListener { model.onClick() }
    (summaryView as LearnMoreTextView).setOnLinkClickListener { model.onClick() }
  }
}

class ClickPreferenceViewHolder(itemView: View) : PreferenceViewHolder<ClickPreference>(itemView) {
  override fun bind(model: ClickPreference) {
    super.bind(model)
    itemView.setOnClickListener { model.onClick() }
    itemView.setOnLongClickListener { model.onLongClick?.invoke() ?: false }
  }
}
class ClickSinglePreferenceViewHolder(itemView: View) : PreferenceViewHolder<ClickSinglePreference>(itemView) {
  override fun bind(model: ClickSinglePreference) {
    super.bind(model)
    itemView.setOnClickListener { model.onClick() }
    itemView.setOnLongClickListener { model.onLongClick?.invoke() ?: false }
  }
}
class ClickFirstPreferenceViewHolder(itemView: View) : PreferenceViewHolder<ClickFirstPreference>(itemView) {
  override fun bind(model: ClickFirstPreference) {
    super.bind(model)
    itemView.setOnClickListener { model.onClick() }
    itemView.setOnLongClickListener { model.onLongClick?.invoke() ?: false }
  }
}
class ClickLastPreferenceViewHolder(itemView: View) : PreferenceViewHolder<ClickLastPreference>(itemView) {
  override fun bind(model: ClickLastPreference) {
    super.bind(model)
    itemView.setOnClickListener { model.onClick() }
    itemView.setOnLongClickListener { model.onLongClick?.invoke() ?: false }
  }
}
class ClickStoryPrivacyPreferenceViewHolder(itemView: View) : PreferenceViewHolder<ClickStoryPrivacyPreference>(itemView) {
  override fun bind(model: ClickStoryPrivacyPreference) {
    super.bind(model)
    itemView.setOnClickListener { model.onClick() }
    itemView.setOnLongClickListener { model.onLongClick?.invoke() ?: false }
  }
}
class LongClickPreferenceViewHolder(itemView: View) : PreferenceViewHolder<LongClickPreference>(itemView) {
  override fun bind(model: LongClickPreference) {
    super.bind(model)
    itemView.setOnLongClickListener() {
      model.onLongClick()
      true
    }
  }
}

class RadioListPreferenceViewHolder(itemView: View) : PreferenceViewHolder<RadioListPreference>(itemView) {
  override fun bind(model: RadioListPreference) {
    super.bind(model)

    if (model.selected >= 0) {
      summaryView.visibility = View.VISIBLE
      summaryView.text = model.listItems[model.selected]
    } else {
      summaryView.visibility = View.GONE
      Log.w(TAG, "Detected a radio list without a default selection: ${model.dialogTitle}")
    }

    itemView.setOnClickListener {
      var selection = -1
      val builder = MaterialAlertDialogBuilder(context)
              .setTitle(model.dialogTitle.resolve(context))
              .setSingleChoiceItems(model.listItems, model.selected) { dialog, which ->
                if (model.confirmAction) {
                  selection = which
                } else {
                  model.onSelected(which)
                  dialog.dismiss()
                }
              }

      if (model.confirmAction) {
        builder
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                  model.onSelected(selection)
                  dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                  dialog.dismiss()
                }
                .show()
      } else {
        builder.show()
      }
    }
  }

  companion object {
    private val TAG = Log.tag(RadioListPreference::class.java)
  }
}
class RadioListSinglePreferenceViewHolder(itemView: View) : PreferenceViewHolder<RadioListSinglePreference>(itemView) {
  override fun bind(model: RadioListSinglePreference) {
    super.bind(model)

    if (model.selected >= 0) {
      summaryView.visibility = View.VISIBLE
      summaryView.text = model.listItems[model.selected]
    } else {
      summaryView.visibility = View.GONE
      Log.w(TAG, "Detected a radio list without a default selection: ${model.dialogTitle}")
    }

    itemView.setOnClickListener {
      var selection = -1
      val builder = MaterialAlertDialogBuilder(context)
              .setTitle(model.dialogTitle.resolve(context))
              .setSingleChoiceItems(model.listItems, model.selected) { dialog, which ->
                if (model.confirmAction) {
                  selection = which
                } else {
                  model.onSelected(which)
                  dialog.dismiss()
                }
              }

      if (model.confirmAction) {
        builder
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                  model.onSelected(selection)
                  dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                  dialog.dismiss()
                }
                .show()
      } else {
        builder.show()
      }
    }
  }

  companion object {
    private val TAG = Log.tag(RadioListSinglePreference::class.java)
  }
}
class RadioListLastPreferenceViewHolder(itemView: View) : PreferenceViewHolder<RadioListLastPreference>(itemView) {
  override fun bind(model: RadioListLastPreference) {
    super.bind(model)

    if (model.selected >= 0) {
      summaryView.visibility = View.VISIBLE
      summaryView.text = model.listItems[model.selected]
    } else {
      summaryView.visibility = View.GONE
      Log.w(TAG, "Detected a radio list without a default selection: ${model.dialogTitle}")
    }

    itemView.setOnClickListener {
      var selection = -1
      val builder = MaterialAlertDialogBuilder(context)
              .setTitle(model.dialogTitle.resolve(context))
              .setSingleChoiceItems(model.listItems, model.selected) { dialog, which ->
                if (model.confirmAction) {
                  selection = which
                } else {
                  model.onSelected(which)
                  dialog.dismiss()
                }
              }

      if (model.confirmAction) {
        builder
                .setPositiveButton(android.R.string.ok) { dialog, _ ->
                  model.onSelected(selection)
                  dialog.dismiss()
                }
                .setNegativeButton(android.R.string.cancel) { dialog, _ ->
                  dialog.dismiss()
                }
                .show()
      } else {
        builder.show()
      }
    }
  }

  companion object {
    private val TAG = Log.tag(RadioListLastPreference::class.java)
  }
}
class RadioListFirstPreferenceViewHolder(itemView: View) : PreferenceViewHolder<RadioListFirstPreference>(itemView) {
  override fun bind(model: RadioListFirstPreference) {
    super.bind(model)

    if (model.selected >= 0) {
      summaryView.visibility = View.VISIBLE
      summaryView.text = model.listItems[model.selected]
    } else {
      summaryView.visibility = View.GONE
      Log.w(TAG, "Detected a radio list without a default selection: ${model.dialogTitle}")
    }

    itemView.setOnClickListener {
      var selection = -1
      val builder = MaterialAlertDialogBuilder(context)
        .setTitle(model.dialogTitle.resolve(context))
        .setSingleChoiceItems(model.listItems, model.selected) { dialog, which ->
          if (model.confirmAction) {
            selection = which
          } else {
            model.onSelected(which)
            dialog.dismiss()
          }
        }

      if (model.confirmAction) {
        builder
          .setPositiveButton(android.R.string.ok) { dialog, _ ->
            model.onSelected(selection)
            dialog.dismiss()
          }
          .setNegativeButton(android.R.string.cancel) { dialog, _ ->
            dialog.dismiss()
          }
          .show()
      } else {
        builder.show()
      }
    }
  }

  companion object {
    private val TAG = Log.tag(RadioListFirstPreference::class.java)
  }
}

class MultiSelectListPreferenceViewHolder(itemView: View) : PreferenceViewHolder<MultiSelectListPreference>(itemView) {
  override fun bind(model: MultiSelectListPreference) {
    super.bind(model)

    summaryView.visibility = View.VISIBLE
    val summaryText = model.selected
      .mapIndexed { index, isChecked -> if (isChecked) model.listItems[index] else null }
      .filterNotNull()
      .joinToString(", ")

    if (summaryText.isEmpty()) {
      summaryView.setText(R.string.preferences__none)
    } else {
      summaryView.text = summaryText
    }

    val selected = model.selected.copyOf()

    itemView.setOnClickListener {
      MaterialAlertDialogBuilder(context)
        .setTitle(model.title.resolve(context))
        .setMultiChoiceItems(model.listItems, selected) { _, _, _ ->
          // Intentionally empty
        }
        .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
        .setPositiveButton(android.R.string.ok) { d, _ ->
          model.onSelected(selected)
          d.dismiss()
        }
        .show()
    }
  }
}
class MultiSelectListFirstPreferenceViewHolder(itemView: View) : PreferenceViewHolder<MultiSelectListFirstPreference>(itemView) {
  override fun bind(model: MultiSelectListFirstPreference) {
    super.bind(model)

    summaryView.visibility = View.VISIBLE
    val summaryText = model.selected
            .mapIndexed { index, isChecked -> if (isChecked) model.listItems[index] else null }
            .filterNotNull()
            .joinToString(", ")

    if (summaryText.isEmpty()) {
      summaryView.setText(R.string.preferences__none)
    } else {
      summaryView.text = summaryText
    }

    val selected = model.selected.copyOf()

    itemView.setOnClickListener {
      MaterialAlertDialogBuilder(context)
              .setTitle(model.title.resolve(context))
              .setMultiChoiceItems(model.listItems, selected) { _, _, _ ->
                // Intentionally empty
              }
              .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
              .setPositiveButton(android.R.string.ok) { d, _ ->
                model.onSelected(selected)
                d.dismiss()
              }
              .show()
    }
  }
}class MultiSelectListLastPreferenceViewHolder(itemView: View) : PreferenceViewHolder<MultiSelectListLastPreference>(itemView) {
  override fun bind(model: MultiSelectListLastPreference) {
    super.bind(model)

    summaryView.visibility = View.VISIBLE
    val summaryText = model.selected
            .mapIndexed { index, isChecked -> if (isChecked) model.listItems[index] else null }
            .filterNotNull()
            .joinToString(", ")

    if (summaryText.isEmpty()) {
      summaryView.setText(R.string.preferences__none)
    } else {
      summaryView.text = summaryText
    }

    val selected = model.selected.copyOf()

    itemView.setOnClickListener {
      MaterialAlertDialogBuilder(context)
              .setTitle(model.title.resolve(context))
              .setMultiChoiceItems(model.listItems, selected) { _, _, _ ->
                // Intentionally empty
              }
              .setNegativeButton(android.R.string.cancel) { d, _ -> d.dismiss() }
              .setPositiveButton(android.R.string.ok) { d, _ ->
                model.onSelected(selected)
                d.dismiss()
              }
              .show()
    }
  }
}

class SwitchPreferenceViewHolder(itemView: View) : PreferenceViewHolder<SwitchPreference>(itemView) {

  private val switchWidget: SwitchMaterial = itemView.findViewById(R.id.switch_widget)

  override fun bind(model: SwitchPreference) {
    super.bind(model)
    switchWidget.isEnabled = model.isEnabled
    switchWidget.isChecked = model.isChecked
    itemView.setOnClickListener {
      model.onClick()
    }
  }
}
class SwitchPreferenceSingleViewHolder(itemView: View) : PreferenceViewHolder<SwitchSinglePreference>(itemView) {

  private val switchWidget: SwitchMaterial = itemView.findViewById(R.id.switch_widget)

  override fun bind(model: SwitchSinglePreference) {
    super.bind(model)
    switchWidget.isEnabled = model.isEnabled
    switchWidget.isChecked = model.isChecked
    itemView.setOnClickListener {
      model.onClick()
    }
  }
}
class SwitchPreferenceFirstViewHolder(itemView: View) : PreferenceViewHolder<SwitchFirstPreference>(itemView) {

  private val switchWidget: SwitchMaterial = itemView.findViewById(R.id.switch_widget)

  override fun bind(model: SwitchFirstPreference) {
    super.bind(model)
    switchWidget.isEnabled = model.isEnabled
    switchWidget.isChecked = model.isChecked
    itemView.setOnClickListener {
      model.onClick()
    }
  }
}
class SwitchPreferenceLastViewHolder(itemView: View) : PreferenceViewHolder<SwitchLastPreference>(itemView) {

  private val switchWidget: SwitchMaterial = itemView.findViewById(R.id.switch_widget)

  override fun bind(model: SwitchLastPreference) {
    super.bind(model)
    switchWidget.isEnabled = model.isEnabled
    switchWidget.isChecked = model.isChecked
    itemView.setOnClickListener {
      model.onClick()
    }
  }
}

class RadioPreferenceViewHolder(itemView: View) : PreferenceViewHolder<RadioPreference>(itemView) {

  private val radioButton: RadioButton = itemView.findViewById(R.id.radio_widget)

  override fun bind(model: RadioPreference) {
    super.bind(model)
    radioButton.isChecked = model.isChecked
    itemView.setOnClickListener {
      model.onClick()
    }
  }
}

class RadioLastPreferenceViewHolder(itemView: View) : PreferenceViewHolder<RadioLastPreference>(itemView) {

  private val radioButton: RadioButton = itemView.findViewById(R.id.radio_widget)

  override fun bind(model: RadioLastPreference) {
    super.bind(model)
    radioButton.isChecked = model.isChecked
    itemView.setOnClickListener {
      model.onClick()
    }
  }
}

class ExternalLinkPreferenceViewHolder(itemView: View) : PreferenceViewHolder<ExternalLinkPreference>(itemView) {
  override fun bind(model: ExternalLinkPreference) {
    super.bind(model)

    val externalLinkIcon = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_fluent_open_20_regular))
    externalLinkIcon.setBounds(0, 0, ViewUtil.dpToPx(20), ViewUtil.dpToPx(20))

    if (ViewUtil.isLtr(itemView)) {
      titleView.setCompoundDrawables(null, null, externalLinkIcon, null)
    } else {
      titleView.setCompoundDrawables(externalLinkIcon, null, null, null)
    }

    itemView.setOnClickListener { CommunicationActions.openBrowserLink(itemView.context, itemView.context.getString(model.linkId)) }
  }
}

class ExternalLinkFirstPreferenceViewHolder(itemView: View) : PreferenceViewHolder<ExternalLinkFirstPreference>(itemView) {
  override fun bind(model: ExternalLinkFirstPreference) {
    super.bind(model)

    val externalLinkIcon = requireNotNull(ContextCompat.getDrawable(context, R.drawable.ic_fluent_open_20_regular))
    externalLinkIcon.setBounds(0, 0, ViewUtil.dpToPx(20), ViewUtil.dpToPx(20))

    if (ViewUtil.isLtr(itemView)) {
      titleView.setCompoundDrawables(null, null, externalLinkIcon, null)
    } else {
      titleView.setCompoundDrawables(externalLinkIcon, null, null, null)
    }

    itemView.setOnClickListener { CommunicationActions.openBrowserLink(itemView.context, itemView.context.getString(model.linkId)) }
  }
}


class DividerPreferenceViewHolder(itemView: View) : MappingViewHolder<DividerPreference>(itemView) {
  override fun bind(model: DividerPreference) = Unit
}

class SectionHeaderPreferenceViewHolder(itemView: View) : MappingViewHolder<SectionHeaderPreference>(itemView) {

  private val sectionHeader: TextView = itemView.findViewById(R.id.section_header)

  override fun bind(model: SectionHeaderPreference) {
    sectionHeader.text = model.title.resolve(context)
  }
}
