package com.zebrand.app1food30s.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityMainBinding
import com.zebrand.app1food30s.ui.home.HomeFragment
import com.zebrand.app1food30s.ui.menu.MenuFragment
import com.zebrand.app1food30s.ui.offers.OffersFragment
import com.zebrand.app1food30s.ui.profile.ProfileAfterLoginFragment
import com.zebrand.app1food30s.ui.profile.ProfileFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleBottomNavigation()

        // Check if MainActivity should load ProfileAfterLoginFragment directly
        if (intent.getBooleanExtra("loadProfileFragment", false)) {
            replaceFragment(ProfileAfterLoginFragment())
        } else {
            // Your default fragment to load
            replaceFragment(HomeFragment())
        }
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
    }
    private fun replaceFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment).commit()
    }
}

// TODO: Code này thuộc branch của Hải 
// import android.content.Intent
// import androidx.appcompat.app.AppCompatActivity
// import android.os.Bundle
// import android.widget.Button
// import com.zebrand.app1food30s.R
// import com.zebrand.app1food30s.ui.admin_statistics.AdminStatisticsActivity

// class MainActivity : AppCompatActivity() {
//     override fun onCreate(savedInstanceState: Bundle?) {
//         super.onCreate(savedInstanceState)
//         setContentView(R.layout.activity_main)

//         // Assuming you have a button in your activity_main.xml with the ID 'btn_view_statistics'
//         val buttonViewStatistics: Button = findViewById(R.id.btn_view_statistics)
//         buttonViewStatistics.setOnClickListener {
//             // Intent to start AdminStatisticsActivity
//             val intent = Intent(this, AdminStatisticsActivity::class.java)
//             startActivity(intent)
//         }
//     }
// }
