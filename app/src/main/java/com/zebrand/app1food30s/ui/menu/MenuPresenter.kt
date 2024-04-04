package com.zebrand.app1food30s.ui.menu

import android.util.Log
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.utils.FirebaseService
import kotlinx.coroutines.coroutineScope

class MenuPresenter(private val view: MenuMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay(calledFromActivity: Boolean = false) {
        coroutineScope {
            view.showShimmerEffectForProducts()
            val products = FirebaseService.getListProducts(db)
            val offers = FirebaseService.getListOffers(db)
            view.showProducts(products, offers)
            view.handleChangeLayout(products)
            if(!calledFromActivity) {
                view.hideShimmerEffectForProducts()
            }

            view.showShimmerEffectForCategories()
            val categories = FirebaseService.getListCategories(db)
            view.showCategories(categories)
            view.filterAndScrollToCategory(categories)
        }
    }

    fun reloadData(productAdapter: ProductAdapter, categoryAdapter: CategoryAdapter): List<Category> {
        val categories = db.categoryDao().getAll()
        filterProductByCategory(categories[0].id, productAdapter)
        view.showShimmerEffectForCategories()
        categoryAdapter.updateData(categories)
        categoryAdapter.updateInitialPosition(0)
        view.hideShimmerEffectForCategories()
        return categories
    }

    fun filterProductByCategory(idCategory: String, adapter: ProductAdapter) {
        try {
            val products = db.productDao().getByCategory("categories/${idCategory}")
            val offers = db.offerDao().getAll()
            view.showShimmerEffectForProducts()
            adapter.updateData(products, offers)
            view.handleChangeLayout(products)
            view.hideShimmerEffectForProducts()
        } catch (e: Exception) {
            Log.i("Error", "getRelatedProductsByCategory: ${e}")
        }
    }
}