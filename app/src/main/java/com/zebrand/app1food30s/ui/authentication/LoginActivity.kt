package com.zebrand.app1food30s.ui.authentication

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseUser
import com.zebrand.app1food30s.data.Account
import com.zebrand.app1food30s.databinding.ActivityLoginBinding
import com.zebrand.app1food30s.ui.main.AdminActivity
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ultis.FireStoreUltis
import com.zebrand.app1food30s.ultis.FirebaseUtils
import com.zebrand.app1food30s.ultis.MySharedPreferences

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding

    companion object{
        const val KEY_FIRST_LOGIN = "KEY_FIRST_LOGIN"
        const val KEY_ROLE = "IS_ADMIN"
        const val KEY_USER_ID = "KEY_USER_ID"
    }

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
//            val intent = Intent(this, MainActivity::class.java)
//            // Include extra information to tell MainActivity to load ProfileAfterLoginFragment
//            intent.putExtra("loadProfileFragment", true)
//            startActivity(intent)
//            finish() // Optional: Close LoginActivity once the transition is made
            onClickLogin()
        }
    }

    private fun onClickLogin() {
        val email = binding.tvLogin.text.toString().trim()
        val password = binding.tvPassword.text.toString().trim()

        val mAuth = FirebaseUtils.fireAuth
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Set KEY_FIRST_LOGIN
                    val mySharePreference = MySharedPreferences.getInstance(this)
                    setFirstLoginShareRef(mySharePreference)

                    // Authorization
                    val user = mAuth.currentUser
                    authorization(user, mySharePreference)

                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun authorization(user: FirebaseUser?, mySharePreference: MySharedPreferences) {
        val query = FireStoreUltis.mDBUserRef.whereEqualTo("email", user?.email).limit(1)
        query.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (documentSnapshot in queryDocumentSnapshots) {
                val userInfo = documentSnapshot.toObject(Account::class.java)
                userInfo.id = documentSnapshot.id

                // da login //changed
                if (userInfo.isAdmin) {
                    mySharePreference.setBoolean(KEY_ROLE, true)
                    myStartActivity(AdminActivity::class.java)
                } else {
                    mySharePreference.setBoolean(KEY_ROLE, false)
                    myStartActivity(MainActivity::class.java)
                }

                mySharePreference.setString(KEY_USER_ID, userInfo.id)
            }
        }
    }

    private fun myStartActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        startActivity(intent)
        finishAffinity()
    }

    private fun setFirstLoginShareRef(mySharePreference: MySharedPreferences) {
        mySharePreference.setBoolean(KEY_FIRST_LOGIN, true)
    }
}