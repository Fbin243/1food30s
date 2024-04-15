package com.zebrand.app1food30s.ui.change_language

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityChangeLanguageBinding
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils

class ChangeLanguageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChangeLanguageBinding
    private lateinit var mySharedPreferences: MySharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangeLanguageBinding.inflate(layoutInflater)
        mySharedPreferences = MySharedPreferences.getInstance(this)
        setContentView(binding.root)
        binding.languageSelection.check(
            when(mySharedPreferences.getString(SingletonKey.KEY_LANGUAGE_CODE)) {
                "en" -> R.id.englishLanguage
                "vi" -> R.id.vietnameseLanguage
                else -> R.id.englishLanguage
            }
        )
        binding.languageSelection.setOnCheckedChangeListener { _, checkId ->
            when(checkId) {
                R.id.englishLanguage -> {
                    mySharedPreferences.setString(SingletonKey.KEY_LANGUAGE_CODE, "en")
                    Utils.setLocale(this, "en")
                }
                R.id.vietnameseLanguage -> {
                    mySharedPreferences.setString(SingletonKey.KEY_LANGUAGE_CODE, "vi")
                    Utils.setLocale(this, "vi")
                }
            }
        }
        handleCloseScreen()
    }
    private fun handleCloseScreen() {
        binding.saveBtn.setOnClickListener {
            restartApp()
        }
        binding.backBtn.root.setOnClickListener {
            finish()
        }
    }

    fun restartApp() {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        ActivityCompat.finishAffinity(this)
    }
}



