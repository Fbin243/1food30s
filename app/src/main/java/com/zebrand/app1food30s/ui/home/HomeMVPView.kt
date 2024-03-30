package com.zebrand.app1food30s.ui.home

import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product

interface HomeMVPView {
    fun showCategories(categories: List<Category>)
    fun showProductsLatestDishes(products: List<Product>, offers: List<Offer>)
    fun showProductsBestSeller(products: List<Product>, offers: List<Offer>)
    fun showOffers(offers: List<Offer>)
    fun showShimmerEffect()
    fun hideShimmerEffect()
}