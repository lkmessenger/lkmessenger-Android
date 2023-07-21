package org.linkmessenger.fire.views

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.fire.adapters.FireItemPagerAdapter
import org.linkmessenger.fire.viewmodel.FireActivityViewModel
import org.thoughtcrime.securesms.databinding.ActivityFireBinding

class FireActivity : AppCompatActivity(), KoinComponent {
    companion object{
        val TAG: String = FireActivity::class.java.toString()
        fun newIntent(context: Context): Intent {
            return Intent(context, FireActivity::class.java)
        }
    }
    private lateinit var binding: ActivityFireBinding
    private lateinit var adapter:FireItemPagerAdapter
    private val viewModel:FireActivityViewModel = get()

    private fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win: Window = activity.window
        val winParams: WindowManager.LayoutParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFireBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
        window.statusBarColor = Color.TRANSPARENT

        adapter = FireItemPagerAdapter(fa = this)
        binding.firePager.adapter = adapter

        viewModel.posts.observe(this){
            adapter.posts.addAll(it)
            binding.firePager.adapter = adapter
        }
    }
}