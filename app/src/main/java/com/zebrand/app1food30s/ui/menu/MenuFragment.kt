package com.zebrand.app1food30s.ui.menu

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
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
import com.zebrand.app1food30s.ui.wishlist.WishlistPresenter
import com.zebrand.app1food30s.ui.wishlist.WishlistRepository
import kotlinx.coroutines.launch

class MenuFragment : Fragment(), MenuMVPView {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var menuPresenter: MenuPresenter
    private lateinit var wishlistRepository: WishlistRepository
    private lateinit var db: AppDatabase
    private var isGrid: Boolean = false
    private var wishlistedProductIds: Set<String> = emptySet()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        menuPresenter = MenuPresenter(this, db)
        lifecycleScope.launch {
            menuPresenter.getDataAndDisplay()
            // TODO
//            fetchAndUpdateWishlistState()
        }

        return binding.root
    }

//    private fun fetchAndUpdateWishlistState() {
//        lifecycleScope.launch {
//            try {
//                val wishlistItems = wishlistPresenter.fetchWishlistForCurrentUser()
//                wishlistedProductIds = wishlistItems.map { it.productId }.toSet()
//                updateAdapterWithWishlistState()
//            } catch (e: Exception) {
//                // Handle errors appropriately
//            }
//        }
//    }

    private fun updateAdapterWithWishlistState() {
        (binding.productRcv.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

    override fun handleChangeLayout(products: List<Product>, offers: List<Offer>) {
        // This assumes wishlistedProductIds are updated elsewhere and accessible here

        binding.gridBtn.setOnClickListener {
            isGrid = true
            binding.gridBtn.setImageResource(R.drawable.ic_active_grid)
            binding.linearBtn.setImageResource(R.drawable.ic_linear)
            binding.productRcv.layoutManager = GridLayoutManager(requireContext(), 2)
            binding.productRcv.adapter = generateAdapterWithLayout(products, offers)
        }

        binding.linearBtn.setOnClickListener {
            isGrid = false
            binding.linearBtn.setImageResource(R.drawable.ic_active_linear)
            binding.gridBtn.setImageResource(R.drawable.ic_grid)
            binding.productRcv.layoutManager = LinearLayoutManager(requireContext())
            binding.productRcv.adapter = generateAdapterWithLayout(products, offers)
        }
    }


    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val adapter = CategoryAdapter(categories)
        val primaryColor = Color.parseColor("#7F9839")
        adapter.onItemClick = { holder ->
            adapter.lastItemClicked?.cateTitle?.setTextColor(Color.parseColor("#FF3A3A4F"))
            adapter.lastItemClicked?.cateUnderline?.setBackgroundResource(0)
            holder.cateUnderline.setBackgroundResource(R.drawable.category_underline)
            holder.cateTitle.setTextColor(primaryColor)
        }
        binding.cateRcv.adapter = adapter
    }

    override fun showProducts(products: List<Product>, offers: List<Offer>) {
        binding.productRcv.layoutManager = LinearLayoutManager(requireContext())
        binding.productRcv.adapter = generateAdapterWithLayout(products, offers)
    }

    private fun generateAdapterWithLayout(
        products: List<Product>,
        offers: List<Offer>
    ): ProductAdapter {
        val adapter = ProductAdapter(products, offers, isGrid, wishlistedProductIds)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        return adapter
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showShimmerEffectForCategories() {
        binding.cateShimmer.startShimmer()
    }

    override fun showShimmerEffectForProducts() {
        binding.productShimmer.startShimmer()
    }

    override fun hideShimmerEffectForCategories() {
        hideShimmerEffectForRcv(binding.cateShimmer, binding.cateRcv)
    }

    override fun hideShimmerEffectForProducts() {
        hideShimmerEffectForRcv(binding.productShimmer, binding.productRcv)
    }

    private fun hideShimmerEffectForRcv(shimmer: ShimmerFrameLayout, recyclerView: RecyclerView) {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}