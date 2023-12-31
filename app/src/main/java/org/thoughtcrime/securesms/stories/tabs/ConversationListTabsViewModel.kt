package org.thoughtcrime.securesms.stories.tabs

import android.app.Activity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import org.thoughtcrime.securesms.stories.Stories
import org.thoughtcrime.securesms.util.livedata.Store

class ConversationListTabsViewModel(repository: ConversationListTabRepository) : ViewModel() {
  private val store = Store(ConversationListTabsState())

  var onCallToTop:(()->Unit)?=null

  val stateSnapshot: ConversationListTabsState = store.state
  val state: LiveData<ConversationListTabsState> = store.stateLiveData
  val disposables = CompositeDisposable()

  private val internalTabClickEvents: Subject<ConversationListTab> = PublishSubject.create()
  val tabClickEvents: Observable<ConversationListTab> = internalTabClickEvents.filter { Stories.isFeatureEnabled() }

  init {
    disposables += repository.getNumberOfUnreadConversations().subscribe { unreadChats ->
      store.update { it.copy(unreadChatsCount = unreadChats) }
    }

    disposables += repository.getNumberOfUnseenStories().subscribe { unseenStories ->
      store.update { it.copy(unreadStoriesCount = unseenStories) }
    }
  }

  override fun onCleared() {
    disposables.clear()
  }

  fun onPostsSelected(){
    if(store.state.tab != ConversationListTab.POSTS){
      internalTabClickEvents.onNext(ConversationListTab.POSTS)
      store.update { it.copy(tab = ConversationListTab.POSTS) }
    }else{
      onCallToTop?.invoke()
    }
  }

  fun onContactsSelected(){
    if(store.state.tab != ConversationListTab.CONTACTS){
      internalTabClickEvents.onNext(ConversationListTab.CONTACTS)
      store.update { it.copy(tab = ConversationListTab.CONTACTS) }
    }else{
      onCallToTop?.invoke()
    }
  }

  fun onChatsSelected() {
    if(store.state.tab != ConversationListTab.CHATS) {
      internalTabClickEvents.onNext(ConversationListTab.CHATS)
      store.update { it.copy(tab = ConversationListTab.CHATS) }
    }
  }

  fun onProfileSelected() {
    if(store.state.tab != ConversationListTab.PROFILE){
      internalTabClickEvents.onNext(ConversationListTab.PROFILE)
      store.update { it.copy(tab = ConversationListTab.PROFILE) }
    }else{
      onCallToTop?.invoke()
    }
  }

  fun onSearchOpened() {
    store.update { it.copy(visibilityState = it.visibilityState.copy(isSearchOpen = true)) }
  }

  fun onSearchClosed() {
    store.update { it.copy(visibilityState = it.visibilityState.copy(isSearchOpen = false)) }
  }

  fun onMultiSelectStarted() {
    store.update { it.copy(visibilityState = it.visibilityState.copy(isMultiSelectOpen = true)) }
  }

  fun onMultiSelectFinished() {
    store.update { it.copy(visibilityState = it.visibilityState.copy(isMultiSelectOpen = false)) }
  }

  fun isShowingArchived(isShowingArchived: Boolean) {
    store.update { it.copy(visibilityState = it.visibilityState.copy(isShowingArchived = isShowingArchived)) }
  }

  fun isShowingAllStories(isShowingAllStories: Boolean) {
    store.update { it.copy(visibilityState = it.visibilityState.copy(isShowingAllStories = isShowingAllStories)) }
  }

  class Factory(private val repository: ConversationListTabRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      return modelClass.cast(ConversationListTabsViewModel(repository)) as T
    }
  }
}
