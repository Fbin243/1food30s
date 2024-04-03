package com.zebrand.app1food30s.ui.home

import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HomePresenter(
    private val view: HomeMVPView, private val db: AppDatabase
) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffect()

            val products =  FirebaseService.getListProducts(db)
            val products1 = FirebaseService.getListProducts(db)
            val categories = FirebaseService.getListCategories(db)
            val offers =  FirebaseService.getListOffers(db)

            view.hideShimmerEffect()

            view.showCategories(categories)
            view.showProductsLatestDishes(products, offers)
            view.showProductsBestSeller(products1, offers)
            view.showOffers(offers)
        }
    }
}