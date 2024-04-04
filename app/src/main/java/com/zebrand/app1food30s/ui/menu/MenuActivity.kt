package com.zebrand.app1food30s.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.databinding.ActivityMenuBinding

class MenuActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMenuBinding
    private lateinit var db: AppDatabase
    private lateinit var categoryId: String
    private var adapterPosition: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        db = AppDatabase.getInstance(this)

        setContentView(binding.root)
        val menuFragment = MenuFragment(true)
        replaceFragment(menuFragment)
        categoryId = intent.getStringExtra("categoryId").toString()
        adapterPosition = intent.getIntExtra("adapterPosition", 0)

        Log.i("TAG123", "onCreate: $categoryId")
        // Listen for changes in the fragment lifecycle
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment,
                    v: View,
                    savedInstanceState: Bundle?
                ) {
                    if (f is MenuFragment) {
                        f.changeHeaderOfFragment()
                        f.saveCategoryIdAndAdapterPosition(categoryId, adapterPosition)
                    }
                }
            }, false
        )
    }

    private fun replaceFragment(fragment: Fragment) {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.fragment_container, fragment).commit()
    }
}

