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
import com.zebrand.app1food30s.adapter.ProductApapter
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.OfferAdapter
import com.zebrand.app1food30s.databinding.FragmentHomeBinding
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.ui.search.SearchActivity

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

        // Product grid
        rcv = binding.productRcv1
        rcv.layoutManager = GridLayoutManager(requireContext(), 2)
        rcv.adapter = ProductApapter(getListProducts())

        // Offer
        rcv = binding.offerRcv
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rcv.adapter = OfferAdapter(getListOffers())

        // Product linear
        rcv = binding.productRcv2
        rcv.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        rcv.adapter = ProductApapter(getListProducts(), false)
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
        var list = listOf<Product>()
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        list = list + Product(
            R.drawable.product1,
            "Sweet & Sour Chicken",
            "Sweet and sour chicken with crispy chicken, pineapple and delicious chilly sauce.",
            4.5
        )
        return list
    }

    private fun getListOffers(): List<Offer> {
        var list = listOf<Offer>()
        list = list + Offer(R.drawable.offer1, 7)
        list = list + Offer(R.drawable.offer2, 5)
        return list
    }

}