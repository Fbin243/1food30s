package com.zebrand.app1food30s.ui.authentication

import android.app.Dialog
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.EmailAuthProvider
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityDeleteAccountBinding
import com.zebrand.app1food30s.databinding.DialogDeleteAccountBinding
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FirebaseUtils
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import com.zebrand.app1food30s.utils.ValidateInput

class DeleteAccountActivity : AppCompatActivity() {
    lateinit var binding: ActivityDeleteAccountBinding
    private val mySharePreference = MySharedPreferences.getInstance(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDeleteAccountBinding.inflate(layoutInflater)
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
        binding.confirmBtn.setOnClickListener {
            if(checkValid()){
                val email = binding.tvEmail.text.toString()
//                Toast.makeText(this, email, Toast.LENGTH_LONG).show()
                showCustomConfirmDialogBox()
            }
        }

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

        bindingSub.title = "Delete account"
        bindingSub.content = resources.getString(R.string.txt_confirm_delete_account)

        acceptBtn.setOnClickListener {
            onClickDeleteAccount()
            dialog.dismiss()
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun onClickDeleteAccount() {
        val user = FirebaseUtils.fireAuth.currentUser

        val email = mySharePreference.getString(SingletonKey.KEY_EMAIL) ?: ""
        val password = mySharePreference.getString(SingletonKey.KEY_PASSWORD) ?: ""
        val credential = EmailAuthProvider.getCredential(email, password)

        // Prompt the user to re-provide their sign-in credentials
        user?.reauthenticate(credential)
            ?.addOnCompleteListener { reAuthTask ->
                if (reAuthTask.isSuccessful) {
                    // User has been re-authenticated, now delete the account
                    user.delete()
                        .addOnCompleteListener { deleteTask ->
                            if (deleteTask.isSuccessful) {
//                                Toast.makeText(this, "Delete account successfully", Toast.LENGTH_SHORT).show()
                                Utils.showCustomToast(this, "Delete account successfully", "success")

                                android.os.Handler(Looper.getMainLooper()).postDelayed({
                                    deleteAccountLogic()
                                }, 2000)
                            }
                        }
                }
            }
    }

    private fun deleteAccountLogic(){
        val mAuth = FirebaseUtils.fireAuth

//            Sign out
        mAuth.signOut()

//        Init account
        val mDBUser = FireStoreUtils.mDBUserRef
        val userId = mySharePreference.getString(SingletonKey.KEY_USER_ID)

//        Init cart
        val mDBCart = FireStoreUtils.mDBCartRef
        val query = mDBCart.whereEqualTo("accountId", userId).limit(1)

        if (userId != null) {
//            Delete account
            val userRef = mDBUser.document(userId)
            userRef.delete()
                .addOnSuccessListener {
                    Log.d("TAG", "Document successfully deleted!")
                }
                .addOnFailureListener { e ->
                    Log.w("TAG", "Error deleting document", e)
                }

//            Delete cart
            query.get().addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    document.reference.delete()
                        .addOnSuccessListener {
                            Log.d("TAG", "Document successfully deleted!")
                        }
                        .addOnFailureListener { e ->
                            Log.w("TAG", "Error deleting document", e)
                        }
                }
            }.addOnFailureListener { e ->
                Log.w("TAG", "Error getting documents", e)
            }
        } else {
            Log.d("TAG", "No user ID found in SharedPreferences")
        }

        //            Start activity
        GlobalUtils.myStartActivityFinishAffinity(this, MainActivity::class.java)

        //            Clear data in local DB
        GlobalUtils.resetMySharedPreferences(mySharePreference)
    }

    private fun checkValid(): Boolean {
        binding.emailContainer.error =
            ValidateInput.validEmailDelete(this, mySharePreference, binding.tvEmail)

        val validEmail = binding.emailContainer.error == null

        return validEmail
    }
}