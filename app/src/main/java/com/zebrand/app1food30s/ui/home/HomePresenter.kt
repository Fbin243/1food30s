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
            view.showShimmerEffects()

            val productsLatest =  FirebaseService.getListProducts(db).toMutableList()
            val productsBestSeller =  FirebaseService.getListProducts(db).toMutableList()
            val categories = FirebaseService.getListCategories(db).toMutableList()
            val offers =  FirebaseService.getListOffers(db).toMutableList()

            view.hideShimmerEffects()

            view.showCategories(categories)
            view.showProductsLatestDishes(productsLatest, offers)
            view.showProductsBestSeller(productsBestSeller, offers)
            view.showOffers(offers)
        }
    }
}