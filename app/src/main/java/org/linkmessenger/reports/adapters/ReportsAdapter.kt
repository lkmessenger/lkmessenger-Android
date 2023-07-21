package org.linkmessenger.reports.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.linkmessenger.profile.models.ProfileData
import org.linkmessenger.reports.models.Report
import org.thoughtcrime.securesms.R

class ReportsAdapter : RecyclerView.Adapter<ReportsAdapter.TextViewHolder>() {
    private var selectedPosition = RecyclerView.NO_POSITION
    val items: ArrayList<Report> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.report_item, parent, false)
        return TextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.LTGRAY)
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setSelectedPosition(position: Int) {
        val prevSelectedPosition = selectedPosition
        selectedPosition = position
        notifyItemChanged(prevSelectedPosition)
        notifyItemChanged(selectedPosition)
    }


    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val reportText: TextView = itemView.findViewById(R.id.report_text)

        fun bind(report: Report) {
            reportText.text = report.title
        }

    }

}
