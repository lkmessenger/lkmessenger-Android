package org.linkmessenger.reports.views.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.profile.view.activities.PostsViewerActivity
import org.linkmessenger.reports.adapters.ReportsAdapter
import org.linkmessenger.reports.models.Report
import org.linkmessenger.reports.viewmodels.ReportsViewModel
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.ActivityCollectionsBinding
import org.thoughtcrime.securesms.databinding.ActivityReportsBinding
import org.thoughtcrime.securesms.util.visible
import java.lang.reflect.Type

class ReportsActivity : AppCompatActivity(), KoinComponent {
    companion object{
        fun newIntent(context: Context, postId:Long): Intent {
            return Intent(context, ReportsActivity::class.java).apply {
                putExtra("post_id", postId)
            }
        }
    }
    private var postId:Long = 0L
    private var selectedReport: Report? = null
    private val viewModel: ReportsViewModel = get()
    var adapter: ReportsAdapter = ReportsAdapter()
    lateinit var binding: ActivityReportsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReportsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getLongExtra("post_id", 0)

        supportActionBar?.hide()
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
        binding.sendReport.isEnabled = false
        binding.items.layoutManager = LinearLayoutManager(this)
        binding.items.adapter = adapter

        viewModel.loadReportTypes()

        viewModel.loading.observe(this){
            binding.progress.visible = it
            binding.sendReport.isEnabled = !it
        }

        viewModel.reportTypes.observe(this){
            adapter.items.addAll(it)
            adapter.notifyItemRangeInserted(0, it.size)
        }

        binding.items.setOnItemClickListener { view, position, x, y ->
            adapter.setSelectedPosition(position)
            selectedReport = adapter.items[position]
            binding.sendReport.isEnabled = true
        }

        binding.sendReport.setOnClickListener {
            if(selectedReport != null){
                viewModel.reportPost(postId, selectedReport!!.id)
            }
        }

        viewModel.isReported.observe(this){
            if(it){
                showSuccessfullySentDialog()
            }
        }
    }

    private fun showSuccessfullySentDialog() {
        val dialog = MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_successfully_sent_title)
                .setMessage(R.string.dialog_successfully_sent_message)
                .setPositiveButton(R.string.ChangeNumber__okay) { dialog, which ->
                   onBackPressed()
                }
                .show()

        dialog.findViewById<TextView>(android.R.id.message)?.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.reportTypes.value?.clear()
        viewModel.isReported.value = false
    }
}