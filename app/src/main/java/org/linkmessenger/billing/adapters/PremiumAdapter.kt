package org.linkmessenger.billing.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.ProductDetails
import org.thoughtcrime.securesms.R
import org.thoughtcrime.securesms.databinding.LayoutPremiumItemBinding

class PremiumAdapter: RecyclerView.Adapter<PremiumAdapter.PremiumItemViewHolder>() {
    private val items:MutableList<ProductDetails> = mutableListOf()
    private var selectedPosition = 0
    @SuppressLint("NotifyDataSetChanged")
    fun update(subList: MutableList<ProductDetails>){
        items.clear()
        items.addAll(subList)
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun setSelection(index:Int){
        selectedPosition = index
        notifyDataSetChanged()
//        notifyItemRangeChanged(0, itemCount)
    }
    override fun getItemCount(): Int {
        return items.size
    }
    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
    override fun getItemViewType(position: Int): Int {
        return position
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PremiumItemViewHolder {
        val binding = LayoutPremiumItemBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return PremiumItemViewHolder(binding)
    }
    override fun onBindViewHolder(holder: PremiumItemViewHolder, position: Int) {
        holder.bind(items[position], position)
    }
    inner class PremiumItemViewHolder(val binding: LayoutPremiumItemBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(productDetails: ProductDetails, position: Int){
            binding.name.text = productDetails.name
            binding.desc.text = productDetails.description
            binding.price.text = productDetails.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.formattedPrice

            if (position==selectedPosition){
                binding.root.strokeWidth = 5
                binding.price.setTextColor(ContextCompat.getColor(binding.root.context, R.color.signal_colorPrimary))
            }else{
                binding.root.strokeWidth = 0
                binding.price.setTextColor(ContextCompat.getColor(binding.root.context, R.color.signal_colorOnSecondaryContainer))
            }
        }
    }
}