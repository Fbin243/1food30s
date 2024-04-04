package com.zebrand.app1food30s.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.Cart
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.search.SearchActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistMVPView
import com.zebrand.app1food30s.ui.wishlist.WishlistPresenter
import com.zebrand.app1food30s.ui.wishlist.WishlistRepository
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBCartRef
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBProductRef
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), HomeMVPView, WishlistMVPView {
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

        // TODO
//        val userId = SingletonKey.KEY_USER_ID
//        Log.d("Test00", "onCreateView: $userId")
        val mySharedPreferences = context?.let { MySharedPreferences.getInstance(it) }
        val userId = mySharedPreferences?.getString(SingletonKey.KEY_USER_ID) ?: "Default Value"
        val wishlistRepository = WishlistRepository(userId)
        wishlistPresenter = WishlistPresenter(this, wishlistRepository)
        fetchAndUpdateWishlistState()

        handleOpenSearchScreen()

        return binding.root
    }

    override fun showProductsLatestDishes(products: MutableList<Product>, offers: MutableList<Offer>) {
        currentProducts = products
        binding.productRcv1.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = ProductAdapter(products.take(4).toMutableList(), offers, true, wishlistedProductIds)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        adapter.onAddButtonClick = { product ->
            addProductToCart(requireContext(), product.id)
        }
        adapter.onWishlistProductClick = { product ->
            wishlistPresenter.toggleWishlist(product)
        }
        binding.productRcv1.adapter = adapter
    }

    override fun showProductsBestSeller(products: MutableList<Product>, offers: MutableList<Offer>) {
        currentProducts = products
        binding.productRcv2.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ProductAdapter(products.take(4).toMutableList(), offers, false, wishlistedProductIds)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        adapter.onAddButtonClick = { product ->
            addProductToCart(requireContext(), product.id)
        }
        adapter.onWishlistProductClick = { product ->
            wishlistPresenter.toggleWishlist(product)
        }
        binding.productRcv2.adapter = adapter
    }

    private fun fetchAndUpdateWishlistState() {
        wishlistPresenter.fetchAndUpdateWishlistState()
    }

    override fun refreshWishlistState(wishlistedProductIds: Set<String>) {
        this.wishlistedProductIds = wishlistedProductIds.toMutableSet()
        updateAdaptersWithWishlistState() // Update your UI accordingly
    }

    private fun updateAdaptersWithWishlistState() {
        (binding.productRcv1.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
        (binding.productRcv2.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

//    override fun showRemoveSuccessMessage() {
//        Toast.makeText(context, "Product was removed from the wishlist", Toast.LENGTH_SHORT).show()
//    }
//
//    override fun showError(message: String) {
//        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
//    }

    // Implementation of WishlistMVPView methods
    override fun updateWishlistItemStatus(product: Product, isAdded: Boolean) {
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

        val cartRef = mDBCartRef.document(userId)

        val productRef = mDBProductRef.document(productId)
        productRef.get().addOnSuccessListener { productSnapshot ->
            val product = productSnapshot.toObject(Product::class.java)
            val stock = product?.stock ?: 0

            if (stock > 0) {
                cartRef.get().addOnSuccessListener { document ->
                    val cart = if (document.exists()) {
                        document.toObject(Cart::class.java)
                    } else {
                        Cart(userId = db.document("accounts/$userId"), items = mutableListOf())
                    }

                    cart?.let {
                        val existingItemIndex = it.items.indexOfFirst { item -> item.productId == productRef }
                        if (existingItemIndex >= 0) {
                            // Product exists, update quantity
                            it.items[existingItemIndex].quantity += 1
                        } else {
                            // New product, add to cart
                            // TODO!
                            it.items.add(CartItem(productRef, "", "", 0.0, "", 0, 1))
                        }

                        cartRef.set(it).addOnSuccessListener {
                            Toast.makeText(context, "Added to cart successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.addOnFailureListener { exception ->
//                    Log.e("addProductToCart", "Error updating cart: ", exception)
                    Toast.makeText(context, "Failed to add to cart.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Product is out of stock.", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
//            Log.e("addProductToCart", "Error getting product: ", exception)
            Toast.makeText(context, "Failed to get product details.", Toast.LENGTH_SHORT).show()
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
        binding.cateShimmer.startShimmer()
        binding.product1Shimmer.startShimmer()
        binding.offerShimmer.startShimmer()
        binding.product2Shimmer.startShimmer()
    }

    override fun hideShimmerEffect() {
        hideShimmerEffectForRcv(binding.cateShimmer, binding.cateRcv)
        hideShimmerEffectForRcv(binding.product1Shimmer, binding.productRcv1)
        hideShimmerEffectForRcv(binding.product2Shimmer, binding.productRcv2)
        hideShimmerEffectForRcv(binding.offerShimmer, binding.offerRcv)
    }

    private fun hideShimmerEffectForRcv(shimmer: ShimmerFrameLayout, recyclerView: RecyclerView) {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
    }
}