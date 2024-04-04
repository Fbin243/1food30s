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
import com.zebrand.app1food30s.ui.wishlist.WishlistMVPView
import com.zebrand.app1food30s.ui.wishlist.WishlistPresenter
import com.zebrand.app1food30s.ui.wishlist.WishlistRepository
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import kotlinx.coroutines.launch

class MenuFragment : Fragment(), MenuMVPView, WishlistMVPView {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var menuPresenter: MenuPresenter
    private lateinit var wishlistPresenter: WishlistPresenter
    private lateinit var db: AppDatabase
    private var isGrid: Boolean = false
    private var wishlistedProductIds: MutableSet<String> = mutableSetOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater)

        val mySharedPreferences = context?.let { MySharedPreferences.getInstance(it) }
        val userId = mySharedPreferences?.getString(SingletonKey.KEY_USER_ID) ?: "Default Value"
        val wishlistRepository = WishlistRepository(userId)
        wishlistPresenter = WishlistPresenter(this, wishlistRepository)

        db = AppDatabase.getInstance(requireContext())
        menuPresenter = MenuPresenter(this, db)
        lifecycleScope.launch {
            menuPresenter.getDataAndDisplay()
            fetchAndUpdateWishlistState()
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Initialize your adapter here but don't set data yet
        setupInitialAdapter()

        lifecycleScope.launch {
            fetchAndUpdateWishlistState()
            // After this completes, now set up or update your adapter with the products and wishlistedProductIds
        }
    }

    private fun setupInitialAdapter() {
        // Assuming isGrid and offers are known at this point, or use placeholders
        val initialProducts: MutableList<Product> = mutableListOf()
        val initialOffers: MutableList<Offer> = mutableListOf()
        val initialWishlistedIds: Set<String> = emptySet() // Placeholder for wishlisted product IDs

        binding.productRcv.layoutManager = if (isGrid) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext())
        }

        // Initialize adapter with placeholders/empty lists
        binding.productRcv.adapter = ProductAdapter(initialProducts, initialOffers, isGrid, initialWishlistedIds)
    }

    private fun updateAdapterWithData(products: List<Product>, offers: List<Offer>) {
        // Here, wishlistedProductIds should already be updated from fetchAndUpdateWishlistState
        val adapter = binding.productRcv.adapter as? ProductAdapter
        adapter?.updateData(products, offers, wishlistedProductIds)
    }

    override fun updateWishlistItemStatus(product: Product, isAdded: Boolean) {
        // Update the set of wishlisted product IDs based on the action
        if (isAdded) {
            wishlistedProductIds.add(product.id)
        } else {
            wishlistedProductIds.remove(product.id)
        }

        (binding.productRcv.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

    private fun fetchAndUpdateWishlistState() {
        lifecycleScope.launch {
            wishlistPresenter.fetchAndUpdateWishlistState()
        }
    }

    override fun refreshWishlistState(wishlistedProductIds: Set<String>) {
        this.wishlistedProductIds = wishlistedProductIds as MutableSet<String>
//        Log.d("Test00", "refreshWishlistState: $wishlistedProductIds")
        updateAdapterWithWishlistState()
    }

    private fun updateAdapterWithWishlistState() {
        (binding.productRcv.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

    override fun handleChangeLayout(products: MutableList<Product>, offers: MutableList<Offer>) {
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

    // adapter
    override fun showProducts(products: MutableList<Product>, offers: MutableList<Offer>) {
        binding.productRcv.layoutManager = LinearLayoutManager(requireContext())
        binding.productRcv.adapter = generateAdapterWithLayout(products, offers)
    }

    private fun generateAdapterWithLayout(
        products: MutableList<Product>,
        offers: MutableList<Offer>
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