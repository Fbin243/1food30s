package com.zebrand.app1food30s.ui.product_detail

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Picasso
import com.zebrand.app1food30s.R
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.adapter.ReviewAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.databinding.ActivityProductDetailBinding
import com.zebrand.app1food30s.ui.authentication.LoginActivity
import com.zebrand.app1food30s.ui.review.ReviewActivity
import com.zebrand.app1food30s.ui.wishlist.WishlistMVPView
import com.zebrand.app1food30s.ui.wishlist.WishlistManager
import com.zebrand.app1food30s.ui.wishlist.WishlistPresenter
import com.zebrand.app1food30s.ui.wishlist.WishlistRepository
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import com.zebrand.app1food30s.utils.Utils
import kotlinx.coroutines.launch
import kotlin.math.log

class ProductDetailActivity : AppCompatActivity(), ProductDetailMVPView, WishlistMVPView,
    SwipeRefreshLayout.OnRefreshListener {
    private lateinit var binding: ActivityProductDetailBinding
    private lateinit var productDetailPresenter: ProductDetailPresenter
    private lateinit var db: AppDatabase
    private lateinit var idProduct: String
    private lateinit var preferences: MySharedPreferences
    private lateinit var userId: String
    private lateinit var defaultUserId: String
    private var currentProduct: Product? = null
    private lateinit var wishlistPresenter: WishlistPresenter
    private var wishlistedProductIds: MutableSet<String> = mutableSetOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = AppDatabase.getInstance(this)

        preferences = MySharedPreferences.getInstance(this)
        userId = preferences.getString(SingletonKey.KEY_USER_ID) ?: "Default Value"
        defaultUserId = MySharedPreferences.defaultStringValue

        // TODO
        val wishlistRepository = WishlistRepository(userId)
        wishlistPresenter = WishlistPresenter(this, wishlistRepository)
        wishlistPresenter.fetchAndUpdateWishlistState {
            // Optional: Do something after wishlist state is updated, if necessary
        }

        productDetailPresenter = ProductDetailPresenter(this, db)
        idProduct = intent.getStringExtra("idProduct")!!
        Utils.initSwipeRefreshLayout(binding.swipeRefreshLayout, this, resources)
        lifecycleScope.launch {
            productDetailPresenter.getProductDetail(idProduct)

//            productDetailPresenter.fetchRelatedProductsAndOffers(/* You need to provide idCategory and idProduct here */, { relatedProducts, offers ->
//                // Use the fetched relatedProducts and offers here
//                showRelatedProducts(relatedProducts, offers)
//            })
        }

        handleOpenReviewScreen()
        handleCloseDetailScreen()
        handleAddToCart()
        handleMainProductWishlistClick()
    }

    private fun handleMainProductWishlistClick() {
        binding.ivWishlist.setOnClickListener {
            currentProduct?.let { product ->
                if (userId == defaultUserId) {
                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                } else {
                    wishlistPresenter.toggleWishlist(product)
                    // Optionally toggle the icon immediately (feedback)
                    updateWishlistIcon(product.id, !wishlistedProductIds.contains(product.id))
                }
            }
        }
    }

    private fun updateWishlistIcon(productId: String, isWishlisted: Boolean) {
        if (isWishlisted) {
            binding.ivWishlist.setImageResource(R.drawable.ic_wishlist_active)
            wishlistedProductIds.add(productId)

        } else {
            binding.ivWishlist.setImageResource(R.drawable.ic_wishlist)
            wishlistedProductIds.remove(productId)
        }
        currentProduct?.let { product ->
//            Log.d("Test00", "updateWishlistIcon: ")
            wishlistPresenter.toggleWishlistProductDetail(product)
        }
    }

    override fun refreshWishlistState(wishlistedProductIds: Set<String>) {
        this.wishlistedProductIds = wishlistedProductIds.toMutableSet()
        val thisWishlistedProductIds = this.wishlistedProductIds
//        Log.d("Test00", "refreshWishlistState: $thisWishlistedProductIds")
        // TODO
        (binding.relatedProductRcv.adapter as? ProductAdapter)?.updateWishlistState(wishlistedProductIds)
    }

    override fun updateWishlistItemStatus(product: Product, isAdded: Boolean) {
        // Update the set of wishlisted product IDs based on the action
        val productId = product.id
//        Log.d("Test00", "updateWishlistItemStatus: $productId")
        if (isAdded) {
//            Log.d("Test00", "updateWishlistItemStatus: added")
            wishlistedProductIds.add(productId)
        } else {
//            Log.d("Test00", "updateWishlistItemStatus: removed")
            wishlistedProductIds.remove(productId)
        }
        // updated correctly
//        Log.d("Test00", "updateWishlistItemStatus: $wishlistedProductIds")
        (binding.relatedProductRcv.adapter as? ProductAdapter)?.updateWishlistProductIds(
            wishlistedProductIds
        )

        // Notify all adapters about the update
        val adapters = listOfNotNull(
            binding.relatedProductRcv.adapter as? ProductAdapter,
        )
//        Log.d("Test00", "updateProductInAllAdapters: $adapters")
        adapters.forEach { adapter ->
            val index = adapter.products.indexOfFirst { it.id == productId }
            if (index != -1) {
//                Log.d("Test00", "updateProductInAllAdapters: notified")
                adapter.notifyItemChanged(index)
            }
        }
        // updateProductInAllAdapters(product.id, isAdded)
    }

    private fun handleAddToCart() {
        binding.addToCartBtn.setOnClickListener {
            currentProduct?.let { product ->
                Utils.addProductToCart(applicationContext, product.id, userId, defaultUserId)  // Using userId as both userId and defaultUserId for example
            }
        }
    }

