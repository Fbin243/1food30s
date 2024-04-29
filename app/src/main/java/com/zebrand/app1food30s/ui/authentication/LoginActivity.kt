package com.zebrand.app1food30s.ui.authentication

import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.databinding.ActivityLoginBinding
import com.zebrand.app1food30s.ui.main.MainActivity
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FirebaseUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import com.zebrand.app1food30s.utils.ValidateInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONException
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID


class LoginActivity : AppCompatActivity() {
    lateinit var binding: ActivityLoginBinding
    private val mySharePreference = MySharedPreferences.getInstance(this)
    private lateinit var callbackManager: CallbackManager

    private lateinit var queue: RequestQueue
    var orderId = ""
    val url = "https://api-m.sandbox.paypal.com/v2/checkout/orders/"
    val urlToken = "https://api-m.sandbox.paypal.com/v1/oauth2/token"
    var token = "Bearer "
    val clientId = "AXpqoGgnoXww1RmM2N15AKI7LV4es1uEB-kk0qO1X9OwdELkXnS18nTQ50Kdt9ERQQUoVOsGvOolFgWI"
    val clientSecret = "EO0MxmLuQfbA0Cq8PSNbDA6JHHims1JBMjG4gCLLmjMesgI0tpIdWYyIJMBjsPCuRzltRQdbQaWIAIc4"

    companion object {
        private const val RC_SIGN_IN = 9001
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
        init()
        events()
    }

    private fun init() {
        val rememberMe = mySharePreference.getBoolean(SingletonKey.KEY_REMEMBER_ME)
        val email = mySharePreference.getString(SingletonKey.KEY_EMAIL)
        val password = mySharePreference.getString(SingletonKey.KEY_PASSWORD)
        binding.rememberMe.isChecked = rememberMe
        if (rememberMe) {
            binding.tvEmail.setText(email)
            binding.tvPassword.setText(password)
        }
        // Facebook login
        handleLoginWithFacebook()
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

        binding.googleLoginBtn.setOnClickListener {
            onClickGoogleLogin()
        }

        binding.facebookLoginBtn.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("email", "public_profile"))
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

    private fun authorization(email: String, mySharePreference: MySharedPreferences) {
        CoroutineScope(Dispatchers.Main).launch {
            val userInfo = getUserInfo(email)
            userInfo?.let {
                Log.d("userInfo", "Da di qua")
                Log.d("userInfo", "Login " + userInfo.toString())
                mySharePreference.setString(SingletonKey.KEY_USER_ID, userInfo.id.toString())
                mySharePreference.setBoolean(SingletonKey.IS_ADMIN, userInfo.admin)
                myStartActivity(MainActivity::class.java, userInfo.id!!)
            }
        }
    }

    private suspend fun getUserInfo(email: String): User? = withContext(Dispatchers.IO) {
        val query = FireStoreUtils.mDBUserRef.whereEqualTo("email", email)
        return@withContext try {
            val querySnapshot = query.get().await()
            if (!querySnapshot.isEmpty) {
                val documentSnapshot = querySnapshot.documents[0]
                val userInfo = documentSnapshot.toObject(User::class.java)
                userInfo?.id = documentSnapshot.id
                return@withContext userInfo
            } else {
                return@withContext null
            }
        } catch (e: Exception) {
            Log.e("userInfo", "Error getting user info: ", e)
            return@withContext null
        }
    }

    private fun myStartActivity(cls: Class<*>, idUser: String) {
        val intent = Intent(this, cls).apply {
            putExtra("USER_ID", idUser)
        }
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

    private fun onClickLogin() {
        if (checkValid()) {
            val email = binding.tvEmail.text.toString().trim()
            val password = binding.tvPassword.text.toString().trim()

            val mAuth = FirebaseUtils.fireAuth
            mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Set KEY_LOGGED
                        setKeyShareRef(email, password)

                        // Authorization
                        Log.d("userInfo", "Da di qua 1 " + email)
                        authorization(email, mySharePreference)

                    } else {
                        Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun onClickGoogleLogin() {
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Facebook login
        callbackManager.onActivityResult(requestCode, resultCode, data)

        // Google login
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseUtils.fireAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = FirebaseUtils.fireAuth.currentUser
                    Log.i(
                        "TAG123",
                        "firebaseAuthWithGoogle: ${user?.displayName} ${user?.email} ${user?.photoUrl}"
                    )

                    if (user != null) {
                        handleLoggedInUser(user.displayName, user.email, user.photoUrl.toString())
                    } else {
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun handleLoginWithFacebook() {
        callbackManager = CallbackManager.Factory.create()
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    val request = GraphRequest.newMeRequest(
                        result.accessToken
                    ) { jsonObject, _ ->
                        jsonObject?.let {
                            try {
                                val name = it.getString("name")
                                val eMail = it.getString("email")
                                val fbUserID = it.getString("id")
                                val url = it.getJSONObject("picture").getJSONObject("data")
                                    .getString("url")

                                Log.i("TAG123", "onSuccess: $name $fbUserID $eMail $url")
                                handleLoggedInUser(name, eMail, url)
                            } catch (e: JSONException) {
                                e.printStackTrace()
                            } catch (e: NullPointerException) {
                                e.printStackTrace()
                            }
                        }
                    }

                    val parameters = Bundle()
                    parameters.putString(
                        "fields",
                        "id, name, email, gender, birthday, picture.type(large)"
                    )
                    request.parameters = parameters
                    request.executeAsync()
                }

                override fun onCancel() {
                    Log.v("LoginScreen", "---onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.v("LoginScreen", "----onError: ${error.message}")
                }
            })
    }

    private fun handleLoggedInUser(name: String?, email: String?, photoUrl: String?) {
        mySharePreference.setBoolean(SingletonKey.KEY_LOGGED, true)
        Toast.makeText(this, "Signed in as $name", Toast.LENGTH_SHORT)
            .show()
        FireStoreUtils.mDBUserRef.whereEqualTo("email", email).get()
            .addOnSuccessListener { queryDocumentSnapshots ->
                if (queryDocumentSnapshots.isEmpty) {
                    Log.d("TAG123", "User not found in Firestore")
                    val fileName = "ava${UUID.randomUUID()}.png"
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val imagePath = "images/avatars/$fileName"
                            uploadImageFromUrl(photoUrl.toString(), imagePath)
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Profile image uploaded successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            val newUser = User(
                                firstName = name?.split(" ", limit = 2)?.get(0) ?: "",
                                lastName = name?.split(" ", limit = 2)?.get(1) ?: "",
                                email = email ?: "",
                                admin = false,
                                avatar = imagePath
                            )

                            Utils.setUserDataInFireStore(newUser) {
                                authorization(email ?: "", mySharePreference)
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@LoginActivity,
                                    "Failed to upload profile image",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                } else {
                    Log.d("TAG123", "User found in Firestore")
                    authorization(email ?: "", mySharePreference)
                }
            }
    }

    private suspend fun uploadImageFromUrl(imageUrl: String, storagePath: String) {
        withContext(Dispatchers.IO) {
            val url = URL(imageUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()

            val input = connection.inputStream
            val bitmap = BitmapFactory.decodeStream(input)

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            val data = baos.toByteArray()

            val storageRef = FirebaseStorage.getInstance().reference.child(storagePath)
            storageRef.putBytes(data).await()
        }
    }
}