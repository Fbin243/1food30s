package com.zebrand.app1food30s.ui.home

import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HomePresenter(
    private val view: HomeMVPView
) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffect()
            val productsDeferred = async { FirebaseUtils.getListProducts() }
            val categoriesDeferred = async { FirebaseUtils.getListCategories() }
            val offersDeferred = async { FirebaseUtils.getListOffers() }

            val categories = categoriesDeferred.await()
            val products = productsDeferred.await()
            val offers = offersDeferred.await()

            view.hideShimmerEffect()
            view.showCategories(categories)
            view.showProductsLatestDishes(products, offers)
            view.showProductsBestSeller(products, offers)
            view.showOffers(offers)
        }
    }
}