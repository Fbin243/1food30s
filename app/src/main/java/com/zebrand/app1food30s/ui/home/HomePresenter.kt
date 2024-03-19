package com.zebrand.app1food30s.ui.home

import com.zebrand.app1food30s.ui.base.DataPresenter
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class HomePresenter(
    private val view: HomeMVPView
) : DataPresenter() {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffect()
            val productsDeferred = async { getListProducts() }
            val categoriesDeferred = async { getListCategories() }
            val offersDeferred = async { getListOffers() }

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