package com.zebrand.app1food30s.ui.list_product

import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product

interface ListProductMVPView {
    fun showProducts(products: List<Product>, offers: List<Offer>)
    fun showShimmerEffectForProducts()
    fun hideShimmerEffectForProducts()
    fun handleChangeLayout(products: List<Product>)
}