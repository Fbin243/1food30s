package com.zebrand.app1food30s.ui.wishlist

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.databinding.ActivityWishlistBinding
import com.zebrand.app1food30s.ui.list_product.ListProductFragment
import com.zebrand.app1food30s.utils.Utils

class WishlistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWishlistBinding
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        db = AppDatabase.getInstance(this)
        setContentView(binding.root)

        Utils.replaceFragment(ListProductFragment(hasBackBtn = true, hasTitle = true, hasLoading = true), supportFragmentManager, R.id.fragment_container)
        supportFragmentManager.registerFragmentLifecycleCallbacks(
            object : FragmentManager.FragmentLifecycleCallbacks() {
                override fun onFragmentViewCreated(
                    fm: FragmentManager,
                    f: Fragment,
                    v: View,
                    savedInstanceState: Bundle?
                ) {
                    val title = intent.getStringExtra("title")!!
                    val filterBy = intent.getStringExtra("filterBy")!!
                    if (f is ListProductFragment) {
                        f.setInfo(title, filterBy, null)
                        f.initProductViewAll()
                    }
                }
            }, false
        )
    }
}
