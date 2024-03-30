package com.zebrand.app1food30s.ui.profile

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.ui.admin_stats.AdminStatsActivity
import com.zebrand.app1food30s.databinding.FragmentProfileAfterLoginBinding
import com.zebrand.app1food30s.ui.authentication.DeleteAccountActivity
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import com.zebrand.app1food30s.ui.change_password.ChangePasswordActivity
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ultis.FirebaseUtils
import com.zebrand.app1food30s.ultis.FirebaseUtils.fireStorage
import com.zebrand.app1food30s.ultis.FirebaseUtils.fireStore
import com.zebrand.app1food30s.ultis.GlobalUtils
import com.zebrand.app1food30s.ultis.MySharedPreferences
import com.zebrand.app1food30s.ultis.SingletonKey
import java.util.Date
import java.util.UUID


class ProfileAfterLoginFragment : Fragment() {
    private lateinit var binding: FragmentProfileAfterLoginBinding
    private var idUser: String? = null
    private lateinit var avaImageView: ImageView
    private val PICK_IMAGE_REQUEST = 71 // Unique request code
    private var imageUri: Uri? = null
    private var currentImagePath: String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileAfterLoginBinding.inflate(inflater, container, false)
        idUser = arguments?.getString("USER_ID")
        Log.d("MainActivity", "idUserProfileAfterLoginFragment: $idUser")
        events()

        // ========== Code này ở branch Hai3 (đã sửa lại dùng binding) =========
        binding.ava.setOnClickListener {
            startImagePicker()
        }
        // Set up the click listener for the layoutMyOrders
        binding.layoutMyOrders.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, AdminStatsActivity::class.java)
            startActivity(intent)
        }
        // ================================================================
//        binding.username.setText(idUser)
        binding.layoutEditProfile.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, EditProfileActivity::class.java).apply {
                putExtra("USER_ID", idUser)
            }
            startActivity(intent)
        }

        binding.layoutChangePassword.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, ChangePasswordActivity::class.java).apply {
                putExtra("USER_ID", idUser)
            }
            startActivity(intent)
        }

        return binding.root
    }

    private fun startImagePicker() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == AppCompatActivity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data!!
            Picasso.get().load(imageUri).into(avaImageView)
        }

        idUser?.let { saveAvaUserToFirestore(it) }
    }

    private fun saveAvaUserToFirestore(userId: String) {
        imageUri?.let { uri ->
            val fileName = "ava${UUID.randomUUID()}.png"
            val storageReference = fireStorage.reference.child("images/avatars/$fileName")
            val uploadTask = storageReference.putFile(uri)

            uploadTask.addOnSuccessListener {
                val imagePath = "images/avatars/$fileName"
                updateUserAvatar(userId, imagePath)
            }.addOnFailureListener {
//                Toast.makeText(this, "Image upload failed: ${it.message}", Toast.LENGTH_LONG).show()
            }
        } ?: run {
            updateUserAvatar(userId, currentImagePath!!)
        }
    }

    private fun updateUserAvatar(userId: String, imagePath: String) {
        val userInforUpdate = hashMapOf<String, Any>(
            "avatar" to imagePath
        )
        fireStore.collection("accounts").document(userId).update(userInforUpdate)
            .addOnSuccessListener {
//                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_LONG).show()
//                finish()
            }
            .addOnFailureListener { e ->
//                Toast.makeText(this, "Error updating product: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun events() {
        binding.btnLogout.setOnClickListener {
            signOut()
        }

        binding.layoutDeleteAccount.setOnClickListener {
            GlobalUtils.myStartActivity(requireContext(), DeleteAccountActivity::class.java)
        }
    }

    fun signOut(){
        val mAuth = FirebaseUtils.fireAuth
        val mySharedPreferences = MySharedPreferences.getInstance(requireContext())

//            Sign out
        mAuth.signOut()

//            Clear data in local DB
        GlobalUtils.resetMySharedPreferences(mySharedPreferences)

//            Start activity
        GlobalUtils.myStartActivityFinishAffinity(requireContext(), MainActivity::class.java)
    }

}
