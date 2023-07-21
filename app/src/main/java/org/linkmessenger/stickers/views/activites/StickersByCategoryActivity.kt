package org.linkmessenger.stickers.views.activites

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.stickers.adapters.StickerItemAdapter
import org.linkmessenger.stickers.adapters.StickerItemType
import org.linkmessenger.stickers.models.StickersWithCategory
import org.linkmessenger.stickers.viewmodels.StickersByCategoryViewModel
import org.thoughtcrime.securesms.databinding.ActivityStickersByCategoryBinding
import org.thoughtcrime.securesms.stickers.StickerPackPreviewActivity
import org.thoughtcrime.securesms.util.visible

class StickersByCategoryActivity : AppCompatActivity(), KoinComponent {
    private lateinit var binding: ActivityStickersByCategoryBinding
    private lateinit var layoutManager: LinearLayoutManager
    private var adapter: StickerItemAdapter = StickerItemAdapter(type = StickerItemType.StickersVertical)
    val viewModel: StickersByCategoryViewModel = get()

    var categoryName = ""
    var categoryId = 0

    var isLoading = false

    companion object{
        fun newIntent(context: Context, category: StickersWithCategory): Intent {
            return Intent(context, StickersByCategoryActivity::class.java).apply {
                putExtra("category_name", category.title)
                putExtra("category_id", category.categoryId)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStickersByCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener{ onBackPressed() }

        categoryId = intent.getIntExtra("category_id", 0)
        categoryName = intent.getStringExtra("category_name") ?: ""

        binding.title.text = categoryName

        viewModel.getStickersByCategory(categoryId)

        layoutManager = LinearLayoutManager(this)
        binding.recycler.layoutManager = layoutManager
        binding.recycler.adapter = adapter

        viewModel.stickers.observe(this){
            if(it!=null){
                adapter.items.addAll(it)
                adapter.notifyItemRangeInserted(0, it.size);
            }
            isLoading = false
        }

        viewModel.loading.observe(this){
            binding.loading.visible = it
        }

        binding.recycler.setOnItemClickListener { _, position, _, _ ->
            startActivity(StickerPackPreviewActivity.getIntent(adapter.items[position].packId, adapter.items[position].packKey))
        }

        binding.recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItem: Int = layoutManager.itemCount
                    val lastVisibleItem: Int = layoutManager.findLastVisibleItemPosition()
                    if (!isLoading && lastVisibleItem == totalItem - 1) {
                        isLoading = true
                        viewModel.getStickersByCategory(categoryId)
                    }
                }
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stickers.value?.clear()
        adapter.items.clear()
    }
}