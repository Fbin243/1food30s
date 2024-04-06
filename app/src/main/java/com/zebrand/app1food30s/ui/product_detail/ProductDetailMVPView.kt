package com.zebrand.app1food30s.ui.product_detail

import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.Review

interface ProductDetailMVPView {
    fun showProductDetail(product: Product, category: Category, offer: Offer?)

    fun showReviews(reviews: List<Review>)
    fun showRelatedProducts(relatedProducts: MutableList<Product>, offers: MutableList<Offer>)
    fun showShimmerEffects()
    fun hideShimmerEffects()
}