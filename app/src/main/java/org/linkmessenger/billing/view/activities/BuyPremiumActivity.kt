package org.linkmessenger.billing.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.google.common.collect.ImmutableList
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.linkmessenger.billing.adapters.PremiumAdapter
import org.linkmessenger.billing.viewmodel.BuyPremiumViewModel
import org.linkmessenger.billing.viewmodel.ProductType
import org.thoughtcrime.securesms.databinding.ActivityBuyPremiumBinding

class BuyPremiumActivity : AppCompatActivity(), KoinComponent, PurchasesUpdatedListener {
    companion object{
        fun newIntent(context: Context, type: ProductType):Intent{
            return Intent(context, BuyPremiumActivity::class.java).apply {
                putExtra("type", type)
            }
        }
    }
    private lateinit var binding: ActivityBuyPremiumBinding
    private lateinit var adapter: PremiumAdapter
    private lateinit var layoutManager: LinearLayoutManager
    private val viewModel:BuyPremiumViewModel = get()
    private lateinit var billingClient: BillingClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBuyPremiumBinding.inflate(layoutInflater)
        setContentView(binding.root)


        billingClient = BillingClient.newBuilder(this)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        adapter = PremiumAdapter()
        layoutManager = LinearLayoutManager(this)

        binding.items.layoutManager = layoutManager
        binding.items.adapter = adapter

        binding.closeButton.setOnClickListener {
            onBackPressed()
        }
        binding.root.setOnRefreshListener {
//            getProducts()
        }
        binding.buyButton.setOnClickListener {
            launchPurchase()
        }
        binding.items.setOnItemClickListener { _, position ->
            viewModel.selectProduct(position)
        }

        viewModel.products.observe(this){
            adapter.update(it)
        }
        viewModel.selectedIndex.observe(this){
            adapter.setSelection(it)
        }
    }

    override fun onResume() {
        super.onResume()
        connectToBilling()
    }
    private fun connectToBilling(){
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
//                    getProducts()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }
    fun getProducts(productType:ProductType){
        val type = if(productType==ProductType.Subs){
            BillingClient.ProductType.SUBS
        }else{
            BillingClient.ProductType.INAPP
        }
        if (billingClient.connectionState != BillingClient.ConnectionState.CONNECTED){
            binding.root.isRefreshing = false
            connectToBilling()
            return
        }
        val productList = if(productType == ProductType.Subs){
            viewModel.subsIds.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(type)
                    .build()
            }
        }else{
            viewModel.productIds.map {
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(type)
                    .build()
            }
        }
        val queryProductDetailsParams =
            QueryProductDetailsParams.newBuilder()
                .setProductList(productList).build()

        billingClient.queryProductDetailsAsync(queryProductDetailsParams) {
                billingResult,
                productDetailsList ->
            // check billingResult
            // process returned productDetailsList


            binding.root.isRefreshing = false

            productDetailsList.sortBy { it.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.priceAmountMicros }
            viewModel.updateProducts(productDetailsList)
        }
    }
    private fun launchPurchase(){
        val productDetails = viewModel.getSelectedProduct()
        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                .setProductDetails(productDetails)
                // to get an offer token, call ProductDetails.subscriptionOfferDetails()
                // for a list of offers that are available to the user
//                .setOfferToken(selectedOfferToken)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

// Launch the billing flow
        val billingResult = billingClient.launchBillingFlow(this, billingFlowParams)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            viewModel.handlePurchase(purchases)
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            // Handle an error caused by a user cancelling the purchase flow.
        } else {
            // Handle any other error codes.
        }
    }
}