//    private suspend fun fetchWishlistAndUpdateUI() {
//        val wishlistItems = WishlistManager.fetchWishlistForCurrentUser()
//        // Now you have the latest wishlistItems, you can use them to mark products as wishlisted in the UI
//        // This step will depend on how you implement the display of related products
//        showRelatedProducts(productDetailPresenter.relatedProducts, productDetailPresenter.offers)
//    }

    override fun showProductDetail(product: Product, category: Category, offer: Offer?) {
        currentProduct = product

        product.name.also { binding.productTitle.text = it }
        "| ${category.name}".also {
            var truncatedText = it
            if (it.length > 20) truncatedText = it.substring(0, 20) + "..."
            binding.productCategory.text = truncatedText
        }
        " | ${resources.getString(R.string.txt_sold)}: ${product.sold}".also {
            binding.productSold.text = it
        }
        product.description.also { binding.productDescription.text = it }
        "${product.stock}".also { binding.productStock.text = it }
//        Handle price with offer
        val oldPrice = product.price
        "${Utils.formatPrice(oldPrice, this)}".also { binding.productPrice.text = it }
        if (offer != null) {
            val newPrice = product.price - offer.discountRate * product.price / 100
            "${Utils.formatPrice(oldPrice, this)}".also { binding.productOldPrice.text = it }
            "${Utils.formatPrice(newPrice, this)}".also { binding.productPrice.text = it }
            binding.productOldPrice.visibility = View.VISIBLE
        }
        Picasso.get().load(product.image).placeholder(Utils.getShimmerDrawable())
            .into(binding.productImage)
        binding.productRating.text = "${Utils.formatRating(product.avgRating)} / 5"

        // Wishlist
        val productId = product.id
        val isProductWishlisted = productId in wishlistedProductIds
//        Log.d("Test00", "onBindViewHolder: $productId")
//        Log.d("Test00", "onBindViewHolder: $wishlistedProductIds")
//        Log.d("Test00", "onBindViewHolder: $isProductWishlisted")
        binding.ivWishlist.setImageResource(
            if (isProductWishlisted) R.drawable.ic_wishlist_active else R.drawable.ic_wishlist
        )
    }

    // TODO
    override fun showRelatedProducts(
        relatedProducts: MutableList<Product>,
        offers: MutableList<Offer>
    ) {
        for (product in relatedProducts) {
            product.isGrid = true
        }
        binding.relatedProductRcv.layoutManager = GridLayoutManager(this, 2)
        val adapter = ProductAdapter(relatedProducts, offers, wishlistedProductIds)
        adapter.onItemClick = { product ->
            openDetailProduct(product)
        }
        adapter.onWishlistProductClick = { product ->
            if (userId == defaultUserId) {
                val loginIntent = Intent(this, LoginActivity::class.java)
                loginIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                this.startActivity(loginIntent)
            } else {
                wishlistPresenter.toggleWishlist(product)
            }
        }
        binding.relatedProductRcv.adapter = adapter
        if(relatedProducts.isEmpty()) {
            binding.noRelatedProductText.visibility = View.VISIBLE
        }
    }

    private fun openDetailProduct(product: Product) {
        val intent = Intent(this, ProductDetailActivity::class.java)
        intent.putExtra("idProduct", product.id)
        startActivity(intent)
    }

    override fun showShimmerEffects() {
        Utils.showShimmerEffect(binding.productShimmer, binding.cardView)
        Utils.showShimmerEffect(binding.reviewTitleShimmer.root, binding.reviewTitle)
        Utils.showShimmerEffect(binding.reviewShimmer, binding.reviewRcv)
        Utils.showShimmerEffect(binding.viewAllBtnShimmer.root, binding.viewAllBtn.root)
        Utils.showShimmerEffect(binding.relatedProductShimmer, binding.relatedProductRcv)
    }

    override fun hideShimmerEffectForProduct() {
        Utils.hideShimmerEffect(binding.productShimmer, binding.cardView)
    }

    override fun hideShimmerEffectForReviews() {
        Utils.hideShimmerEffect(binding.reviewTitleShimmer.root, binding.reviewTitle)
        Utils.hideShimmerEffect(binding.reviewShimmer, binding.reviewRcv)
    }

    override fun hideShimmerEffectForRelatedProducts() {
        Utils.hideShimmerEffect(binding.relatedProductShimmer, binding.relatedProductRcv)
    }

    private fun handleCloseDetailScreen() {
        binding.backFromDetail.root.setOnClickListener {
            finish()
        }
    }

    private fun handleOpenReviewScreen() {
        binding.viewAllBtn.root.setOnClickListener {
            val intent = Intent(this, ReviewActivity::class.java)
            intent.putExtra("idProduct", idProduct)
            startActivity(intent)
        }
    }

    override fun showReviews(reviews: List<Review>) {
        binding.reviewRcv.layoutManager = LinearLayoutManager(this)
        binding.reviewRcv.adapter = ReviewAdapter(reviews.take(5))
        binding.reviewTitle.text = "${reviews.size} ${resources.getString(R.string.txt_reviews)}"
        if(reviews.isEmpty()) {
            binding.noReviewText.visibility = View.VISIBLE
        }
        Utils.hideShimmerEffect(binding.viewAllBtnShimmer.root, binding.viewAllBtn.root, reviews.isNotEmpty())
    }

    override fun onRefresh() {
        lifecycleScope.launch {
            productDetailPresenter.getProductDetail(intent.getStringExtra("idProduct")!!)
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }
}