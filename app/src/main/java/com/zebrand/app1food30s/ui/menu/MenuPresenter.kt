package com.zebrand.app1food30s.ui.menu

import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MenuPresenter(private val view: MenuMVPView) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            val categories = async { FirebaseUtils.getListCategories() }.await()
            val products = async { FirebaseUtils.getListProducts() }.await()
            val offers = async { FirebaseUtils.getListOffers() }.await()
            view.showCategories(categories)
            view.showProducts(products, offers)
            view.handleChangeLayout(products, offers)
        }
    }
}