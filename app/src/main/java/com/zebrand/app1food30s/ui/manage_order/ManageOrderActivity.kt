package com.zebrand.app1food30s.ui.manage_order

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityManageOrderBinding
import com.zebrand.app1food30s.ui.my_order.MyOrderDetailsActivity

class ManageOrderActivity : AppCompatActivity() {
    lateinit var binding: ActivityManageOrderBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOrderBinding.inflate(layoutInflater)
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
        binding.testOrderStatus.root.setOnClickListener{
            val intent = Intent(this, ManageOrderDetailsActivity::class.java)
            startActivity(intent)
        }
    }
}