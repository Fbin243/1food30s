package com.zebrand.app1food30s.ui.menu

import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product

interface MenuMVPView {
    fun showCategories(categories: List<Category>)
    fun showProducts(products: List<Product>, offers: List<Offer>)
    fun handleChangeLayout(products: List<Product>, offers: List<Offer>)
    fun showShimmerEffectForCategories()
    fun showShimmerEffectForProducts()
    fun hideShimmerEffectForCategories()
    fun hideShimmerEffectForProducts()
}