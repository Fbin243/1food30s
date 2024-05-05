package com.zebrand.app1food30s.ui.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.data.entity.Wishlist
import com.zebrand.app1food30s.databinding.ActivitySignUpBinding
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FirebaseUtils
import com.zebrand.app1food30s.utils.Utils
import com.zebrand.app1food30s.utils.ValidateInput


class SignUpActivity : AppCompatActivity() {
    lateinit var binding: ActivitySignUpBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        events()
    }

    private fun events() {
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.signUpBtn.setOnClickListener {
            onClickSignUp()
        }

        ValidateInput.firstNameFocusListener(this, binding.tvFirstName, binding.firstNameContainer)
        ValidateInput.lastNameFocusListener(this, binding.tvLastName, binding.lastNameContainer)
        ValidateInput.emailFocusListener(this, binding.tvEmail, binding.emailContainer)
        ValidateInput.passwordFocusListener(this, binding.tvPassword, binding.passwordContainer)
        ValidateInput.rePasswordFocusListener(
            this,
            binding.tvPassword,
            binding.tvRePassword,
            binding.rePasswordContainer
        )
    }

    private fun onClickSignUp() {
        if (checkValid()) {
            val mAuth = FirebaseUtils.fireAuth
            val firstName = binding.tvFirstName.text.toString()
            val lastName = binding.tvLastName.text.toString()
            val email = binding.tvEmail.text.toString()
            val password = binding.tvPassword.text.toString()

            mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Đăng ký thành công, người dùng đã được tạo
                        //FirebaseUser user = mAuth.getCurrentUser();
                        // Tiếp tục xử lý sau khi đăng ký thành công
                        setUserData(
                            User(
                                firstName = firstName,
                                lastName = lastName,
                                email = email,
                                admin = false,
                            )
                        )

//                        Toast.makeText(this, "Sign up successfully", Toast.LENGTH_SHORT).show()
                        Utils.showCustomToast(this, "Sign up successfully", "success")

                        finish()
                    } else {
                        // Đăng ký thất bại
                        val exception = task.exception
                        if (exception is FirebaseAuthUserCollisionException) {
//                            Toast.makeText(this, "Email exists", Toast.LENGTH_SHORT).show()
                            Utils.showCustomToast(this, "Email exists", "error")
                        } else {
                            // Xử lý các trường hợp lỗi khác
                            Utils.showCustomToast(this, "Sign up failed", "error")
                        }
                    }
                }
        } else {
//            Toast.makeText(this, "Sign up failed", Toast.LENGTH_SHORT).show()
            Utils.showCustomToast(this, "Sign up failed", "error")
        }
    }

    private fun setUserData(user: User) {
        Utils.setUserDataInFireStore(user)
    }

    private fun checkValid(): Boolean {
        binding.firstNameContainer.error = ValidateInput.validFirstName(this, binding.tvFirstName)
        binding.lastNameContainer.error = ValidateInput.validLastName(this, binding.tvLastName)
        binding.emailContainer.error = ValidateInput.validEmail(this, binding.tvEmail)
        binding.passwordContainer.error = ValidateInput.validPassword(this, binding.tvPassword)
        binding.rePasswordContainer.error =
            ValidateInput.validRePassword(this, binding.tvPassword, binding.tvRePassword)

        val validFirstName = binding.firstNameContainer.error == null
        val validLastName = binding.lastNameContainer.error == null
        val validEmail = binding.emailContainer.error == null
        val validPassword = binding.passwordContainer.error == null
        val validRePassword = binding.rePasswordContainer.error == null

        return validEmail && validPassword && validRePassword && validFirstName && validLastName
    }
}