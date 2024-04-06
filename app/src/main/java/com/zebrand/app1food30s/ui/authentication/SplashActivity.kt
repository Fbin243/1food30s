package com.zebrand.app1food30s.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.auth.User
import com.zebrand.app1food30s.databinding.ActivitySplashBinding
import com.zebrand.app1food30s.ui.main.AdminActivity
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FirebaseUtils
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES) {
                // Dark mode is enabled, so force light mode
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }
        setContentView(binding.root)

        val mySharedPreferences = MySharedPreferences.getInstance(this)

        android.os.Handler(Looper.getMainLooper()).postDelayed({
            nextActivity(mySharedPreferences)
        }, 1500)

    }

    private fun nextActivity(ref: MySharedPreferences) {
        val user = FirebaseUtils.fireAuth.currentUser
//        Log.d("userInfo", user.toString() + ref.getBoolean(SingletonKey.KEY_LOGGED).toString())
        if (user == null || !ref.getBoolean(SingletonKey.KEY_LOGGED)) {
            // Not logged in
            GlobalUtils.myStartActivityFinishAffinity(this, MainActivity::class.java)
        } else {
            // Logged in
            myStartActivity(MainActivity::class.java)
        }
    }

    private fun myStartActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        startActivity(intent)
        finishAffinity()
    }
}