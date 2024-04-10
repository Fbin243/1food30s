package com.zebrand.app1food30s.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.lifecycleScope
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.databinding.ActivityMainBinding
import com.zebrand.app1food30s.ui.admin_stats.AdminStatsFragment
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import com.zebrand.app1food30s.ui.cart.CartFragment
import com.zebrand.app1food30s.ui.home.HomeFragment
import com.zebrand.app1food30s.ui.manage_order.ManageOrderFragment
import com.zebrand.app1food30s.ui.menu.MenuFragment
import com.zebrand.app1food30s.ui.offers.OffersFragment
import com.zebrand.app1food30s.ui.order_confirm.OrderConfirmationDialogFragment
import com.zebrand.app1food30s.ui.profile.ProfileAfterLoginFragment
import com.zebrand.app1food30s.ui.profile.ProfileFragment
import kotlinx.coroutines.launch
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.MySharedPreferences.Companion.defaultStringValue
import com.zebrand.app1food30s.utils.SingletonKey

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var adminLogin: Boolean = false
    private lateinit var db: AppDatabase
    private var idUser: String? = null
    private val mySharedPreferences = MySharedPreferences.getInstance(this)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)
//        idUser = intent.getStringExtra("USER_ID") ?: ""
        idUser = mySharedPreferences.getString(SingletonKey.KEY_USER_ID)
        adminLogin = mySharedPreferences.getBoolean(SingletonKey.IS_ADMIN)

//        Log.d("adminLogin", "adminLogin: $adminLogin")
        if (adminLogin) {
//            First screen for admin
            replaceFragment(AdminStatsFragment())
            handleBottomNavigationForAdmin()
        } else {
//            First screen for user
            replaceFragment(HomeFragment())
            handleBottomNavigation()
        }

        if (intent.getBooleanExtra("loadProfileFragment", false)) {
            replaceFragment(ProfileAfterLoginFragment())
        } else if (intent.getBooleanExtra("showOrderConfirmation", false)) {
            showOrderConfirmationToast()
            showOrderConfirmationDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("TAG123", "onDestroy: Xóa db")
        db.clearAllTables()
    }

//    override fun onStop() {
//        super.onStop()
//        Log.i("TAG123", "onDestroy: Xóa db")
//        db.clearAllTables()
//    }

    private fun showOrderConfirmationToast() {
        val toastView = findViewById<LinearLayout>(R.id.toast)

        toastView.visibility = View.VISIBLE

        // Optionally, hide after some duration
//        customSnackbarView.postDelayed({
//            customSnackbarView.visibility = View.GONE
//        }, duration)
    }

    private fun showOrderConfirmationDialog() {
        val dialog = OrderConfirmationDialogFragment()
        dialog.show(supportFragmentManager, "OrderConfirmationDialog")
    }


    private fun handleBottomNavigationForAdmin() {
        binding.bottomNavView.menu.clear()
        binding.icCart.visibility = View.GONE
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu_admin)
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            val isLogin = mySharedPreferences.getBoolean(SingletonKey.KEY_LOGGED)
            when (menuItem.itemId) {
                R.id.ic_dashboard -> replaceFragment(AdminStatsFragment())
                R.id.ic_order -> replaceFragment(ManageOrderFragment())
                R.id.ic_manage -> replaceFragment(ProfileFragment())
                R.id.ic_profile -> {
                    if (isLogin) {
                        val fragment = ProfileAfterLoginFragment().apply {
                            arguments = Bundle().apply {
                                putString("USER_ID", idUser)
                            }
                        }
                        replaceFragment(fragment)
                    } else {
                        replaceFragment(ProfileFragment())
                    }
                }
            }
            true
        }
    }

    private fun handleBottomNavigation() {
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            val isLogin = mySharedPreferences.getBoolean(SingletonKey.KEY_LOGGED)
            when(menuItem.itemId) {
                R.id.ic_home -> replaceFragment(HomeFragment())
                R.id.ic_menu -> replaceFragment(MenuFragment())
                R.id.ic_offers -> replaceFragment(OffersFragment())
                R.id.ic_profile -> {
                    if (isLogin) {
                        val fragment = ProfileAfterLoginFragment().apply {
                            arguments = Bundle().apply {
                                putString("USER_ID", idUser)
                            }
                        }
                        replaceFragment(fragment)
                    } else {
                        replaceFragment(ProfileFragment())
                    }
                }
//                R.id.ic_cart -> replaceFragment(CartFragment())
            }
            true
        }
        replaceFragment(HomeFragment())
        setupFloatingButton()
    }

    private fun setupFloatingButton() {
        binding.icCart.setOnClickListener {
            // Check if the user is logged in before proceeding
            val defaultId = defaultStringValue
            if (idUser == defaultId) {
                // User is not logged in, navigate to LoginActivity
                val loginIntent = Intent(this, LoginActivity::class.java)
                startActivity(loginIntent)
                return@setOnClickListener // Stop further execution of this function
            }

            replaceFragment(CartFragment())
            binding.bottomNavView.selectedItemId = R.id.placeholder
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment).commit()
    }
}
