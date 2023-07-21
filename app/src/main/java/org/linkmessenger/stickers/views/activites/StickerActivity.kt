package org.linkmessenger.stickers.views.activites

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.stickers.adapters.StickerWithCategoryAdapter
import org.linkmessenger.stickers.viewmodels.StickersViewModel
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityAddStickerBinding
import org.thoughtcrime.securesms.util.visible

class StickerActivity : AppCompatActivity(), KoinComponent {
    private lateinit var binding: ActivityAddStickerBinding
    private var adapter: StickerWithCategoryAdapter = StickerWithCategoryAdapter()
    val viewModel:StickersViewModel = get()

    companion object{
        fun newIntent(context: Context): Intent {
            return Intent(context, StickerActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener{ onBackPressed() }

        viewModel.getStickersWithCategory()

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        viewModel.stickersWithCategory.observe(this){
            if(it!=null){
                adapter.items.addAll(it)
                adapter.notifyItemRangeInserted(0, it.size);
            }
        }

        viewModel.loading.observe(this){
            binding.loading.visible = it
        }

        binding.recycler.setOnItemClickListener { view, position, x, y ->
            val more: TextView = view.findViewById(R.id.see_all)

            if(more.left<x && more.measuredWidth + more.left>x &&
                more.top<y && more.measuredHeight + more.top>y){
                startActivity(StickersByCategoryActivity.newIntent(this, category = adapter.items[position]))
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stickersWithCategory.value?.clear()
        adapter.items.clear()
    }
}