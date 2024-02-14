package com.zebrand.app1food30s.ui.authentication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import com.zebrand.app1food30s.R

class SignUpActivity : AppCompatActivity() {
    private lateinit var tv_login: TextView
    private lateinit var back_icon: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        init()
        events()
    }

    private fun init(){
        tv_login = findViewById(R.id.tv_login)
        back_icon = findViewById(R.id.back_icon)
    }

    private fun events(){
        tv_login.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        back_icon.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }
}