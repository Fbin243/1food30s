package com.zebrand.app1food30s.ui.profile

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.databinding.FragmentProfileAfterLoginBinding
import com.zebrand.app1food30s.ui.authentication.DeleteAccountActivity
import com.zebrand.app1food30s.ui.change_language.ChangeLanguageActivity
import com.zebrand.app1food30s.ui.change_password.ChangePasswordActivity
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.ui.my_order.MyOrderActivity
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FirebaseUtils
import com.zebrand.app1food30s.utils.FirebaseUtils.fireStorage
import com.zebrand.app1food30s.utils.FirebaseUtils.fireStore
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID


class ProfileAfterLoginFragment : Fragment() {
    private lateinit var binding: FragmentProfileAfterLoginBinding
    private var idUser: String? = null
    private lateinit var avaImageView: ImageView
    private var imageUri: Uri? = null
    private var currentImagePath: String? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    // ResultLauncher for handling image picking result
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                Picasso.get().load(it).placeholder(Utils.getShimmerDrawable()).into(binding.ava)
                idUser?.let { userId ->
                    saveAvaUserToFirestore(userId)
                }
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileAfterLoginBinding.inflate(inflater, container, false)
        idUser = arguments?.getString("USER_ID")
//        Log.d("MainActivity", "idUserProfileAfterLoginFragment: $idUser")

        Utils.showShimmerEffect(binding.usernameShimmer.root, binding.username)
        Utils.showShimmerEffect(binding.emailShimmer.root, binding.email)
        fetchUserInformation(idUser.orEmpty())
        events()

        binding.ava.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        // Set up the click listener for the layoutMyOrders
        binding.layoutMyOrders.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, MyOrderActivity::class.java)
            startActivity(intent)
        }
        // ================================================================
//        binding.username.setText(idUser)
        binding.layoutChangeLanguage.setOnClickListener {
            // Navigate to AdminStatisticsActivity
            val intent = Intent(activity, ChangeLanguageActivity::class.java).apply {
//                putExtra("currentLanguage", idUser)
            }
            startActivity(intent)
        }

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        return binding.root
    }


    override fun onResume() {
        super.onResume()
        Log.i("TAG123", "onResume: goi ham fetch lai $idUser")
        idUser?.let {
            fetchUserInformation(it, true)
        }
    }

    private fun saveAvaUserToFirestore(userId: String) {
        imageUri?.let { uri ->
            val fileName = "ava${UUID.randomUUID()}.png"
            val storageReference = fireStorage.reference.child("images/avatars/$fileName")
            storageReference.putFile(uri).addOnSuccessListener {
                val imagePath = "images/avatars/$fileName"
                updateUserAvatar(userId, imagePath)
            }.addOnFailureListener { exception ->
                Toast.makeText(
                    context,
                    "Image upload failed: ${exception.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun updateUserAvatar(userId: String, imagePath: String) {
        fireStore.collection("accounts").document(userId).update("avatar", imagePath)
            .addOnSuccessListener {
                Snackbar.make(binding.root, "Profile updated successfully", Snackbar.LENGTH_LONG)
                    .show()
            }
            .addOnFailureListener { e ->
                Snackbar.make(
                    binding.root,
                    "Error updating profile: ${e.message}",
                    Snackbar.LENGTH_LONG
                ).show()
            }
    }

    private fun fetchUserInformation(userId: String, fetchWithoutImage: Boolean = false) {
        lifecycleScope.launch {
            try {
                val documentSnapshot = FireStoreUtils.mDBUserRef.document(userId).get().await()
                val user = documentSnapshot.toObject(User::class.java)
                user?.let {
                    val ava = binding.ava
                    // Check if user's avatar is not null and not empty
                    if(!fetchWithoutImage) {
                        if (it.avatar.isNotEmpty()) {
                            // Fetch and set the user's avatar with resizing
                            val imageUrl =
                                fireStorage.reference.child(it.avatar).downloadUrl.await().toString()
                            Picasso.get().load(imageUrl).placeholder(Utils.getShimmerDrawable())
                                .into(ava)
                        } else {
                            // Load a default image when avatar is null or empty, also with resizing
                            Picasso.get().load(R.drawable.default_avatar)
                                .placeholder(Utils.getShimmerDrawable())// Adjust cropping to maintain aspect ratio
                                .into(ava)
                        }
                    }
                    binding.username.text = it.firstName
                    binding.email.text = it.email
                    Utils.hideShimmerEffect(binding.usernameShimmer.root, binding.username)
                    Utils.hideShimmerEffect(binding.emailShimmer.root, binding.email)
                    // Update other user info views as necessary
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error fetching user information", Toast.LENGTH_LONG).show()
            }
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

    private fun signOut() {
        val mAuth = FirebaseUtils.fireAuth
        val mySharedPreferences = MySharedPreferences.getInstance(requireContext())

//            Sign out
        mAuth.signOut()

        mGoogleSignInClient.signOut()

//            Clear data in local DB
        GlobalUtils.resetMySharedPreferences(mySharedPreferences)

//            Start activity
        GlobalUtils.myStartActivityFinishAffinity(requireContext(), MainActivity::class.java)
    }

}
