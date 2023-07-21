package org.linkmessenger.collections.views.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.base.ui.components.post.PostViewType
import org.linkmessenger.collections.viewmodels.CollectionsViewModel
import org.linkmessenger.openPostsView
import org.linkmessenger.profile.adapters.PostsAdapter
import org.linkmessenger.profile.view.activities.PostTypes
import org.linkmessenger.showError
import org.linkmessenger.utils.view.ItemOffsetDecoration
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityCollectionsBinding
import org.thoughtcrime.securesms.util.DynamicNoActionBarTheme
import org.thoughtcrime.securesms.util.DynamicTheme
import org.thoughtcrime.securesms.util.visible

class CollectionsActivity : AppCompatActivity(), KoinComponent {
    companion object{
        fun newIntent(context: Context): Intent {
            return Intent(context, CollectionsActivity::class.java)
        }
    }

    private lateinit var binding: ActivityCollectionsBinding
    private val collectionsViewModel: CollectionsViewModel = get()
    var postsAdapter: PostsAdapter?=null
    var isPostsLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCollectionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        this.changeStatusBarColor()
        supportActionBar?.hide()
        collectionsViewModel.loadCollections()

        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        collectionsViewModel.loading.observe(this){
            binding.loading.isVisible = it
        }

        val layoutManager = GridLayoutManager(this, 3)
        binding.items.layoutManager = layoutManager
        val itemDecoration = ItemOffsetDecoration(this, R.dimen.item_offset)
        binding.items.addItemDecoration(itemDecoration)
        postsAdapter = PostsAdapter(this, PostViewType.Grid)
        binding.items.adapter = postsAdapter

        binding.items.setOnItemClickListener { _, position, _, _ ->
            val postItem = postsAdapter?.items?.get(position)
            postItem?.let { this.openPostsView(it.id, null, PostTypes.Collections::class.java, null) }
        }

        collectionsViewModel.emptyState.observe(this){
            binding.emptyState.visible = it
            binding.items.visible = !it
        }

        collectionsViewModel.posts.observe(this){
            if(postsAdapter!=null){
                val tmpSize = postsAdapter!!.items.size
                postsAdapter!!.items.addAll(it)
                postsAdapter!!.notifyItemRangeInserted(tmpSize, it.size)
            }
            isPostsLoading = false
        }

        collectionsViewModel.error.observe(this){
            if(it!=null){
                binding.root.showError(it)
                collectionsViewModel.error.postValue(null)
            }
        }

        binding.items.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItem: Int = layoutManager.itemCount
                    val lastVisibleItem: Int = layoutManager.findLastVisibleItemPosition()
                    if (!isPostsLoading && lastVisibleItem == totalItem - 1) {
                        isPostsLoading = true
                        collectionsViewModel.loadCollections()
                    }
                }
            }
        })

    }

    fun loadMore(){
        collectionsViewModel.loadCollections()
    }

    override fun onDestroy() {
        super.onDestroy()
        collectionsViewModel.posts.value?.clear()
        collectionsViewModel.page = 1
    }
}