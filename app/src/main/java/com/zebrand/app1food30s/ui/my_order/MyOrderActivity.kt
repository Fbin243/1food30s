package com.zebrand.app1food30s.ui.my_order

import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.databinding.ActivityMyOrderBinding
import com.zebrand.app1food30s.ui.my_order.adapter.MyOrderViewPagerAdapter
import com.zebrand.app1food30s.ui.my_order.my_order_details.MyOrderDetailsActivity
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey

class MyOrderActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyOrderBinding
//    Chưa login nên không có đi qua local db để lấy data được
    private lateinit var mySharePreference: MySharedPreferences
    private lateinit var viewPager2Adapter: MyOrderViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyOrderBinding.inflate(layoutInflater)

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

    private fun init(){
        mySharePreference = MySharedPreferences.getInstance(this)

        viewPager2Adapter = MyOrderViewPagerAdapter(supportFragmentManager, lifecycle)
        binding.viewPager2.adapter = viewPager2Adapter // Set adapter here
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
            when (position) {
                0 -> tab.text = "Active"
                1 -> tab.text = "Previous"
            }
        }.attach()
    }

    private fun events(){
        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }


}