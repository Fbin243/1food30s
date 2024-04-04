package com.zebrand.app1food30s.ui.search

import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product

interface SearchMVPView {

    fun showProducts(products: List<Product>, offers: List<Offer>)
    fun handleChangeLayout(products: List<Product>)
}