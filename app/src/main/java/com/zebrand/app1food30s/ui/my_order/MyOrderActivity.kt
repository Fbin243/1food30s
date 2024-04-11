package com.zebrand.app1food30s.ui.my_order

import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.adapter.MyOrderAdapter
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.databinding.ActivityMyOrderBinding
import com.zebrand.app1food30s.ui.my_order.my_order_details.MyOrderDetailsActivity
import com.zebrand.app1food30s.utils.GlobalUtils
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey

class MyOrderActivity : AppCompatActivity(), MyOrderMVPView {
    lateinit var binding: ActivityMyOrderBinding
//    Chưa login nên không có đi qua local db để lấy data được
    private lateinit var mySharePreference: MySharedPreferences
    private lateinit var presenter: MyOrderPresenter
    private lateinit var myActiveOrderAdapter: MyOrderAdapter
    private var myActiveOrderList: MutableList<Order> = mutableListOf()
    private lateinit var myPrevOrderAdapter: MyOrderAdapter
    private var myPrevOrderList: MutableList<Order> = mutableListOf()

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

        getActiveMyOrderList()
        getPrevMyOrderList()
    }

    private fun init(){
        presenter = MyOrderPresenter(this)
        mySharePreference = MySharedPreferences.getInstance(this)
    }

    private fun events(){
        binding.backIcon.root.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun getActiveMyOrderList() {
        val userId = mySharePreference.getString(SingletonKey.KEY_USER_ID)!!
        Log.d("Test00", "getMyOrderList: ${userId}")

//        val userId = "8U49yTcDk55UW2UJO69h"
        myActiveOrderAdapter = MyOrderAdapter(myActiveOrderList)
        myActiveOrderAdapter.onItemClick = {
            GlobalUtils.myStartActivityWithString(this, MyOrderDetailsActivity::class.java, "idOrder", it.id)
        }

        // Set layout manager và adapter cho RecyclerView
        binding.rcvActiveMyOrder.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcvActiveMyOrder.adapter = myActiveOrderAdapter

        //getData
        presenter.getActiveOrderList(userId, myActiveOrderAdapter)
    }

    override fun getPrevMyOrderList() {
        val userId = mySharePreference.getString(SingletonKey.KEY_USER_ID)!!
//        val userId = "8U49yTcDk55UW2UJO69h"
        myPrevOrderAdapter = MyOrderAdapter(myPrevOrderList)
        myPrevOrderAdapter.onItemClick = {
            GlobalUtils.myStartActivityWithString(this, MyOrderDetailsActivity::class.java, "idOrder", it.id)
        }

        // Set layout manager và adapter cho RecyclerView
        binding.rcvPrevMyOrder.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.rcvPrevMyOrder.adapter = myPrevOrderAdapter

        //getData
        presenter.getPrevOrderList(userId, myPrevOrderAdapter)
    }
}