package com.zebrand.app1food30s.ui.product_detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.adapter.ReviewAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.databinding.ActivityProductDetailBinding
import com.zebrand.app1food30s.ui.review.ReviewActivity
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity(), ProductDetailMVPView,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var productDetailPresenter: ProductDetailPresenter
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getInstance(this)
        val userId = "QXLiLOiPLaHhY5gu7ZdS"
        // TODO
//        WishlistManager.initialize(userId)
        productDetailPresenter = ProductDetailPresenter(this, db)
        val idProduct = intent.getStringExtra("idProduct")
        Utils.initSwipeRefreshLayout(binding.swipeRefreshLayout, this, resources)
        lifecycleScope.launch {
            productDetailPresenter.getProductDetail(idProduct!!)
//            fetchWishlistAndUpdateUI()
//            productDetailPresenter.fetchRelatedProductsAndOffers(/* You need to provide idCategory and idProduct here */, { relatedProducts, offers ->
//                // Use the fetched relatedProducts and offers here
//                showRelatedProducts(relatedProducts, offers)
//            })
        }

        handleOpenReviewScreen()
        handleCloseDetailScreen()
    }

//    private suspend fun fetchWishlistAndUpdateUI() {
//        val wishlistItems = WishlistManager.fetchWishlistForCurrentUser()
//        // Now you have the latest wishlistItems, you can use them to mark products as wishlisted in the UI
//        // This step will depend on how you implement the display of related products
//        showRelatedProducts(productDetailPresenter.relatedProducts, productDetailPresenter.offers)
//    }

    override fun showProductDetail(product: Product, category: Category, offer: Offer?) {
        product.name.also { binding.productTitle.text = it }
        "| ${category.name}".also {
            var truncatedText = it
            if (it.length > 20) truncatedText = it.substring(0, 20) + "..."
            binding.productCategory.text = truncatedText
        }
        " | Sold: ${product.sold}".also {
            binding.productSold.text = it
        }
        product.description.also { binding.productDescription.text = it }
        "${product.stock}".also { binding.productStock.text = it }
//        Handle price with offer
        val oldPrice = product.price
        "$${Utils.formatPrice(oldPrice)}".also { binding.productPrice.text = it }
        if (offer != null) {
            val newPrice = product.price - offer.discountRate * product.price / 100
            "$${Utils.formatPrice(oldPrice)}".also { binding.productOldPrice.text = it }
            "$${Utils.formatPrice(newPrice)}".also { binding.productPrice.text = it }
            binding.productOldPrice.visibility = View.VISIBLE
        }
        Picasso.get().load(product.image).placeholder(Utils.getShimmerDrawable())
            .into(binding.productImage)
    }

    // TODO
    override fun showRelatedProducts(
        relatedProducts: MutableList<Product>,
        offers: MutableList<Offer>
    ) {
        binding.relatedProductRcv.layoutManager = GridLayoutManager(this, 2)
        val adapter = ProductAdapter(relatedProducts, offers, mutableSetOf())
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        binding.relatedProductRcv.adapter = adapter
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showShimmerEffects() {
        Utils.showShimmerEffect(binding.productShimmer, binding.cardView)
        Utils.showShimmerEffect(binding.relatedProductShimmer, binding.relatedProductRcv)
    }

    override fun hideShimmerEffects() {
        Utils.hideShimmerEffect(binding.productShimmer, binding.cardView)
        Utils.hideShimmerEffect(binding.relatedProductShimmer, binding.relatedProductRcv)
    }

    private fun handleCloseDetailScreen() {
        binding.backFromDetail.root.setOnClickListener {
            finish()
        }
    }

    private fun handleOpenReviewScreen() {
        binding.viewAllBtn.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            startActivity(intent)
        }
    }

    override fun showReviews(reviews: List<Review>) {
        binding.reviewRcv.layoutManager = LinearLayoutManager(this)
        binding.reviewRcv.adapter = ReviewAdapter(reviews)
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            productDetailPresenter.getProductDetail(intent.getStringExtra("idProduct")!!)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}