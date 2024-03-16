package com.zebrand.app1food30s.ui.home

import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product

interface HomeMVPView {
    fun showCategories(categories: List<Category>)
    fun showProductsLatestDishes(products: List<Product>)
    fun showProductsBestSeller(products: List<Product>)
    fun showOffers(offers: List<Offer>)
    fun showShimmerEffect()
    fun hideShimmerEffect()
}