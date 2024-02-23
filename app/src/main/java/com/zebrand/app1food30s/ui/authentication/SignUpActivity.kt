package com.zebrand.app1food30s.ui.authentication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityForgotPasswordBinding
import com.zebrand.app1food30s.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        events()
    }

    private fun events(){
        binding.tvLogin.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}