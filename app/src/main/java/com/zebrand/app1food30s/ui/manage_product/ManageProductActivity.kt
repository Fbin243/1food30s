package com.zebrand.app1food30s.ui.manage_product

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductApapter
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.ActivityManageProductBinding

class ManageProductActivity : AppCompatActivity() {
    private lateinit var binding: ActivityManageProductBinding
    private lateinit var rcv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handleChangeLayout()
        handleDisplayProductList()
    }

    private fun handleDisplayProductList() {
        rcv = binding.productRcv
        rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = ProductApapter(getListProducts(), false)
        rcv.adapter = adapter
    }

    private fun handleChangeLayout() {
        binding.gridBtn.setOnClickListener {
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            rcv.layoutManager = GridLayoutManager(this, 2)
            rcv.adapter = ProductApapter(getListProducts())
        }

        binding.linearBtn.setOnClickListener {
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            rcv.adapter = ProductApapter(getListProducts(), false)
        }
    }

    private fun getListProducts(): List<Product> {
        return listOf(
            Product(
                R.drawable.product1,
                "Sweet & Sour Chicken",
                "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
                4.5
            ),
            Product(
                R.drawable.product1,
                "Sweet & Sour Chicken",
                "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
                4.5
            ),
            Product(
                R.drawable.product1,
                "Sweet & Sour Chicken",
                "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
                4.5
            ),
            Product(
                R.drawable.product1,
                "Sweet & Sour Chicken",
                "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
                4.5
            ),
            Product(
                R.drawable.product1,
                "Sweet & Sour Chicken",
                "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
                4.5
            ),
            // Add more products as needed
        )
    }
}
