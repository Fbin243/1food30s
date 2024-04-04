package com.zebrand.app1food30s.ui.product_view_all

import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseService
import kotlinx.coroutines.coroutineScope

class ProductViewAllPresenter(private val view: ProductViewAllMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay(isLatestDishes: Boolean) {
        coroutineScope {
            view.showShimmerEffectForProducts()
            val products = FirebaseService.getListProducts(db)
            val offers = FirebaseService.getListOffers(db)
            view.showProducts(products, offers)
            view.handleChangeLayout(products)
            view.hideShimmerEffectForProducts()
        }
    }
}