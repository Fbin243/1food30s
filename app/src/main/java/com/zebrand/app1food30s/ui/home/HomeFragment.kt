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
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)
        val view = binding.root
        handleDisplay()
        handleOpenSearchScreen()
        return view
    }

    override fun onResume() {
        super.onResume()
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

    private fun handleDisplay() {
        lifecycleScope.launch {
            binding.cateShimmer.startShimmer()
            binding.product1Shimmer.startShimmer()
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
            binding.productRcv1.adapter = adapter
            binding.product1Shimmer.stopShimmer()
            binding.product1Shimmer.visibility = View.GONE
            binding.productRcv1.visibility = View.VISIBLE

            // Offer
            binding.offerRcv.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            binding.offerRcv.adapter = OfferAdapter(offers.await())

            // Product linear
            binding.productRcv2.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = ProductAdapter(products.await(), false)
            adapter.onItemClick = { holder ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                startActivity(intent)
            }
            binding.productRcv2.adapter = adapter
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