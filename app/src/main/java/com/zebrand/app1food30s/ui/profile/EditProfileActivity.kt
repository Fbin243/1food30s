package com.zebrand.app1food30s.ui.profile

import com.zebrand.app1food30s.R
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.data.User
import com.zebrand.app1food30s.databinding.ActivityEditProfileBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date
import java.util.UUID

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private val PICK_IMAGE_REQUEST = 71 // Unique request code

    private lateinit var firstNameEditText: TextInputEditText
    private lateinit var lastNameEditText: TextInputEditText
    private lateinit var emailEditText: TextInputEditText
    private lateinit var addressEditText: TextInputEditText
    private lateinit var phoneEditText: TextInputEditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        firstNameEditText = binding.editFirstName

        val userId = intent.getStringExtra("USER_ID")
        Log.d("MainActivity", "idUserInActivityEditProfile: $userId")
//        firstNameEditText.setText(userId)
//        if (userId == null) {
////            fetchUserInformation(userId)
//            firstNameEditText.setText("id null")
//        }

        setupUI()
    }

    private fun setupUI() {
        firstNameEditText = binding.editFirstName
        lastNameEditText = binding.editLastName
        emailEditText = binding.editEmail
        addressEditText = binding.editAddress
        phoneEditText = binding.editPhone
        saveButton = binding.saveBtn

        saveButton.setOnClickListener {
            val userId = intent.getStringExtra("USER_ID")
            userId?.let {
                updateUserInformation(it)
            } ?: run {
                Toast.makeText(this, "Error: User ID is missing.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserInformation(userId: String) {
        val firstName = firstNameEditText.text.toString().trim()
        val lastName = lastNameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val address = addressEditText.text.toString().trim()
        val phone = phoneEditText.text.toString().trim()

        val userInforUpdate = hashMapOf<String, Any>(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "address" to address,
            "phone" to phone,
            "date" to Date()
        )

        fireStore.collection("products").document(userId).update(userInforUpdate)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_LONG).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error updating product: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun fetchUserInformation(userId: String) {
        lifecycleScope.launch {
            try {
                val documentSnapshot = fireStore.collection("accounts").document(userId).get().await()
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    with(binding) {
                        firstNameEditText.setText(it.firstName)
                        lastNameEditText.setText(it.lastName)
                        emailEditText.setText(it.email)
                        addressEditText.setText(it.address)
                        phoneEditText.setText(it.phone)
                    }
                }
            } catch (e: Exception) {
                // Xử lý lỗi
            }
        }
    }
}
