package com.zebrand.app1food30s.ui.menu

import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MenuPresenter(private val view: MenuMVPView) {
    // Properties to hold the current products and offers
    var currentProducts: List<Product> = emptyList()
    var currentOffers: List<Offer> = emptyList()

    suspend fun getDataAndDisplay() {
        coroutineScope {
            val categoriesDeferred = async { FirebaseUtils.getListCategories() }
            val productsDeferred = async { FirebaseUtils.getListProducts() }
            val offersDeferred = async { FirebaseUtils.getListOffers() }

            // Await the results of the asynchronous operations
            val categories = categoriesDeferred.await()
            currentProducts = productsDeferred.await()
            currentOffers = offersDeferred.await()

            // Now use the fetched data to update the UI
            view.showCategories(categories)
            view.showProducts(currentProducts, currentOffers)
            view.handleChangeLayout(currentProducts, currentOffers)
        }
    }
}