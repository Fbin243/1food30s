package com.zebrand.app1food30s.ui.home

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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.Cart
import com.zebrand.app1food30s.data.CartItem
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.app1food30s.ui.product_detail.ProductDetailActivity
import com.zebrand.app1food30s.ui.search.SearchActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val fireStore = FirebaseFirestore.getInstance()
    private val fireStorage = FirebaseStorage.getInstance()
    private lateinit var rcv: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
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
//        binding = null
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
        lifecycleScope.launch {
            binding.cateShimmer.startShimmer()
            binding.product1Shimmer.startShimmer()
            binding.offerShimmer.startShimmer()
            binding.cateShimmer.startShimmer()

            val products = async { getListProducts() }
            val categories = async { getListCategories() }
            val offers = async { getListOffers() }

            // Category
            binding.cateRcv.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
            binding.cateRcv.adapter = CategoryAdapter(categories.await())
            binding.cateShimmer.stopShimmer()
            binding.cateShimmer.visibility = View.GONE
            binding.cateRcv.visibility = View.VISIBLE

            // Product grid
            binding.productRcv1.layoutManager = GridLayoutManager(requireContext(), 2)
            var adapter = ProductAdapter(products.await())
            adapter.onItemClick = { holder ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                startActivity(intent)
            }
            // Add to cart
            adapter.onAddButtonClick = { product ->
                addProductToCart(product.id)
            }
            binding.productRcv1.adapter = adapter
            binding.product1Shimmer.stopShimmer()
            binding.product1Shimmer.visibility = View.GONE
            binding.productRcv1.visibility = View.VISIBLE

            // Offer
            binding.offerRcv.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            binding.offerRcv.adapter = OfferAdapter(offers.await())
            binding.offerShimmer.stopShimmer()
            binding.offerShimmer.visibility = View.GONE
            binding.offerRcv.visibility = View.VISIBLE


            // Product linear
            binding.productRcv2.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = ProductAdapter(products.await(), false)
            adapter.onItemClick = { holder ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                startActivity(intent)
            }
            // add to cart
            adapter.onAddButtonClick = { product ->
                addProductToCart(product.id)
            }
            binding.productRcv2.adapter = adapter
            binding.product2Shimmer.stopShimmer()
            binding.product2Shimmer.visibility = View.GONE
            binding.productRcv2.visibility = View.VISIBLE
        }

//        setupRecyclerView(binding.productRcv2, isGrid = false)
//        setupRecyclerView(binding.productRcv1, isGrid = true)
    }

    // private fun setupRecyclerView(recyclerView: RecyclerView, isGrid: Boolean) {
    //     val layoutManager = if (isGrid) {
    //         GridLayoutManager(requireContext(), 2)
    //     } else {
    //         LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
    //     }
    //     recyclerView.layoutManager = layoutManager

    //     val adapter = ProductAdapter(getListProducts(), isGrid)
    //     adapter.onItemClick = { product ->
    //         val intent = Intent(requireContext(), ProductDetailActivity::class.java)
    //         startActivity(intent)
    //     }

    //     // addProductToCart

    //     recyclerView.adapter = adapter
    // }

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

    private suspend fun getListCategories(): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("categories").get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val date = document.getDate("date")
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()

                    Category(
                        id,
                        name,
                        imageUrl,
                        0,
                        date
                    )
                }
            } catch (e: Exception) {
                Log.e("getListCategories", "Error getting products", e)
                emptyList()
            }
        }
    }

    private suspend fun getListProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("products").get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val idCategory = document.getDocumentReference("idCategory")
                    val idOffer = document.getDocumentReference("idOffer")
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val price = document.getDouble("price") ?: 0.0
                    val description = document.getString("description") ?: ""
                    val stock = document.getDouble("stock") ?: 0
                    val sold = document.getDouble("sold") ?: 0
                    val date = document.getDate("date")

                    Product(
                        id,
                        idCategory,
                        idOffer,
                        name,
                        imageUrl,
                        price,
                        description,
                        stock.toInt(),
                        sold.toInt(),
                        null,
                        date
                    )
                }.take(4)
            } catch (e: Exception) {
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
    }

    private suspend fun getListOffers(): List<Offer> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("offers").get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val price = document.getDouble("price") ?: 0.0
                    val numProduct = document.getDouble("numProduct") ?: 0
                    val date = document.getDate("date")

                    Offer(
                        id,
                        name,
                        imageUrl,
                        price,
                        numProduct.toInt(),
                        date
                    )
                }.take(2)
            } catch (e: Exception) {
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
    }
}