package com.zebrand.app1food30s.ui.product_view_all

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivityProductViewAllBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class ProductViewAllActivity : AppCompatActivity(), ProductViewAllMVPView {
    private lateinit var binding: ActivityProductViewAllBinding
    private lateinit var db: AppDatabase
    private var isGrid: Boolean = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager
    private lateinit var productViewAllPresenter: ProductViewAllPresenter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductViewAllBinding.inflate(layoutInflater)
        db = AppDatabase.getInstance(this)
        setContentView(binding.root)
        productViewAllPresenter = ProductViewAllPresenter(this, db)
        linearLayoutManager = LinearLayoutManager(this)
        gridLayoutManager = GridLayoutManager(this, 2)
        lifecycleScope.launch {
            productViewAllPresenter.getDataAndDisplay(intent.getBooleanExtra("isLatestDishes", false))
        }

        binding.textView.text = intent.getStringExtra("title")

        handleCloseViewAllScreen()
    }

    private fun handleCloseViewAllScreen() {
        binding.backFromViewAll.root.setOnClickListener {
            finish()
        }
    }

    override fun showProducts(products: List<Product>, offers: List<Offer>) {
        binding.productRcv.layoutManager = if (isGrid) gridLayoutManager else linearLayoutManager
        val adapter = ProductAdapter(products, offers, emptySet())
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        binding.productRcv.adapter = adapter
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun handleChangeLayout(products: List<Product>) {
        setTypeDisplay(products)
        binding.gridBtn.setOnClickListener {
            isGrid = true
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            binding.productRcv.layoutManager = gridLayoutManager
            changeLayout(products)
        }

        binding.linearBtn.setOnClickListener {
            isGrid = false
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            binding.productRcv.layoutManager = linearLayoutManager
            changeLayout(products)
        }

    }

    private fun changeLayout(products: List<Product>) {
        setTypeDisplay(products)
        binding.productRcv.adapter?.notifyDataSetChanged()
    }

    private fun setTypeDisplay(products: List<Product>) {
        for (product in products) {
            product.isGrid = isGrid
        }
    }

    override fun showShimmerEffectForProducts() {
        Utils.showShimmerEffect(binding.productShimmer, binding.productRcv)
    }

    override fun hideShimmerEffectForProducts() {
        Utils.hideShimmerEffect(binding.productShimmer, binding.productRcv)
    }
}