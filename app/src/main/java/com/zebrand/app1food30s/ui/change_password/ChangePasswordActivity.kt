package com.zebrand.app1food30s.ui.change_password

import android.os.Bundle
import com.zebrand.app1food30s.R
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.zebrand.app1food30s.databinding.ActivityChangePasswordBinding

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        setupEvents()
    }

    private fun setupEvents() {
        binding.saveBtn.setOnClickListener {
            val currentPassword = binding.tvCurrentPassword.text.toString().trim()
            if(currentPassword.isEmpty()){
                Toast.makeText(this, "Current password is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            changePassword(currentPassword)
        }

        binding.backIcon.root.setOnClickListener {
            finish()
        }
    }

    private fun changePassword(currentPassword: String) {
        val user = auth.currentUser
        val email = user?.email ?: return

        // Tái xác thực người dùng với mật khẩu hiện tại
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val newPassword = binding.tvNewPassword.text.toString().trim()
                val confirmNewPassword = binding.tvConfirmNewPassword.text.toString().trim()

                // Kiểm tra tính hợp lệ của mật khẩu mới và xác nhận mật khẩu
                if (newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                    Toast.makeText(this, "Please fill out all fields.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                if (newPassword != confirmNewPassword) {
                    Toast.makeText(this, "New password and confirm password do not match.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                if (newPassword.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }

                // Cập nhật mật khẩu mới
                user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                    if (updateTask.isSuccessful) {
                        Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_SHORT).show()
                        finish() // Quay lại màn hình trước sau khi cập nhật thành công
                    } else {
                        Toast.makeText(this, "Failed to update password.", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Failed to re-authenticate. Please check your current password.", Toast.LENGTH_LONG).show()
            }
        }
    }
}
