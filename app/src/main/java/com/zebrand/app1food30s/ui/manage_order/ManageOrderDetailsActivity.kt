package com.zebrand.app1food30s.ui.manage_order

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityManageOrderDetailsBinding

class ManageOrderDetailsActivity : AppCompatActivity() {
    lateinit var binding: ActivityManageOrderDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        events()
    }

    private fun events(){
        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}