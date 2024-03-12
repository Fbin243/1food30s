package com.zebrand.app1food30s.ui.cart_checkout

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleCloseCheckoutScreen()
    }

    private fun handleCloseCheckoutScreen() {
        binding.ivBack.root.setOnClickListener {
            finish()
        }
    }
}