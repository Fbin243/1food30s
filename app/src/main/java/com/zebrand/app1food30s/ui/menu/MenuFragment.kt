package com.zebrand.app1food30s.ui.menu

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.zebrand.app1food30s.ui.search.SearchActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistManager
import com.zebrand.app1food30s.utils.Utils
import com.zebrand.app1food30s.utils.Utils.hideShimmerEffect
import com.zebrand.app1food30s.utils.Utils.showShimmerEffect
import kotlinx.coroutines.launch

class MenuFragment(private var calledFromActivity: Boolean = false) : Fragment(), MenuMVPView,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentMenuBinding
    private lateinit var menuPresenter: MenuPresenter
    private lateinit var db: AppDatabase
    private var isGrid: Boolean = false
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var gridLayoutManager: GridLayoutManager
    private var wishlistedProductIds: Set<String> = emptySet()
    private var categoryId: String? = null
    private var adapterPosition: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMenuBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        linearLayoutManager = LinearLayoutManager(requireContext())
        gridLayoutManager = GridLayoutManager(requireContext(), 2)
        WishlistManager.initialize(userId = "QXLiLOiPLaHhY5gu7ZdS")
        menuPresenter = MenuPresenter(this, db)

        // Make function reloading data when swipe down
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.primary))

        lifecycleScope.launch {
            showShimmerEffect(binding.shimmerLayout, binding.textView)
            menuPresenter.getDataAndDisplay(calledFromActivity)
            hideShimmerEffect(binding.shimmerLayout, binding.textView)
            fetchAndUpdateWishlistState()
        }

        handleOpenSearchScreen()

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

    private fun handleOpenSearchScreen() {
        binding.searchButton.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }


    private fun updateAdapterWithWishlistState() {
        (binding.productRcv.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
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

    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        val adapter = CategoryAdapter(categories, true, adapterPosition)
        menuPresenter.filterProductByCategory(
            categories[0].id,
            binding.productRcv.adapter as ProductAdapter
        )
        adapter.onItemClick = { holder ->
            adapter.lastItemClicked?.cateTitle?.setTextColor(resources.getColor(R.color.black))
            adapter.lastItemClicked?.cateUnderline?.setBackgroundResource(0)
            holder.cateUnderline.setBackgroundResource(R.drawable.category_underline)
            holder.cateTitle.setTextColor(resources.getColor(R.color.primary))
            binding.textView.text = categories[holder.adapterPosition].name
            // Update UI by category
            menuPresenter.filterProductByCategory(
                categories[holder.adapterPosition].id,
                binding.productRcv.adapter as ProductAdapter
            )
        }
        binding.cateRcv.adapter = adapter
    }

    override fun showProducts(products: List<Product>, offers: List<Offer>) {
        Log.i("TAG123", "showProducts: TAO PRODUCT")
        binding.productRcv.layoutManager = if (isGrid) gridLayoutManager else linearLayoutManager
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
        showShimmerEffect(binding.cateShimmer, binding.cateRcv)
    }

    override fun showShimmerEffectForProducts() {
        showShimmerEffect(binding.productShimmer, binding.productRcv)
    }

    override fun hideShimmerEffectForCategories() {
        hideShimmerEffect(binding.cateShimmer, binding.cateRcv)
    }

    override fun hideShimmerEffectForProducts() {
        hideShimmerEffect(binding.productShimmer, binding.productRcv)
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            val categories = menuPresenter.reloadData(
                binding.productRcv.adapter as ProductAdapter,
                binding.cateRcv.adapter as CategoryAdapter
            )
            fetchAndUpdateWishlistState()
            filterAndScrollToCategory(categories)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    fun changeHeaderOfFragment() {
        binding.backFromMenu.visibility = View.VISIBLE
        binding.backFromMenu.setOnClickListener {
            Log.i("TAG123", "changeHeaderOfFragment: DA BAM NUT BACK")
            requireActivity().finish()
        }
    }

    fun saveCategoryIdAndAdapterPosition(categoryId: String, adapterPosition: Int) {
        this.categoryId = categoryId
        this.adapterPosition = adapterPosition
    }


    override fun filterAndScrollToCategory(categories: List<Category>) {
        if(categoryId == null) categoryId = categories[0].id
        menuPresenter.filterProductByCategory(
            categoryId!!,
            binding.productRcv.adapter as ProductAdapter)
        binding.cateRcv.scrollToPosition(adapterPosition)
        binding.textView.text = categories[adapterPosition].name
        hideShimmerEffectForCategories()
    }
}