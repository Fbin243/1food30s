package com.zebrand.app1food30s.ui.menu

import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseService
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.coroutineScope

class MenuPresenter(private val view: MenuMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            val categories = FirebaseService.getListCategories(db)
            val products = FirebaseService.getListProducts(db)
            val offers = FirebaseService.getListOffers(db)
            view.showCategories(categories)
            view.showProducts(products, offers)
            view.handleChangeLayout(products, offers)
        }
    }
}