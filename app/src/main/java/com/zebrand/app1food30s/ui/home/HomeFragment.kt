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
import com.zebrand.app1food30s.R
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private val fireStore = FirebaseFirestore.getInstance()

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
        // Category
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.cateRcv.adapter = CategoryAdapter(getListCategories())

        lifecycleScope.launch {
            // Product grid
            binding.productRcv1.layoutManager = GridLayoutManager(requireContext(), 2)
            var adapter = ProductAdapter(getListProducts())
            adapter.onItemClick = { holder ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                startActivity(intent)
            }
            binding.productRcv1.adapter = adapter

            // Offer
            binding.offerRcv.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            binding.offerRcv.adapter = OfferAdapter(getListOffers())

            // Product linear
            binding.productRcv2.layoutManager =
                LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = ProductAdapter(getListProducts(), false)
            adapter.onItemClick = { holder ->
                val intent = Intent(requireContext(), ProductDetailActivity::class.java)
                startActivity(intent)
            }
            binding.productRcv2.adapter = adapter
        }

    }

    private fun getListCategories(): List<Category> {
        var list = listOf<Category>()
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Burgers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
//        list = list + Category(R.drawable.cate1, "Appetizers")
        return list
    }

    private suspend fun getListProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("products").get().await()
                querySnapshot.documents.map { document ->
                    val id = document.id
                    val name = document.getString("name") ?: ""
                    val price = document.getDouble("price") ?: 0.0
                    val description = document.getString("description") ?: ""
                    val stock = document.getDouble("stock") ?: 0
                    val sold = document.getDouble("sold") ?: 0
                    val date = document.getDate("date")

                    Product(
                        "",
                        "",
                        "",
                        name,
                        R.drawable.product1,
                        price,
                        description,
                        stock.toInt(),
                        sold.toInt(),
                        null,
                        date
                    )
                }
            } catch (e: Exception) {
                // Xử lý khi có lỗi
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
    }

    private fun getListOffers(): List<Offer> {
        var list = listOf<Offer>()
        list = list + Offer(R.drawable.offer1, 7)
        list = list + Offer(R.drawable.offer2, 5)
        return list
    }

}