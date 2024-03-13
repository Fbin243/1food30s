package com.zebrand.app1food30s.ui.view.cart_checkout

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebrand.app1food30s.adapter.CheckoutItemsAdapter
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding
import com.zebrand.app1food30s.ui.viewmodel.ProductViewModel

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeCheckoutItems()
        handleCloseCheckoutScreen()
    }

    private fun observeCheckoutItems() {
        viewModel.checkoutItems.observe(this) { checkoutItems ->
//            Log.d("CheckoutActivity Log", "Observing checkout items: ${checkoutItems.size} items")
            val adapter = CheckoutItemsAdapter(checkoutItems) // Ensure CheckoutItemsAdapter accepts CheckoutItem instances
            binding.checkoutItemsRecyclerView.layoutManager = LinearLayoutManager(this)
            binding.checkoutItemsRecyclerView.adapter = adapter
        }
    }

    private fun handleCloseCheckoutScreen() {
        binding.ivBack.root.setOnClickListener {
            finish()
        }
    }
}
