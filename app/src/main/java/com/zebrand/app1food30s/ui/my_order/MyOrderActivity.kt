package com.zebrand.app1food30s.ui.my_order

import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.databinding.ActivityMyOrderBinding

class MyOrderActivity : AppCompatActivity(), MyOrderMVPView {
    lateinit var binding: ActivityMyOrderBinding
//    private val mySharePreference = MySharedPreferences.getInstance(this)
    private lateinit var presenter: MyOrderPresenter
    private lateinit var myOrderAdapter: MyOrderAdapter
    private var myOrderList: List<Order> = mutableListOf()

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

        getMyOrderList()
    }

    private fun init(){
        presenter = MyOrderPresenter(this, this)
    }

    private fun events(){
//        binding.testOrderStatus.root.setOnClickListener {
//            val intent = Intent(this, MyOrderDetailsActivity::class.java)
//            startActivity(intent)
//        }
    }

    override fun getMyOrderList() {
//        val userId = mySharePreference.getString(SingletonKey.KEY_USER_ID) as String
        val userId = "8U49yTcDk55UW2UJO69h"
        myOrderAdapter = MyOrderAdapter(myOrderList)

        // Set layout manager và adapter cho RecyclerView
        binding.rcvMyOrder.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcvMyOrder.adapter = myOrderAdapter

        //getData
        presenter.getMyOrderList(userId, myOrderAdapter, myOrderList)
    }
}