package com.zebrand.app1food30s.ui.main

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
import com.zebrand.app1food30s.ui.cart.CartMVPFragment
import com.zebrand.app1food30s.ui.home.HomeFragment
import com.zebrand.app1food30s.ui.menu.MenuFragment
import com.zebrand.app1food30s.ui.offers.OffersFragment
import com.zebrand.app1food30s.ui.order_confirm.OrderConfirmationDialogFragment
import com.zebrand.app1food30s.ui.profile.ProfileAfterLoginFragment
import com.zebrand.app1food30s.ui.profile.ProfileFragment
import kotlinx.coroutines.launch
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var adminLogin: Boolean = false
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = AppDatabase.getInstance(this)

        if (adminLogin) {
            handleBottomNavigationForAdmin()
        } else {
            handleBottomNavigation()
        }

        if (intent.getBooleanExtra("loadProfileFragment", false)) {
            replaceFragment(ProfileAfterLoginFragment())
        } else if (intent.getBooleanExtra("showOrderConfirmation", false)) {
            showOrderConfirmationToast()
            showOrderConfirmationDialog()
        } else {
            // Your default fragment to load
            replaceFragment(HomeFragment())
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
            when (menuItem.itemId) {
//                R.id.ic_dashboard -> replaceFragment()
//                R.id.ic_order -> replaceFragment()
//                R.id.ic_manage -> replaceFragment()
                R.id.ic_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
//        replaceFragment()
    }

    private fun handleBottomNavigation() {
        binding.bottomNavView.menu.clear()
        binding.bottomNavView.inflateMenu(R.menu.bottom_nav_menu)
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            val mySharedPreferences = MySharedPreferences.getInstance(this)
            val isLogin = mySharedPreferences.getBoolean(SingletonKey.KEY_LOGGED)
            when(menuItem.itemId) {
                R.id.ic_home -> replaceFragment(HomeFragment())
                R.id.ic_menu -> replaceFragment(MenuFragment())
                R.id.ic_offers -> replaceFragment(OffersFragment())
                R.id.ic_profile -> {
                    if (isLogin) {
                        replaceFragment(ProfileAfterLoginFragment())
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
            replaceFragment(CartMVPFragment())
            binding.bottomNavView.selectedItemId = R.id.placeholder
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment).commit()
    }

}
