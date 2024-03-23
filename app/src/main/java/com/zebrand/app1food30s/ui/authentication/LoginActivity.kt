package com.zebrand.app1food30s.ui.authentication

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseUser
import com.zebrand.app1food30s.data.User
import com.zebrand.app1food30s.databinding.ActivityLoginBinding
import com.zebrand.app1food30s.ui.main.AdminActivity
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ultis.FireStoreUtils
import com.zebrand.app1food30s.ultis.FirebaseUtils
import com.zebrand.app1food30s.ultis.MySharedPreferences
import com.zebrand.app1food30s.ultis.SingletonKey
import com.zebrand.app1food30s.ultis.ValidateInput

class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val mySharePreference = MySharedPreferences.getInstance(this)

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
        init()
        events()
    }

    private fun init(){
        val rememberMe = mySharePreference.getBoolean(SingletonKey.KEY_REMEMBER_ME)
        val email = mySharePreference.getString(SingletonKey.KEY_EMAIL)
        val password = mySharePreference.getString(SingletonKey.KEY_PASSWORD)
        binding.rememberMe.isChecked = rememberMe
        if(rememberMe){
            binding.tvEmail.setText(email)
            binding.tvPassword.setText(password)
        }
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
            onClickLogin()
        }

        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.rememberMe.setOnCheckedChangeListener { _, isChecked -> //buttonView, isChecked
            mySharePreference.setBoolean(SingletonKey.KEY_REMEMBER_ME, isChecked)
        }

        ValidateInput.emailFocusListener(this, binding.tvEmail, binding.emailContainer)
        ValidateInput.passwordFocusListener(this, binding.tvPassword, binding.passwordContainer)
    }

    private fun onClickLogin() {
        if(checkValid()){
        val email = binding.tvEmail.text.toString().trim()
        val password = binding.tvPassword.text.toString().trim()

        val mAuth = FirebaseUtils.fireAuth
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Set KEY_LOGGED
                    setKeyShareRef(email, password)

                    // Authorization
                    val user = mAuth.currentUser
                    authorization(user, mySharePreference)
                    Log.d("userInfo", user?.uid.toString())

                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun authorization(user: FirebaseUser?, mySharePreference: MySharedPreferences) {
        val query = FireStoreUtils.mDBUserRef.whereEqualTo("email", user?.email).limit(1)
        query.get().addOnSuccessListener { queryDocumentSnapshots ->
            for (documentSnapshot in queryDocumentSnapshots) {
                val userInfo = documentSnapshot.toObject(User::class.java)
                userInfo.id = documentSnapshot.id

                // da login //changed
                if (userInfo.isAdmin) {
                    mySharePreference.setBoolean(SingletonKey.IS_ADMIN, true)
                    myStartActivity(AdminActivity::class.java)
                } else {
                    mySharePreference.setBoolean(SingletonKey.IS_ADMIN, false)
                    myStartActivity(MainActivity::class.java)
                }

                mySharePreference.setString(SingletonKey.KEY_USER_ID, userInfo.id.toString())
            }
        }
    }

    private fun myStartActivity(cls: Class<*>) {
        val intent = Intent(this, cls)
        startActivity(intent)
        finishAffinity()
    }

    private fun setKeyShareRef(email: String, password: String) {
        mySharePreference.setBoolean(SingletonKey.KEY_LOGGED, true)
        mySharePreference.setString(SingletonKey.KEY_EMAIL, email)
        mySharePreference.setString(SingletonKey.KEY_PASSWORD, password)
    }

    private fun checkValid(): Boolean {
        binding.emailContainer.error = ValidateInput.validEmail(this, binding.tvEmail)
        binding.passwordContainer.error = ValidateInput.validPassword(this, binding.tvPassword)

        val validEmail = binding.emailContainer.error == null
        val validPassword = binding.passwordContainer.error == null

        return validEmail && validPassword
    }
}