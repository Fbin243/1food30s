package com.zebrand.app1food30s.ui.product_detail

import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product

interface ProductDetailMVPView {
    fun showProductDetail(product: Product, category: Category, offer: Offer?)
    fun showRelatedProducts(relatedProducts: List<Product>, offers: List<Offer>)
    fun showShimmerEffect()
    fun hideShimmerEffect()
}