package com.zebrand.app1food30s.ui.main

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityMainBinding
import com.zebrand.app1food30s.ui.cart_checkout.CartFragment
import com.zebrand.app1food30s.ui.home.HomeFragment
import com.zebrand.app1food30s.ui.menu.MenuFragment
import com.zebrand.app1food30s.ui.offers.OffersFragment
import com.zebrand.app1food30s.ui.profile.ProfileAfterLoginFragment
import com.zebrand.app1food30s.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var adminLogin: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(adminLogin) {
            handleBottomNavigationForAdmin()
        } else handleBottomNavigation()

        // Check if MainActivity should load ProfileAfterLoginFragment directly
        if (intent.getBooleanExtra("loadProfileFragment", false)) {
            replaceFragment(ProfileAfterLoginFragment())
        } else {
            // Your default fragment to load
            replaceFragment(HomeFragment())
        }
    }

    private fun handleBottomNavigationForAdmin() {
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
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
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.ic_home -> replaceFragment(HomeFragment())
                R.id.ic_menu -> replaceFragment(MenuFragment())
                R.id.ic_offers -> replaceFragment(OffersFragment())
                R.id.ic_profile -> replaceFragment(ProfileFragment())
            }
            true
        }
        replaceFragment(HomeFragment())
        setupFloatingButton()
    }
    private fun setupFloatingButton() {
        binding.icCart.setOnClickListener {
            replaceFragment(CartFragment())
            binding.bottomNavView.selectedItemId = R.id.placeholder
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment).commit()
    }
}