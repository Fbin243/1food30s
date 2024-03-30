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
import com.facebook.shimmer.ShimmerFrameLayout
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.Cart
import com.zebrand.app1food30s.data.CartItem
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.search.SearchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), HomeMVPView {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homePresenter: HomePresenter
    private lateinit var db: AppDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        db = AppDatabase.getInstance(requireContext())
        homePresenter = HomePresenter(this, db)
        lifecycleScope.launch { homePresenter.getDataAndDisplay() }

        handleOpenSearchScreen()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.searchInput.clearFocus()
    }

//    override fun onDestroyView() {
//        super.onDestroyView()
////        binding = null
//        lifecycleScope.launch(Dispatchers.IO) {
//            db.clearAllTables()
//        }
//    }

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

    private fun addProductToCart(context: Context, productId: String, cartId: String = "mdXn8lvirHaAogStOY1K") {
        val db = FirebaseFirestore.getInstance()
        val productRef = db.collection("products").document(productId)

        productRef.get().addOnSuccessListener { productSnapshot ->
            val product = productSnapshot.toObject(Product::class.java)
            val stock = product?.stock ?: 0

            if (stock > 0) {
                val cartRef = db.collection("carts").document(cartId)
                cartRef.get().addOnSuccessListener { document ->
                    val cart = if (document.exists()) {
                        document.toObject(Cart::class.java)
                    } else {
                        // If the cart does not exist, create a new one
                        Cart(id = cartId, accountId = null, items = mutableListOf())
                    }

                    cart?.let {
                        val existingItemIndex = it.items.indexOfFirst { item -> item.productId?.path == productRef.path }
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
        binding.productRcv1.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = ProductAdapter(products.take(4), offers)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        adapter.onAddButtonClick = { product ->
            addProductToCart(requireContext(), product.id)
        }
        binding.productRcv1.adapter = adapter
    }
    
    override fun showProductsBestSeller(products: List<Product>, offers: List<Offer>) {
        binding.productRcv2.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ProductAdapter(products.take(4), offers, false)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        adapter.onAddButtonClick = { product ->
            addProductToCart(requireContext(), product.id)
        }
        binding.productRcv2.adapter = adapter
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
        binding.cateShimmer.startShimmer()
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