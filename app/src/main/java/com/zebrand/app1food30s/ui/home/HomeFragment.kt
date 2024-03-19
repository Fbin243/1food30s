package com.zebrand.app1food30s.ui.home

import android.content.Intent
import android.os.Bundle
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
import kotlinx.coroutines.launch

class HomeFragment : Fragment(), HomeMVPView {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var homePresenter: HomePresenter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        homePresenter = HomePresenter(this)
        lifecycleScope.launch { homePresenter.getDataAndDisplay() }

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

    override fun showCategories(categories: List<Category>) {
        binding.cateRcv.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false)
        binding.cateRcv.adapter = CategoryAdapter(categories)
    }

    override fun showProductsLatestDishes(products: List<Product>, offers: List<Offer>) {
        binding.productRcv1.layoutManager = GridLayoutManager(requireContext(), 2)
        val adapter = ProductAdapter(products, offers)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        binding.productRcv1.adapter = adapter
    }

    override fun showProductsBestSeller(products: List<Product>, offers: List<Offer>) {
        binding.productRcv2.layoutManager =
            LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        val adapter = ProductAdapter(products, offers, false)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
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