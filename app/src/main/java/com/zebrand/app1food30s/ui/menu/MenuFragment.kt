package com.zebrand.app1food30s.ui.menu

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.databinding.FragmentMenuBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistManager
import com.zebrand.app1food30s.utils.Utils.hideShimmerEffectForRcv
import com.zebrand.app1food30s.utils.Utils.showShimmerEffectForRcv
import kotlinx.coroutines.launch

class MenuFragment : Fragment(), MenuMVPView, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var menuPresenter: MenuPresenter
    private lateinit var db: AppDatabase
    private var isGrid: Boolean = false
    private var wishlistedProductIds: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        WishlistManager.initialize(userId = "QXLiLOiPLaHhY5gu7ZdS")
        menuPresenter = MenuPresenter(this, db)
        lifecycleScope.launch {
            menuPresenter.getDataAndDisplay()
            fetchAndUpdateWishlistState()
        }
        // Make function reloading data when swipe down
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.primary))

        return binding.root
    }

    private fun fetchAndUpdateWishlistState() {
        lifecycleScope.launch {
            try {
                val wishlistItems = WishlistManager.fetchWishlistForCurrentUser()
                wishlistedProductIds = wishlistItems.map { it.productId }.toSet()
                updateAdapterWithWishlistState()
            } catch (e: Exception) {
                // Handle errors appropriately
            }
        }
    }

    private fun updateAdapterWithWishlistState() {
        (binding.productRcv.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

    override fun handleChangeLayout(products: List<Product>, offers: List<Offer>) {
        setTypeDisplay(products)
        binding.gridBtn.setOnClickListener {
            isGrid = true
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            binding.productRcv.layoutManager = GridLayoutManager(requireContext(), 2)
            changeLayout(products)
        }

        binding.linearBtn.setOnClickListener {
            isGrid = false
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            binding.productRcv.layoutManager = LinearLayoutManager(requireContext())
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

    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val adapter = CategoryAdapter(categories)
        menuPresenter.filterProductByCategory(
            categories[0].id,
            binding.productRcv.adapter as ProductAdapter
        )

        adapter.onItemClick = { holder ->
            adapter.lastItemClicked?.cateTitle?.setTextColor(resources.getColor(R.color.black))
            adapter.lastItemClicked?.cateUnderline?.setBackgroundResource(0)
            holder.cateUnderline.setBackgroundResource(R.drawable.category_underline)
            holder.cateTitle.setTextColor(resources.getColor(R.color.primary))
            // Update UI by category
            menuPresenter.filterProductByCategory(
                categories[holder.adapterPosition].id,
                binding.productRcv.adapter as ProductAdapter
            )
        }
        binding.cateRcv.adapter = adapter
    }

    override fun showProducts(products: List<Product>, offers: List<Offer>) {
        binding.productRcv.layoutManager = LinearLayoutManager(requireContext())
        val adapter = ProductAdapter(products, offers, wishlistedProductIds)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        binding.productRcv.adapter = adapter
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showShimmerEffectForCategories() {
        showShimmerEffectForRcv(binding.cateShimmer, binding.cateRcv)
    }

    override fun showShimmerEffectForProducts() {
        showShimmerEffectForRcv(binding.productShimmer, binding.productRcv)
    }

    override fun hideShimmerEffectForCategories() {
        hideShimmerEffectForRcv(binding.cateShimmer, binding.cateRcv)
    }

    override fun hideShimmerEffectForProducts() {
        hideShimmerEffectForRcv(binding.productShimmer, binding.productRcv)
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            menuPresenter.getDataAndDisplay()
            fetchAndUpdateWishlistState()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}