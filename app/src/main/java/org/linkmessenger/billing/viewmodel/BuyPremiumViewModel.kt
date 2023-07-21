package org.linkmessenger.billing.viewmodel

import android.os.Parcelable
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import kotlinx.parcelize.Parcelize

class BuyPremiumViewModel:ViewModel() {
    val products: MutableLiveData<MutableList<ProductDetails>> = MutableLiveData()
    var selectedIndex:MutableLiveData<Int> = MutableLiveData(0)
    val productIds:MutableSet<String> = mutableSetOf("premium_1","premium_6", "premium_12")
    val subsIds:MutableSet<String> = mutableSetOf("premium","premium_s_6", "premium_s_12")
    fun updateProducts(items:MutableList<ProductDetails>){
        products.postValue(items)
        if (items.size>0){
            selectedIndex.postValue(0)
        }
    }
    fun selectProduct(index:Int){
        selectedIndex.postValue(index)
    }
    fun getSelectedProduct():ProductDetails{
        return products.value!![selectedIndex.value!!]
    }

    fun handlePurchase(purchases: MutableList<Purchase>) {
        if (purchases.isEmpty()){
            return
        }
        val purchase = purchases[0]

    }
}

enum class ProductType {
    Subs, InApp
}