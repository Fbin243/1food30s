package com.zebrand.app1food30s.ui.home

import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import javax.security.auth.callback.Callback

interface HomeMVPView {
    fun showCategories(categories: List<Category>)
    fun showProductsLatestDishes(products: MutableList<Product>, offers: MutableList<Offer>)
    fun showProductsBestSeller(products: MutableList<Product>, offers: MutableList<Offer>)
    fun showOffers(offers: List<Offer>)
    fun showShimmerEffects()
    fun hideShimmerEffects()
    fun fetchAndUpdateWishlistState(callback: () -> Unit)
}