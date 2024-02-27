package com.zebrand.app1food30s.ui.authentication

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityLoginBinding
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ui.profile.ProfileAfterLoginFragment

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        setContentView(binding.root)

        events()
    }

    private fun events() {
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.tvForgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

        binding.loginBtn.setOnClickListener {
            // Intent to start MainActivity
            val intent = Intent(this, MainActivity::class.java)
            // Include extra information to tell MainActivity to load ProfileAfterLoginFragment
            intent.putExtra("loadProfileFragment", true)
            startActivity(intent)
            finish() // Optional: Close LoginActivity once the transition is made
        }
    }
}