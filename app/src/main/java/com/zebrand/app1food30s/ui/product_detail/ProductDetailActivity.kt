package com.zebrand.app1food30s.ui.product_detail

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.databinding.ActivityProductDetailBinding
import com.zebrand.app1food30s.ui.review.ReviewActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistManager
import com.zebrand.app1food30s.utils.Utils.formatPrice
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity(), ProductDetailMVPView {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var productDetailPresenter: ProductDetailPresenter
    private lateinit var db: AppDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getInstance(this)
        val userId = "QXLiLOiPLaHhY5gu7ZdS"
        WishlistManager.initialize(userId)
        productDetailPresenter = ProductDetailPresenter(this, db)
        val idProduct = intent.getStringExtra("idProduct")

        lifecycleScope.launch {
            productDetailPresenter.getProductDetail(idProduct!!)
//            fetchWishlistAndUpdateUI()
//            productDetailPresenter.fetchRelatedProductsAndOffers(/* You need to provide idCategory and idProduct here */, { relatedProducts, offers ->
//                // Use the fetched relatedProducts and offers here
//                showRelatedProducts(relatedProducts, offers)
//            })
        }

        handleDisplayReview()
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
        "| ${category.name} | ".also { binding.productCategory.text = it }
        "Sold: ${product.sold}".also { binding.productSold.text = it }
        product.description.also { binding.productDescription.text = it }
        "${product.stock}".also { binding.productStock.text = it }
//        Handle price with offer
        val oldPrice = product.price
        "$${formatPrice(oldPrice)}".also { binding.productPrice.text = it }
        if (offer != null) {
            val newPrice = product.price - offer.discountRate * product.price / 100
            "$${formatPrice(oldPrice)}".also { binding.productOldPrice.text = it }
            "$${formatPrice(newPrice)}".also { binding.productPrice.text = it }
            binding.productOldPrice.visibility = View.VISIBLE
        }
        Picasso.get().load(product.image).into(binding.productImage)
    }

    // TODO
    override fun showRelatedProducts(relatedProducts: List<Product>, offers: List<Offer>) {
//        binding.relatedProductRcv.layoutManager = GridLayoutManager(this, 2)
//        val adapter = ProductAdapter(relatedProducts, offers, WishlistManager.wishlistedItems.map { it.productId }.toSet())
//        adapter.onItemClick = { product ->
//            openDetailProduct(product)
//        }
//        binding.relatedProductRcv.adapter = adapter
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showShimmerEffect() {
        binding.productShimmer.startShimmer()
        binding.relatedProductShimmer.startShimmer()
    }

    override fun hideShimmerEffect() {
        hideShimmerEffectForCardView(binding.productShimmer, binding.cardView)
        val constraintSet = ConstraintSet()
        val constraintLayout = findViewById<ConstraintLayout>(R.id.constraintLayout)
        constraintSet.clone(constraintLayout)
        constraintSet.connect(
            R.id.viewAllBtn,
            ConstraintSet.TOP,
            R.id.cardView,
            ConstraintSet.BOTTOM,
            20
        )
        constraintSet.applyTo(constraintLayout)
        hideShimmerEffectForRcv(binding.relatedProductShimmer, binding.relatedProductRcv)
    }

    private fun hideShimmerEffectForCardView(shimmer: ShimmerFrameLayout, cardView: CardView) {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        cardView.visibility = View.VISIBLE
    }

    private fun hideShimmerEffectForRcv(shimmer: ShimmerFrameLayout, recyclerView: RecyclerView) {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
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

    private fun handleDisplayReview() {
//        rcv = binding.reviewRcv
//        rcv.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        // TODO
//        rcv.adapter = ReviewAdapter(getListReviews())
    }

    private fun getListReviews(): List<Review> {
        var list = listOf<Review>()
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
//        list = list + Review(R.drawable.ava1, "", 5, "test", "")
        return list
    }
}