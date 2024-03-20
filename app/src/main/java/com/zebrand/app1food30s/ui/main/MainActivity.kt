package com.zebrand.app1food30s.ui.main

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.snackbar.Snackbar
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityMainBinding
import com.zebrand.app1food30s.ui.cart.CartFragment
import com.zebrand.app1food30s.ui.home.HomeFragment
import com.zebrand.app1food30s.ui.menu.MenuFragment
import com.zebrand.app1food30s.ui.offers.OffersFragment
import com.zebrand.app1food30s.ui.order_confirm.OrderConfirmationDialogFragment
import com.zebrand.app1food30s.ui.profile.ProfileAfterLoginFragment
import com.zebrand.app1food30s.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleBottomNavigation()

        if (intent.getBooleanExtra("loadProfileFragment", false)) {
            replaceFragment(ProfileAfterLoginFragment())
        }
        else if (intent.getBooleanExtra("showOrderConfirmation", false)) {
            showOrderConfirmationToast()
            showOrderConfirmationDialog()
        }
        else {
            // Your default fragment to load
            replaceFragment(HomeFragment())
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showOrderConfirmationToast() {
        val snackbar = Snackbar.make(binding.snackbarAnchor, "", Snackbar.LENGTH_INDEFINITE)
        val customView = layoutInflater.inflate(R.layout.snackbar_order_confirmation, null)

        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout

        // Remove the default text
        val textView = snackbarLayout.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.visibility = View.INVISIBLE

        // Add the custom layout
        snackbarLayout.addView(customView, 0)

        // Use anchor view to position Snackbar
        snackbar.anchorView = binding.bottomNavView

        // Access the Snackbar's layout params
        val layoutParams = snackbar.view.layoutParams as ViewGroup.MarginLayoutParams

        // Remove margins
        layoutParams.setMargins(0, 0, 0, 0)

        // Apply layout params
        snackbar.view.layoutParams = layoutParams

        // Force Snackbar to layout
        snackbar.view.requestLayout()

        snackbar.view.elevation = 0f

        snackbar.show()
    }

    private fun showOrderConfirmationDialog() {
        val dialog = OrderConfirmationDialogFragment()
        dialog.show(supportFragmentManager, "OrderConfirmationDialog")
    }

    private fun handleBottomNavigation() {
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.ic_home -> replaceFragment(HomeFragment())
                R.id.ic_menu -> replaceFragment(MenuFragment())
                R.id.ic_offers -> replaceFragment(OffersFragment())
                R.id.ic_profile -> replaceFragment(ProfileFragment())
//                R.id.ic_cart -> replaceFragment(CartFragment())
            }
            true
        }
        replaceFragment(HomeFragment())
        setupFloatingButton()
    }
    private fun setupFloatingButton() {
        binding.icCart.setOnClickListener {
            replaceFragment(CartFragment())
        }
    }
    private fun replaceFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment).commit()
    }
}
