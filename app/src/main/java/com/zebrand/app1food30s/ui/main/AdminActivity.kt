package com.zebrand.app1food30s.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityAdminBinding
import com.zebrand.app1food30s.databinding.ActivityMainBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}