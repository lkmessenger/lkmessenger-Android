package org.thoughtcrime.securesms.stories.tabs

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import org.linkmessenger.posts.view.bottomsheet.AddTabSelectorBottomSheet
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.avatar.view.AvatarView
import org.thoughtcrime.securesms.components.settings.app.appearance.AppearanceSettingsViewModel
import org.thoughtcrime.securesms.database.model.StoryViewState
import org.thoughtcrime.securesms.recipients.Recipient
import org.thoughtcrime.securesms.util.visible
import java.text.NumberFormat


/**
 * Displays the "Chats" and "Stories" tab to a user.
 */
class ConversationListTabsFragment : Fragment(R.layout.conversation_list_tabs) {

  private val viewModel: ConversationListTabsViewModel by viewModels(ownerProducer = { requireActivity() })
  private lateinit var appearanveViewModel: AppearanceSettingsViewModel

  private lateinit var chatsUnreadIndicator: TextView
//  private lateinit var storiesUnreadIndicator: TextView

  private lateinit var postsIcon:ImageView
  private lateinit var contactsIcon:ImageView
  private lateinit var chatsIcon:ImageView
  private lateinit var profileIcon:AvatarView
  private lateinit var addPostIcon:ImageView

  private var itemClickListener: OnBottomNavClickListener? = null
  private var contactsClickCount: Int = 0
  private var isBottomSheetOpen = false

  override fun onDetach() {
    super.onDetach()
    itemClickListener = null;
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    chatsUnreadIndicator = view.findViewById(R.id.chats_unread_indicator)
//    storiesUnreadIndicator = view.findViewById(R.id.stories_unread_indicator)

    chatsIcon = view.findViewById(R.id.chats_tab_icon)
    contactsIcon = view.findViewById(R.id.contacts_tab_icon)
    profileIcon = view.findViewById(R.id.profile_tab_icon)
    postsIcon = view.findViewById(R.id.posts_tab_icon)
    addPostIcon = view.findViewById(R.id.add_post_tab_icon)

    try {
      profileIcon.displayProfileAvatar(Recipient.self())
    }catch (_:java.lang.IllegalStateException){

    }

    appearanveViewModel = ViewModelProvider(this)[AppearanceSettingsViewModel::class.java]

    view.findViewById<View>(R.id.posts_tab_touch_point).setOnClickListener {
      viewModel.onPostsSelected()
    }

    view.findViewById<View>(R.id.chats_tab_touch_point).setOnClickListener {
      viewModel.onChatsSelected()
    }

    view.findViewById<View>(R.id.profile_tab_touch_point).setOnClickListener {
      viewModel.onProfileSelected()
    }

    view.findViewById<View>(R.id.contacts_tab_touch_point).setOnClickListener {
      viewModel.onContactsSelected()
      contactsClickCount++
      if(contactsClickCount == 2){
        contactsClickCount = 0
        itemClickListener?.onContactsClick()
      }
    }

    view.findViewById<View>(R.id.add_post_tab_touch_point).setOnClickListener{
      AddTabSelectorBottomSheet.create().show(parentFragmentManager, "BOTTOM")
    }

    update(viewModel.stateSnapshot, true)

    viewModel.state.observe(viewLifecycleOwner) { update(it, false) }
  }

  private fun update(state: ConversationListTabsState, immediate: Boolean) {

    chatsUnreadIndicator.visible = state.unreadChatsCount > 0
    chatsUnreadIndicator.text = formatCount(state.unreadChatsCount)

//    storiesUnreadIndicator.visible = state.unreadStoriesCount > 0
//    storiesUnreadIndicator.text = formatCount(state.unreadStoriesCount)

    if(isDarkMode()){
      postsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_dark_theme))
      chatsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_dark_theme))
      contactsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_dark_theme))
      addPostIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_dark_theme))
    }else{
      postsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_light_theme))
      chatsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_light_theme))
      contactsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_light_theme))
      addPostIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.text_color_light_theme))
    }

    if(state.tab == ConversationListTab.POSTS){
      postsIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_home_24_filled))
      postsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.signal_colorPrimary))
    }else{
      postsIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_home_24_regular))
    }

    if(state.tab == ConversationListTab.CHATS){
      chatsIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_chat_empty_24_filled))
      chatsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.signal_colorPrimary))
    }else{
      chatsIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_chat_empty_24_regular))
    }

    if(state.tab == ConversationListTab.PROFILE){
      profileIcon.setStoryRingFromState(StoryViewState.PROFILE_SELECTED)
    }else{
      profileIcon.setStoryRingFromState(StoryViewState.PROFILE_UNSELECTED)
    }

    if(state.tab == ConversationListTab.CONTACTS){
      contactsIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_people_24_filled))
      contactsIcon.setColorFilter(ContextCompat.getColor(requireContext(),R.color.signal_colorPrimary))
    }else{
      contactsIcon.setImageDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_fluent_people_24_regular))
    }

    requireView().visible = state.visibilityState.isVisible()
  }

  private fun isDarkMode(): Boolean {
    return appearanveViewModel.getTheme(activity).toString() == "DARK"
  }

  private fun formatCount(count: Long): String {
    if (count > 99L) {
      return getString(R.string.ConversationListTabs__99p)
    }
    return NumberFormat.getInstance().format(count)
  }

  fun setItemClickListener(listener: OnBottomNavClickListener) {
    itemClickListener = listener
  }

  interface OnBottomNavClickListener {
    fun onContactsClick()
  }
}
