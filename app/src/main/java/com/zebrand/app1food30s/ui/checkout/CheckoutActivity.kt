package com.zebrand.app1food30s.ui.checkout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CheckoutItemsAdapter
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding
import com.zebrand.app1food30s.ui.cart.CartRepository

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding
    private val checkoutItemsAdapter = CheckoutItemsAdapter()
    private lateinit var cartRepository: CartRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cartRepository = CartRepository(FirebaseFirestore.getInstance())

        setupRecyclerView()
        loadCartData()
        handleCloseCheckoutScreen()
    }

    private fun setupRecyclerView() {
        binding.checkoutItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.checkoutItemsRecyclerView.adapter = checkoutItemsAdapter
    }

//    private fun updateUIWithCartSummary() {
//        // Assume you've received the data as a JSON string and a total price
//        // For demonstration, let's fetch them directly
//        val itemDescriptionsJson = intent.getStringExtra("item_descriptions")
//        val totalPrice = intent.getDoubleExtra("total_price", 0.0)
//
//        // Deserialize the JSON to List<String>
//        val itemDescriptions = itemDescriptionsJson?.let { json ->
//            // Use your preferred method to deserialize, e.g., Gson, Moshi, or Kotlinx.serialization
//            // Example with Gson (make sure to add the dependency and initialize Gson)
//            val gson = Gson()
//            gson.fromJson(json, Array<String>::class.java).toList()
//        } ?: emptyList()
//
//        // Update the RecyclerView
//        checkoutItemsAdapter.setItems(itemDescriptions)
//
//        // Update the total price TextView
//        binding.tvCartTotalAmount.text = getString(R.string.product_price_number, totalPrice)
//        binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
//    }

    private fun loadCartData() {
        val cartId = intent.getStringExtra("cart_id") ?: return

        cartRepository.fetchProductDetailsForCartItems(cartId) { detailedCartItems, totalPrice ->
            detailedCartItems?.let {
                // Update UI on the main thread
                runOnUiThread {
                    checkoutItemsAdapter.setItems(it)
                    binding.tvCartTotalAmount.text = getString(R.string.product_price_number, totalPrice)
                    binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
                }
            } ?: runOnUiThread {
                // Handle error or empty state
                binding.tvCartTotalAmount.text = getString(R.string.product_price_number, 0.0)
                binding.textViewAmount.text = getString(R.string.product_price_number, 0.0)
            }
        }
    }

    private fun handleCloseCheckoutScreen() {
        binding.ivBack.root.setOnClickListener {
            finish()
        }
    }
}

