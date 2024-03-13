package com.zebrand.app1food30s.ui.cart_checkout

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebrand.app1food30s.adapter.CheckoutItemsAdapter
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        observeCartItems()
        handleCloseCheckoutScreen()
    }

    private fun observeCartItems() {
        sharedViewModel.cartItems.observe(this) { cartItems ->
            Log.d("CheckoutActivity Log", "Observing cart items: ${cartItems.size} items")
            val adapter = CheckoutItemsAdapter(cartItems)
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
