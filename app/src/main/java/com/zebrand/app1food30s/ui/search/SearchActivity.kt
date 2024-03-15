package com.zebrand.app1food30s.ui.search

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.ActivitySearchBinding

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var rcv: RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        handleDisplayProductList()
        handleChangeLayout()
        handleCloseSearchScreen()
    }

    private fun handleCloseSearchScreen() {
        binding.backFromSearch.root.setOnClickListener {
            finish()
        }
    }
    private fun handleDisplayProductList() {
        rcv = binding.productRcv
        rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = ProductAdapter(getListProducts(), false)
        rcv.adapter = adapter
    }

    private fun handleChangeLayout() {
        binding.gridBtn.setOnClickListener {
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            rcv.layoutManager = GridLayoutManager(this, 2)
            rcv.adapter = ProductAdapter(getListProducts())
        }

        binding.linearBtn.setOnClickListener {
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            rcv.adapter = ProductAdapter(getListProducts(), false)
        }

    }

    private fun getListProducts(): List<Product> {
        var list = listOf<Product>()
//        list = list + Product(
//            R.drawable.product1,
//            "Sweet & Sour Chicken",
//            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
//            4.5
//        )
        return list
    }
}