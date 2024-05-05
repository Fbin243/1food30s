package com.zebrand.app1food30s.ui.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.databinding.ActivityEditProfileBinding
import com.zebrand.app1food30s.ui.checkout.AddressMapFragment
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBUserRef
import com.zebrand.app1food30s.utils.FirebaseService
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Date

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private var addressMapFragment: AddressMapFragment? = null
    private var userId: String? = null
    private var isAdmin: Boolean? = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("USER_ID")
        setupTvAddressMapFragment()
        userId?.let { fetchUserInformation(it) }
        setupUI()
    }

    private fun setupTvAddressMapFragment() {
        addressMapFragment = supportFragmentManager.findFragmentById(R.id.address_map_fragment) as? AddressMapFragment
    }

    private fun setupUI() {
        binding.backIcon.setOnClickListener { finish() }
        binding.saveBtn.setOnClickListener {
            userId?.let {
                updateUserInformation(it)
            } ?: run {
//                Toast.makeText(this, "User ID is missing.", Toast.LENGTH_SHORT).show()
                Utils.showCustomToast(this, "User ID is missing.", "error")
            }
        }
    }

    private fun updateUserInformation(userId: String) {
        val firstName = binding.editFirstName.text.toString().trim()
        val lastName = binding.editLastName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val address = addressMapFragment?.getAddressText() ?: ""
        val phone = binding.editPhone.text.toString().trim()

        val userInforUpdate = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "address" to address,
            "phone" to phone,
            "date" to Date()
        )

        mDBUserRef.document(userId).update(userInforUpdate as Map<String, Any>)
            .addOnSuccessListener {
                if (isAdmin == true) {
                    FirebaseService.saveShopAddress(address)
                }
//                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_LONG).show()
                Utils.showCustomToast(this, "Profile updated successfully", "success")
                finish()
            }
            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error updating product: ${e.message}", Toast.LENGTH_LONG).show()
                Utils.showCustomToast(this, "Error updating product: ${e.message}", "error")
            }
    }

    private fun fetchUserInformation(userId: String) {
        lifecycleScope.launch {
            try {
                val documentSnapshot = mDBUserRef.document(userId).get().await()
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    updateUIWithUserInfo(it)
                }
            } catch (e: Exception) {
                Log.e("EditProfileActivity", "Error fetching user information", e)
            }
        }
    }

    private fun updateUIWithUserInfo(user: User) {
        isAdmin = user.admin

        binding.editFirstName.setText(user.firstName)
        binding.editLastName.setText(user.lastName)
        binding.editEmail.setText(user.email)
        binding.editPhone.setText(user.phone)

        // Ensure the fragment is ready before setting text
        if (isFragmentReady()) {
            addressMapFragment?.setAddressText(user.address)
        } else {
            Log.e("EditProfileActivity", "Fragment is not ready")
        }
    }

    private fun isFragmentReady(): Boolean {
        return addressMapFragment?.isAdded == true && addressMapFragment?.view != null
    }
}
