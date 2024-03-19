package com.zebrand.app1food30s.ui.authentication

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseUser
import com.zebrand.app1food30s.data.Account
import com.zebrand.app1food30s.databinding.ActivitySplashBinding
import com.zebrand.app1food30s.ui.main.AdminActivity
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ultis.FireStoreUltis
import com.zebrand.app1food30s.ultis.FirebaseUtils
import com.zebrand.app1food30s.ultis.MySharedPreferences
import java.util.logging.Handler

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    lateinit var binding: ActivitySplashBinding
    companion object {
        const val KEY_FIRST_INSTALL = "KEY_FIRST_INSTALL"
    }
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
        }, 2000)

    }

    private fun nextActivity(ref: MySharedPreferences) {
        val user = FirebaseUtils.fireAuth.currentUser
        if (user == null || !ref.getBoolean(LoginActivity.KEY_FIRST_LOGIN)) {
            // Not logged in
            myStartActivity(LoginActivity::class.java)
        } else {
            // Logged in
            authorization(user)
        }
    }

    private fun authorization(user: FirebaseUser) {
        val query = FireStoreUltis.mDBUserRef.whereEqualTo("email", user.email).limit(1)
        query.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (documentSnapshot in queryDocumentSnapshots) {
                val userInfo = documentSnapshot.toObject(Account::class.java)
                if (userInfo.isAdmin) {
                    myStartActivity(AdminActivity::class.java)
                } else {
                    myStartActivity(MainActivity::class.java)
                }
            }
        }
    }

    private fun myStartActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        startActivity(intent)
        finishAffinity()
    }
}