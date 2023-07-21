package org.linkmessenger.posts.view.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.openProfile
import org.linkmessenger.posts.viewmodel.PostLikesViewModel
import org.linkmessenger.profile.adapters.UsersAdapter
import org.linkmessenger.profile.adapters.UsersListType
import org.linkmessenger.showError
import org.thoughtcrime.securesms.databinding.ActivityPostLikesBinding

class PostLikesActivity : AppCompatActivity(), KoinComponent {
    private lateinit var binding: ActivityPostLikesBinding
    var postId: Long = 0
    private var usersAdapter: UsersAdapter? = null
    val viewModel: PostLikesViewModel = get()
    var isLoading = false
    val layoutManager = LinearLayoutManager(this)

    companion object{
        fun newIntent(context: Context, postId: Long): Intent {
            return Intent(context, PostLikesActivity::class.java).apply {
                putExtra("post_id", postId)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPostLikesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.toolbar.setNavigationOnClickListener{ onBackPressed() }


        postId = intent.getLongExtra("post_id", 0)

        binding.users.layoutManager = layoutManager

        usersAdapter = UsersAdapter(type = UsersListType.PostLikes)
        binding.users.adapter = usersAdapter


        if(postId != 0L){
            viewModel.loadUsers(postId)
        }

        viewModel.loading.observe(this){
            binding.loading.isVisible = it
        }
        viewModel.error.observe(this){
            it?.let { it1 ->
                binding.root.showError(it1)
                viewModel.error.postValue(null)
            }
        }
        binding.users.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (dy > 0) {
                    val totalItem: Int = layoutManager.itemCount
                    val lastVisibleItem: Int = layoutManager.findLastVisibleItemPosition()
                    if (!isLoading && lastVisibleItem == totalItem - 1) {
                        isLoading = true
                        viewModel.loadUsers(postId)
                    }
                }
            }
        })


        viewModel.users.observe(this){
            isLoading = false
            if(usersAdapter!=null){
                val tmpSize = usersAdapter!!.itemCount
                usersAdapter!!.items.addAll(it)
                usersAdapter!!.notifyItemRangeInserted(tmpSize, it.size)
            }
        }

        binding.users.setOnItemClickListener { _, position, _, _ ->
            val user = usersAdapter?.items?.get(position)
            if(user!=null){
                this.openProfile(user.id)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.users.value?.clear()
        viewModel.page = 0
    }
}