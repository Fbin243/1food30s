package com.zebrand.app1food30s.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.data.Review
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.search.SearchActivity
import java.util.Date
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.Cart
import com.zebrand.app1food30s.data.CartItem

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var rcv: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        handleDisplay()
        handleOpenSearchScreen()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.searchInput.clearFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

    private fun handleDisplay() {
        // Category
        rcv = binding.cateRcv
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        rcv.adapter = CategoryAdapter(getListCategories())

        // Offer
        rcv = binding.offerRcv
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rcv.adapter = OfferAdapter(getListOffers())

        setupRecyclerView(binding.productRcv2, isGrid = false)
        setupRecyclerView(binding.productRcv1, isGrid = true)
    }

    private fun setupRecyclerView(recyclerView: RecyclerView, isGrid: Boolean) {
        val layoutManager = if (isGrid) {
            GridLayoutManager(requireContext(), 2)
        } else {
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        }
        recyclerView.layoutManager = layoutManager

        val adapter = ProductAdapter(getListProducts(), isGrid)
        adapter.onItemClick = { product ->
            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
            startActivity(intent)
        }

        // addProductToCart
        adapter.onAddButtonClick = { product ->
            addProductToCart(product.id)
        }

        recyclerView.adapter = adapter
    }

    fun addProductToCart(productId: String, cartId: String = "mdXn8lvirHaAogStOY1K") {
        val db = FirebaseFirestore.getInstance()
        val cartRef = db.collection("carts").document(cartId)
        val productRef = db.collection("products").document(productId) // Convert string ID to DocumentReference

        cartRef.get().addOnSuccessListener { document ->
            val cart = if (document.exists()) {
                document.toObject(Cart::class.java)
            } else {
                // If the cart does not exist, create a new one
                Cart(cartId, null)
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
                cartRef.set(it)
            }
        }.addOnFailureListener { exception ->
            Log.e("addProductToCart", "Error updating cart: ", exception)
        }
    }

    private fun getListCategories(): List<Category> {
        var list = listOf<Category>()
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Burgers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        list = list + Category(R.drawable.cate1, "Appetizers")
        return list
    }

    private fun getListProducts(): List<Product> {
        // Mock date for sample products
        val sampleDate = Date()

        // Mock reviews for sample products
        val sampleReviews = listOf(
            Review("account1", 5.0, "Great product!", sampleDate),
            Review("account2", 4.5, "Really enjoyed this.", sampleDate)
        )

        return listOf(
            Product(
                id = "tQHHzFF0MRcuetqzD6Zc",
                idCategory = null,
                idOffer = null,
                name = "Sweet & Sour Chicken",
                image = "images/product/product1.png",
                price = 4.5,
                description = "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
                stock = 10,
                sold = 5,
                reviews = sampleReviews,
                date = sampleDate
            ),
            Product(
                id = "GsWAFO80YukqOXm5Eybu",
                idCategory = null,
                idOffer = null,
                name = "Burrito",
                image = "images/product/product2.png",
                price = 6.5,
                description = "Burrito description",
                stock = 5,
                sold = 5,
                reviews = sampleReviews,
                date = sampleDate
            ),
        )
    }

    private fun getListOffers(): List<Offer> {
        var list = listOf<Offer>()
        list = list + Offer(R.drawable.offer1, 7)
        list = list + Offer(R.drawable.offer2, 5)
        return list
    }

}