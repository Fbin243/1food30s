package com.zebrand.app1food30s.ui.home

import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HomePresenter(
    private val view: HomeMVPView, private val db: AppDatabase
) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffect()
            val products =  FirebaseUtils.getListProducts()
            val categories = FirebaseUtils.getListCategories(db)
            val offers =  FirebaseUtils.getListOffers()

            view.hideShimmerEffect()
            view.showCategories(categories)
            view.showProductsLatestDishes(products, offers)
            view.showProductsBestSeller(products, offers)
            view.showOffers(offers)
        }
    }
}