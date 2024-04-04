package com.zebrand.app1food30s.ui.search

import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.utils.FirebaseService
import kotlinx.coroutines.coroutineScope

class SearchPresenter(private val view: SearchMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            val products = FirebaseService.getListProducts(db)
            val offers = FirebaseService.getListOffers(db)
            view.showProducts(products, offers)
            view.handleChangeLayout(products)
        }
    }

    fun searchProductsByName(productName: String, adapter: ProductAdapter): Int {
        val products = db.productDao().searchByName(productName)
        val offers = db.offerDao().getAll()
        adapter.updateData(products, offers)
        view.handleChangeLayout(products)
        return products.size
    }
}