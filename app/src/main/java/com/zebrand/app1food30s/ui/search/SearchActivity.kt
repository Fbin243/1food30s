package com.zebrand.app1food30s.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.ActivitySearchBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class SearchActivity : AppCompatActivity(), SearchMVPView {
    private lateinit var binding: ActivitySearchBinding
    private lateinit var searchPresenter: SearchPresenter
    private lateinit var db: AppDatabase
    private var isGrid: Boolean = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getInstance(this)
        linearLayoutManager = LinearLayoutManager(this)
        gridLayoutManager = GridLayoutManager(this, 2)
        searchPresenter = SearchPresenter(this, db)

        lifecycleScope.launch {
            searchPresenter.getDataAndDisplay()
            searchProductsByName("")
        }

        Utils.showShimmerEffect(binding.shimmerLayout, binding.textAvailableContent)
        Utils.hideShimmerEffect(binding.shimmerLayout, binding.textAvailableContent)
        handleCloseSearchScreen()

        binding.searchInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchProductsByName(s.toString())
            }
        })

        binding.clearSearchBtn.setOnClickListener {
            binding.searchInput.text?.clear()
        }
    }

    private fun searchProductsByName(pattern: String) {
        val searchResultNumber = searchPresenter.searchProductsByName(
            pattern,
            binding.productRcv.adapter as ProductAdapter
        )
        binding.textAvailableContent.text = "$searchResultNumber Items Available"
    }

    private fun handleCloseSearchScreen() {
        binding.backFromSearch.root.setOnClickListener {
            finish()
        }
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

    override fun showProducts(products: List<Product>, offers: List<Offer>) {
        Log.i("TAG123", "showProducts: TAO PRODUCT")
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
}