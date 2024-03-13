package com.zebrand.app1food30s.ui.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import androidx.fragment.app.activityViewModels
import com.zebrand.app1food30s.ui.cart_checkout.SharedViewModel

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var rcv: RecyclerView
    private val sharedViewModel: SharedViewModel by activityViewModels()

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
        adapter.onAddButtonClick = { product ->
            // Log.d("CartItemAdapter", "Adding product to cart: ${product.name}")
            sharedViewModel.addToCart(product)
        }
        recyclerView.adapter = adapter
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
                id = "1",
                idCategory = "category1",
                idOffer = "offer1",
                name = "Sweet & Sour Chicken",
                image = "images/product1.png",
                price = 4.5,
                description = "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
                stock = 10,
                sold = 5,
                reviews = sampleReviews,
                date = sampleDate
            ),
            Product(
                id = "2",
                idCategory = "category1",
                idOffer = "offer1",
                name = "Burrito",
                image = "images/product1.png",
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