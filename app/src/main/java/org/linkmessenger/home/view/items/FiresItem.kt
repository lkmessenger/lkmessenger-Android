package org.linkmessenger.home.view.items

import android.view.View
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.components.settings.PreferenceModel
import org.thoughtcrime.securesms.database.model.StoryViewState
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.adapter.mapping.LayoutFactory
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.adapter.mapping.MappingViewHolder


object FiresItem {

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(FiresItem::ViewHolder, R.layout.layout_fire_item))
  }

  class Model(
          val onClick: () -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean = true
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {
    private val avatarView: AvatarView = itemView.findViewById(R.id.avatar)
    override fun bind(model: Model) {
      itemView.setOnClickListener { model.onClick() }
      avatarView.displayProfileAvatar(Recipient.self())
      avatarView.setStoryRingFromState(StoryViewState.UNVIEWED)
    }
  }
}
