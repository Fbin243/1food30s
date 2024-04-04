package com.zebrand.app1food30s.ui.product_view_all

import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product

interface ProductViewAllMVPView {
    fun showProducts(products: List<Product>, offers: List<Offer>)
    fun handleChangeLayout(products: List<Product>)
    fun showShimmerEffectForProducts()
    fun hideShimmerEffectForProducts()
}