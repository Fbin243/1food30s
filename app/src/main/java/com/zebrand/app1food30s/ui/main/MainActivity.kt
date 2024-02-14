package com.zebrand.app1food30s.ui.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.databinding.ActivityMainBinding
import com.zebrand.app1food30s.ui.home.HomeFragment
import com.zebrand.app1food30s.ui.menu.MenuFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavView.setOnItemSelectedListener { menuItem ->
            when(menuItem.itemId) {
                R.id.ic_home -> replaceFragment(HomeFragment())
                R.id.ic_menu -> replaceFragment(MenuFragment())
                R.id.ic_offers -> replaceFragment(HomeFragment())
                R.id.ic_profile -> replaceFragment(HomeFragment())
            }
            true
        }
        replaceFragment(HomeFragment())
    }

//    private fun replaceFragment(fragment: Fragment) {
//        supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
//    }

    private fun replaceFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment).commit()
    }
}