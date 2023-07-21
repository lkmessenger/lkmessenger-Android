package org.thoughtcrime.securesms.stories.landing

import android.view.View
import android.widget.ImageView
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.components.settings.PreferenceModel
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.adapter.mapping.LayoutFactory
import org.thoughtcrime.securesms.util.adapter.mapping.MappingAdapter
import org.thoughtcrime.securesms.util.adapter.mapping.MappingViewHolder

/**
 * Item displayed on an empty Stories landing page allowing the user to add a new story.
 */
object PostsMyStoriesItem {

  fun register(mappingAdapter: MappingAdapter) {
    mappingAdapter.registerFactory(Model::class.java, LayoutFactory(::ViewHolder, R.layout.posts_item_my_stories))
  }

  class Model(
          val onClick: () -> Unit,
          val onAddStoryClick: () -> Unit
  ) : PreferenceModel<Model>() {
    override fun areItemsTheSame(newItem: Model): Boolean = true
  }

  private class ViewHolder(itemView: View) : MappingViewHolder<Model>(itemView) {

    private val avatarView: AvatarView = itemView.findViewById(R.id.avatar)
    private val addPost: ImageView = itemView.findViewById(R.id.add_to_story)

    override fun bind(model: Model) {
      itemView.setOnClickListener { model.onClick() }
      avatarView.displayProfileAvatar(Recipient.self())
      addPost.setOnClickListener{model.onAddStoryClick()}
    }
  }
}
