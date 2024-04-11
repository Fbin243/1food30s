package com.zebrand.app1food30s.ui.list_product

import android.util.Log
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.utils.FirebaseService

class ListProductPresenter(private val view: ListProductMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay() {
        view.showShimmerEffectForProducts()
        val products = FirebaseService.getListProducts(db)
        val offers = FirebaseService.getListOffers(db)
        view.showProducts(products, offers)
        view.handleChangeLayout(products)
        view.hideShimmerEffectForProducts()
    }

    fun refreshData(adapter: ProductAdapter) {
        view.showShimmerEffectForProducts()
        val products = db.productDao().getAll()
        val offers = db.offerDao().getAll()
        adapter.updateData(products, offers)
        view.handleChangeLayout(products)
        view.hideShimmerEffectForProducts()
    }

    fun refreshDataAndSortDataBySold(adapter: ProductAdapter) {
        view.showShimmerEffectForProducts()
        val products = db.productDao().getAll()
        val offers = db.offerDao().getAll()
        adapter.updateData(products.sortedByDescending { it.sold }, offers)
        view.handleChangeLayout(products)
        view.hideShimmerEffectForProducts()
    }

    fun filterProductsByOffer(idOffer: String, adapter: ProductAdapter) {
        val products = db.productDao().getByOffer("offers/${idOffer}")
        val offers = db.offerDao().getAll()
        view.showShimmerEffectForProducts()
        adapter.updateData(products, offers)
        view.handleChangeLayout(products)
        view.hideShimmerEffectForProducts()
    }

    fun searchProductsByName(name: String, adapter: ProductAdapter): Int {
        val products = db.productDao().searchByName(name)
        val offers = db.offerDao().getAll()
        adapter.updateData(products, offers)
        view.handleChangeLayout(products)
        return products.size
    }

    fun filterProductsByCategory(idCategory: String, adapter: ProductAdapter) {
        val products = db.productDao().getByCategory("categories/${idCategory}")
        val offers = db.offerDao().getAll()
        view.showShimmerEffectForProducts()
        adapter.updateData(products, offers)
        view.handleChangeLayout(products)
        view.hideShimmerEffectForProducts()
    }
}