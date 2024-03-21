package com.zebrand.app1food30s.ui.menu

import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product

interface MenuMVPView {
    fun showCategories(categories: List<Category>)
    fun showProducts(products: List<Product>, offers: List<Offer>)
    fun handleChangeLayout(products: List<Product>, offers: List<Offer>)
}