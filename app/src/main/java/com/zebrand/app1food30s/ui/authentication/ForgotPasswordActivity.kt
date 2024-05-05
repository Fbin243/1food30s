package com.zebrand.app1food30s.ui.authentication

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Looper
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityForgotPasswordBinding
import com.zebrand.app1food30s.databinding.DialogDeleteAccountBinding
import com.zebrand.app1food30s.utils.FirebaseUtils
import com.zebrand.app1food30s.utils.Utils
import com.zebrand.app1food30s.utils.ValidateInput

class ForgotPasswordActivity : AppCompatActivity() {
    lateinit var binding: ActivityForgotPasswordBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        events()
    }

    private fun events() {
        binding.tvLogin.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.confirmBtn.setOnClickListener {
            if (checkValid()) {
                showCustomConfirmDialogBox()
            }
        }

        ValidateInput.emailFocusListener(this, binding.tvEmail, binding.emailContainer)
    }

    private fun checkValid(): Boolean {
        binding.emailContainer.error = ValidateInput.validEmail(this, binding.tvEmail)

        val validEmail = binding.emailContainer.error == null

        return validEmail
    }

    private fun showCustomConfirmDialogBox() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        val bindingSub: DialogDeleteAccountBinding =
            DialogDeleteAccountBinding.inflate(layoutInflater)
        dialog.setContentView(bindingSub.root)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val acceptBtn: Button = dialog.findViewById(R.id.saveBtn)
        val cancel: Button = dialog.findViewById(R.id.cancelBtn)

        bindingSub.title = "Forgot password"
        bindingSub.content = resources.getString(R.string.txt_confirm_forgot_password)

        acceptBtn.setOnClickListener {
            resetPassword()
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun resetPassword() {
        val mAuth = FirebaseUtils.fireAuth
        val email = binding.tvEmail.text.toString()

        mAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
//                    Toast.makeText(this, "Email sent.", Toast.LENGTH_SHORT).show()
                    Utils.showCustomToast(this, "Email sent.", "success")

                    android.os.Handler(Looper.getMainLooper()).postDelayed({
                        onBackPressedDispatcher.onBackPressed()
                    }, 1500)
                }
            }
    }
}