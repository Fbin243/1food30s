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

class LoginActivity : AppCompatActivity() {
    private lateinit var tv_sign_up:TextView
    private lateinit var tv_forgot_password:TextView
    private lateinit var back_icon:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }

        setContentView(R.layout.activity_login)

        init()
        events()
    }

    private fun init(){
        tv_sign_up = findViewById(R.id.tv_sign_up)
        tv_forgot_password = findViewById(R.id.tv_forgot_password)
        back_icon = findViewById(R.id.back_icon)
    }

    private fun events(){
        tv_sign_up.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        tv_forgot_password.setOnClickListener {
            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

//        back_icon.setOnClickListener {
//            onBackPressedDispatcher.onBackPressed()
//        }
    }
}