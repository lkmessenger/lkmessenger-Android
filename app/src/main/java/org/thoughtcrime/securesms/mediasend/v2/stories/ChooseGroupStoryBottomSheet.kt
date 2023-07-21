package org.thoughtcrime.securesms.mediasend.v2.stories

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.signal.core.util.DimensionUnit
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.components.FixedRoundedCornerBottomSheetDialogFragment
import org.thoughtcrime.securesms.contacts.paged.ContactSearchConfiguration
import org.thoughtcrime.securesms.contacts.paged.ContactSearchKey
import org.thoughtcrime.securesms.contacts.paged.ContactSearchMediator
import org.thoughtcrime.securesms.contacts.paged.ContactSearchSortOrder
import org.thoughtcrime.securesms.recipients.RecipientId
import org.thoughtcrime.securesms.sharing.ShareContact
import org.thoughtcrime.securesms.sharing.ShareSelectionAdapter
import org.thoughtcrime.securesms.sharing.ShareSelectionMappingModel
import org.thoughtcrime.securesms.util.FeatureFlags

class ChooseGroupStoryBottomSheet : FixedRoundedCornerBottomSheetDialogFragment() {

  companion object {
    const val GROUP_STORY = "group-story"
    const val RESULT_SET = "groups"
  }

  private lateinit var confirmButton: View
  private lateinit var selectedList: RecyclerView
  private lateinit var backgroundHelper: View
  private lateinit var divider: View
  private lateinit var mediator: ContactSearchMediator

  private var animatorSet: AnimatorSet? = null

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.cloneInContext(ContextThemeWrapper(inflater.context, themeResId)).inflate(R.layout.stories_choose_group_bottom_sheet, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    view.minimumHeight = resources.displayMetrics.heightPixels

    val container = view.parent.parent.parent as FrameLayout
    val bottomBar = LayoutInflater.from(requireContext()).inflate(R.layout.stories_choose_group_bottom_bar, container, true)

    confirmButton = bottomBar.findViewById(R.id.share_confirm)
    selectedList = bottomBar.findViewById(R.id.selected_list)
    selectedList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    backgroundHelper = bottomBar.findViewById(R.id.background_helper)
    divider = bottomBar.findViewById(R.id.divider)

    val adapter = ShareSelectionAdapter()
    selectedList.adapter = adapter

    confirmButton.setOnClickListener {
      onDone()
    }

    val contactRecycler: RecyclerView = view.findViewById(R.id.contact_recycler)
    contactRecycler.layoutManager =
      LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    mediator = ContactSearchMediator(
      fragment = this,
      recyclerView = contactRecycler,
      selectionLimits = FeatureFlags.shareSelectionLimit(),
      displayCheckBox = true,
      mapStateToConfiguration = { state ->
        ContactSearchConfiguration.build {
          query = state.query

          addSection(
            ContactSearchConfiguration.Section.Groups(
              includeHeader = false,
              returnAsGroupStories = true,
              sortOrder = ContactSearchSortOrder.RECENCY
            )
          )
        }
      },
      performSafetyNumberChecks = false
    )

    mediator.getSelectionState().observe(viewLifecycleOwner) { state ->
      adapter.submitList(
        state.filterIsInstance(ContactSearchKey.RecipientSearchKey.Story::class.java)
          .map { it.recipientId }
          .mapIndexed { index, recipientId ->
            ShareSelectionMappingModel(
              ShareContact(recipientId),
              index == 0
            )
          }
      )

      if (state.isEmpty()) {
        animateOutBottomBar()
      } else {
        animateInBottomBar()
      }
    }

    val searchField: EditText = view.findViewById(R.id.search_field)
    searchField.doAfterTextChanged {
      mediator.onFilterChanged(it?.toString())
    }
  }

  override fun onDestroyView() {
    super.onDestroyView()
    animatorSet?.cancel()
  }

  private fun animateInBottomBar() {
    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(confirmButton, View.ALPHA, 1f),
        ObjectAnimator.ofFloat(selectedList, View.TRANSLATION_Y, 0f),
        ObjectAnimator.ofFloat(backgroundHelper, View.TRANSLATION_Y, 0f),
        ObjectAnimator.ofFloat(divider, View.TRANSLATION_Y, 0f)
      )
      start()
    }
  }

  private fun animateOutBottomBar() {
    val translationY = DimensionUnit.DP.toPixels(48f)

    animatorSet?.cancel()
    animatorSet = AnimatorSet().apply {
      playTogether(
        ObjectAnimator.ofFloat(confirmButton, View.ALPHA, 0f),
        ObjectAnimator.ofFloat(selectedList, View.TRANSLATION_Y, translationY),
        ObjectAnimator.ofFloat(backgroundHelper, View.TRANSLATION_Y, translationY),
        ObjectAnimator.ofFloat(divider, View.TRANSLATION_Y, translationY)
      )
      start()
    }
  }

  private fun onDone() {
    setFragmentResult(
      GROUP_STORY,
      Bundle().apply {
        putParcelableArrayList(
          RESULT_SET,
          ArrayList(
            mediator.getSelectedContacts()
              .filterIsInstance(ContactSearchKey.RecipientSearchKey.Story::class.java)
              .map { it.recipientId }
          )
        )
      }
    )
    dismissAllowingStateLoss()
  }

  object ResultContract {
    fun getRecipientIds(bundle: Bundle): List<RecipientId> {
      return bundle.getParcelableArrayList(RESULT_SET)!!
    }
  }
}
