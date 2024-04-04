package com.zebrand.app1food30s.ui.menu

import android.util.Log
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseService
import kotlinx.coroutines.coroutineScope

class MenuPresenter(private val view: MenuMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffectForCategories()
            val categories = FirebaseService.getListCategories(db)
            view.hideShimmerEffectForCategories()
            view.showCategories(categories)

            view.showShimmerEffectForProducts()
            FirebaseService.getListProducts(db)
            FirebaseService.getListOffers(db)
            filterProductByCategory(categories[0].id)
            view.hideShimmerEffectForProducts()
        }
    }

    private fun filterProductByCategory(idCategory: String) {
        try {
            val products = db.productDao().getByCategory("categories/${idCategory}").toMutableList()
            val offers = db.offerDao().getAll().toMutableList()
            view.showProducts(products, offers)
            view.handleChangeLayout(products, offers)
        } catch (e: Exception) {
            Log.i("Error", "getRelatedProductsByCategory: ${e}")
        }
    }
}