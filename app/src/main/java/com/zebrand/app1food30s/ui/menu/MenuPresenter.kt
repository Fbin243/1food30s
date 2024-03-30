package com.zebrand.app1food30s.ui.menu

import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.coroutineScope

class MenuPresenter(private val view: MenuMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            val categories = FirebaseUtils.getListCategories(db)
            view.showCategories(categories)
//            val products = async { FirebaseUtils.getListProducts() }.await()
//            val offers = async { FirebaseUtils.getListOffers() }.await()
        }
    }
}