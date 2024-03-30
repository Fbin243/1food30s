package com.zebrand.app1food30s.ui.checkout

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CheckoutItemsAdapter
import com.zebrand.app1food30s.data.entity.DetailedCartItem
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding
import com.zebrand.app1food30s.ui.cart.CartRepository
import com.zebrand.app1food30s.ui.main.MainActivity

class CheckoutActivity : AppCompatActivity(), CheckoutMVPView {

    private lateinit var binding: ActivityCheckoutBinding
    private val checkoutItemsAdapter = CheckoutItemsAdapter()
    private lateinit var cartRepository: CartRepository
    private lateinit var presenter: CheckoutPresenter
    private lateinit var cartId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        cartRepository = CartRepository(FirebaseFirestore.getInstance())
        presenter = CheckoutPresenter(this, cartRepository)

        setupRecyclerView()
        handleCloseCheckoutScreen()

        cartId = intent.getStringExtra("cart_id") ?: return
        presenter.loadCartData(cartId)

        handlePlaceOrderButton()
    }

    override fun navigateToOrderConfirmation(showOrderConfirmation: Boolean) {
        if (showOrderConfirmation) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("showOrderConfirmation", true)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            // Handle the failure case appropriately, maybe show an error message
        }
    }

    // presenter.onPlaceOrderClicked(cartId)
    private fun handlePlaceOrderButton() {
        binding.btnPlaceOrder.setOnClickListener {
            presenter.onPlaceOrderClicked(cartId)
//            presenter.testCallback(object : CheckoutPresenter.SimpleCallback {
//                override fun onSuccess() {
//                    Log.d("Test00", "Test operation succeeded!")
//                    // Success handling
//                    runOnUiThread {
//                        // Assuming you have a method in CheckoutActivity to handle success
//                        navigateToOrderConfirmation(true)
//                    }
//                }
//
//                override fun onFailure(error: String) {
//                    Log.d("Test00", "Test operation failed: $error")
//                    // Error handling
//                    runOnUiThread {
//                        // Assuming you have a method in CheckoutActivity to handle error
//                        displayError(error)
//                    }
//                }
//            })
        }
    }


    private fun setupRecyclerView() {
        binding.checkoutItemsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.checkoutItemsRecyclerView.adapter = checkoutItemsAdapter
    }

    override fun displayCartItems(detailedCartItems: List<DetailedCartItem>, totalPrice: Double) {
        runOnUiThread {
            checkoutItemsAdapter.setItems(detailedCartItems)
            binding.tvCartTotalAmount.text = getString(R.string.product_price_number, totalPrice)
            binding.textViewAmount.text = getString(R.string.product_price_number, totalPrice)
        }
    }

    override fun displayError(error: String) {
        runOnUiThread {
            // Handle error or empty state
            binding.tvCartTotalAmount.text = getString(R.string.product_price_number, 0.0)
            binding.textViewAmount.text = getString(R.string.product_price_number, 0.0)
        }
    }

    private fun handleCloseCheckoutScreen() {
        binding.ivBack.root.setOnClickListener {
            finish()
        }
    }
}

