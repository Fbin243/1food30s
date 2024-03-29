package com.zebrand.app1food30s.ui.my_order

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityMyOrderBinding
import com.zebrand.app1food30s.databinding.ActivityMyOrderDetailsBinding
import com.zebrand.app1food30s.ui.authentication.ForgotPasswordActivity

class MyOrderActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyOrderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrderBinding.inflate(layoutInflater)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        setContentView(binding.root)

        events()
    }

    private fun events(){
        binding.testOrderStatus.root.setOnClickListener {
            val intent = Intent(this, MyOrderDetailsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun directOrderStatus(){
        val intent = Intent(this, MyOrderDetailsActivity::class.java)
        startActivity(intent)
    }
}