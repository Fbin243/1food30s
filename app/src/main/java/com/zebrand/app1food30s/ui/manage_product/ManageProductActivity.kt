package com.zebrand.app1food30s.ui.manage_product

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ManageProductAdapter
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

        handleDisplayProductList()
    }

    private fun handleDisplayProductList() {
        rcv = binding.productRcv
        rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        val adapter = ManageProductAdapter(getListProducts(), false)
        rcv.adapter = adapter
    }

    private fun getListProducts(): List<Product> {
        return listOf(
            Product(
                R.drawable.sample_food,
                "Bread Burger Fusion",
                "Hamburger",
                20.5
            ),
            Product(
                R.drawable.sample_food,
                "Bread Burger Fusion",
                "Hamburger",
                20.5
            ),
            Product(
                R.drawable.sample_food,
                "Bread Burger Fusion",
                "Hamburger",
                20.5
            ),
            Product(
                R.drawable.sample_food,
                "Bread Burger Fusion",
                "Hamburger",
                20.5
            ),
            Product(
                R.drawable.sample_food,
                "Bread Burger Fusion",
                "Hamburger",
                20.5
            ),
            Product(
                R.drawable.sample_food,
                "Bread Burger Fusion",
                "Hamburger",
                20.5
            ),
            Product(
                R.drawable.sample_food,
                "Bread Burger Fusion",
                "Hamburger",
                20.5
            ),
            // Add more products as needed
        )
    }
}
