package com.zebrand.app1food30s.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.WishlistItem
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.search.SearchActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistMVPView
import com.zebrand.app1food30s.ui.wishlist.WishlistManager
import com.zebrand.app1food30s.ui.wishlist.WishlistPresenter
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils.hideShimmerEffectForRcv
import com.zebrand.app1food30s.utils.Utils.showShimmerEffectForRcv
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), HomeMVPView, WishlistMVPView, SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homePresenter: HomePresenter
    private lateinit var db: AppDatabase
    private lateinit var wishlistPresenter: WishlistPresenter
    private var currentProducts: List<Product> = emptyList()
    private var wishlistedProductIds: MutableSet<String> = mutableSetOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        homePresenter = HomePresenter(this, db)
        lifecycleScope.launch { homePresenter.getDataAndDisplay() }

        // Make function reloading data when swipe down
        binding.swipeRefreshLayout.setOnRefreshListener(this)
        binding.swipeRefreshLayout.setColorSchemeColors(resources.getColor(R.color.primary))


        val userId = "QXLiLOiPLaHhY5gu7ZdS"
        WishlistManager.initialize(userId)
        wishlistPresenter = WishlistPresenter(this)

        handleOpenSearchScreen()
        fetchAndUpdateWishlistState()
        return binding.root
    }

    private fun fetchAndUpdateWishlistState() {
        lifecycleScope.launch {
            try {
                val wishlistItems = WishlistManager.fetchWishlistForCurrentUser()
                wishlistedProductIds = wishlistItems.map { it.productId }.toSet() as MutableSet<String>
                updateAdaptersWithWishlistState()
            } catch (e: Exception) {
            }
        }
    }

    private fun updateAdaptersWithWishlistState() {
        (binding.productRcv1.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
        (binding.productRcv2.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

    // TODO
    override fun showWishlistItems(items: List<WishlistItem>) {
        // Here, you'd update your UI with the wishlist items.
        // This might involve updating a RecyclerView adapter or similar.
        // For example:
        // wishlistAdapter.submitList(items)
    }

    override fun showRemoveSuccessMessage() {
        Toast.makeText(context, "Product was removed from the wishlist", Toast.LENGTH_SHORT).show()
    }

    override fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    // Implementation of WishlistMVPView methods
    override fun showWishlistUpdated(product: Product, isAdded: Boolean) {
        // Update the set of wishlisted product IDs based on the action
        if (isAdded) {
            wishlistedProductIds.add(product.id)
        } else {
            wishlistedProductIds.remove(product.id)
        }

        // Notify all adapters about the update
        updateProductInAllAdapters(product.id, isAdded)
    }

    private fun updateProductInAllAdapters(productId: String, isWishlisted: Boolean) {
        val adapters = listOfNotNull(
            binding.productRcv1.adapter as? ProductAdapter,
            binding.productRcv2.adapter as? ProductAdapter
        )

        adapters.forEach { adapter ->
            val index = adapter.products.indexOfFirst { it.id == productId }
            if (index != -1) {
                // Update the wishlist status if your data model requires it
                // e.g., adapter.products[index].isWishlisted = isWishlisted

                adapter.notifyItemChanged(index)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAndUpdateWishlistState()
        binding.searchInput.clearFocus()
    }

    private fun handleOpenSearchScreen() {
        binding.searchInput.setOnFocusChangeListener { _, focus ->
            if (focus) {
                val intent = Intent(requireContext(), SearchActivity::class.java)
                startActivity(intent)
            }
        }
        binding.searchInput.setOnClickListener {
            val intent = Intent(requireContext(), SearchActivity::class.java)
            startActivity(intent)
        }
    }

    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.cateRcv.adapter = CategoryAdapter(categories)
    }

    private fun addProductToCart(context: Context, productId: String) {
        val db = FirebaseFirestore.getInstance()
        val preferences = MySharedPreferences.getInstance(context)
        val userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: ""

        val cartRef = db.collection("carts").document(userId)

        val productRef = db.collection("products").document(productId)
        productRef.get().addOnSuccessListener { productSnapshot ->
            val product = productSnapshot.toObject(Product::class.java)
            val stock = product?.stock ?: 0

            if (stock > 0) {
                cartRef.get().addOnSuccessListener { document ->
                    val cart = if (document.exists()) {
                        document.toObject(Cart::class.java)
                    } else {
                        // If the cart does not exist, create a new one
                        Cart(userId = db.document("accounts/$userId"), items = mutableListOf())
                    }
//                    Log.d("Test00", "addProductToCart: $cart")

                    cart?.let {
                        val existingItemIndex = it.items.indexOfFirst { item -> item.productId == productRef }
                        if (existingItemIndex >= 0) {
                            // Product exists, update quantity
                            it.items[existingItemIndex].quantity += 1
                        } else {
                            // New product, add to cart
                            it.items.add(CartItem(productRef, 1))
                        }

                        // Save updated cart back to Firestore
                        cartRef.set(it).addOnSuccessListener {
                            Toast.makeText(context, "Added to cart successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener { exception ->
                    Log.e("addProductToCart", "Error updating cart: ", exception)
                    Toast.makeText(context, "Failed to add to cart.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Product is out of stock.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("addProductToCart", "Error getting product: ", exception)
            Toast.makeText(context, "Failed to get product details.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun showProductsLatestDishes(products: List<Product>, offers: List<Offer>) {
        for (product in products) {
            product.isGrid = true
        }
        currentProducts = products
        binding.productRcv1.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = ProductAdapter(products.take(4), offers, wishlistedProductIds)
        addCallBacksForAdapter(adapter)
        binding.productRcv1.adapter = adapter
    }

    override fun showProductsBestSeller(products: List<Product>, offers: List<Offer>) {
        currentProducts = products
        binding.productRcv2.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ProductAdapter(products.take(6), offers, wishlistedProductIds)
        addCallBacksForAdapter(adapter)
        binding.productRcv2.adapter = adapter
    }

    private fun addCallBacksForAdapter(adapter: ProductAdapter) {
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        adapter.onAddButtonClick = { product ->
            addProductToCart(requireContext(), product.id)
        }
        adapter.onWishlistProductClick = { product ->
            wishlistPresenter.toggleWishlist(product)
        }
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(requireContext(), ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showOffers(offers: List<Offer>) {
        // Offer
        binding.offerRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        binding.offerRcv.adapter = OfferAdapter(offers)
    }

    override fun showShimmerEffect() {
        showShimmerEffectForRcv(binding.cateShimmer, binding.cateRcv)
        showShimmerEffectForRcv(binding.product1Shimmer, binding.productRcv1)
        showShimmerEffectForRcv(binding.product2Shimmer, binding.productRcv2)
        showShimmerEffectForRcv(binding.offerShimmer, binding.offerRcv)
    }

    override fun hideShimmerEffect() {
        hideShimmerEffectForRcv(binding.cateShimmer, binding.cateRcv)
        hideShimmerEffectForRcv(binding.product1Shimmer, binding.productRcv1)
        hideShimmerEffectForRcv(binding.product2Shimmer, binding.productRcv2)
        hideShimmerEffectForRcv(binding.offerShimmer, binding.offerRcv)
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            homePresenter.getDataAndDisplay()
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}