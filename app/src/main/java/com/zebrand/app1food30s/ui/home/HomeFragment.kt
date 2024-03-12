package com.zebrand.app1food30s.ui.home

import android.content.Intent
import android.os.Bundle
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

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var rcv: RecyclerView

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

//        // Product linear
//        rcv = binding.productRcv2
//        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
//        var adapter = ProductAdapter(getListProducts(), false)
//        adapter.onItemClick = { holder ->
//            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
//            startActivity(intent)
//        }
//        rcv.adapter = adapter
//
//        // Product grid
//        rcv = binding.productRcv1
//        rcv.layoutManager = GridLayoutManager(requireContext(), 2)
//        adapter = ProductAdapter(getListProducts())
//        adapter.onItemClick = { holder ->
//            val intent = Intent(requireContext(), ProductDetailActivity::class.java)
//            startActivity(intent)
//        }
//        rcv.adapter = adapter

        // Setup the linear layout RecyclerView
        setupRecyclerView(binding.productRcv2, isGrid = false)

        // Setup the grid layout RecyclerView
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
            // Add more products as needed
        )
    }

    private fun getListOffers(): List<Offer> {
        var list = listOf<Offer>()
        list = list + Offer(R.drawable.offer1, 7)
        list = list + Offer(R.drawable.offer2, 5)
        return list
    }

}