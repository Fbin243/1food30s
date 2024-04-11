package com.zebrand.app1food30s.ui.menu

import android.util.Log
import com.zebrand.app1food30s.adapter.CategoryAdapter
import com.zebrand.app1food30s.adapter.ProductAdapter
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.ui.wishlist.WishlistRepository
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.utils.FirebaseService
import com.zebrand.app1food30s.utils.MySharedPreferences
import com.zebrand.app1food30s.utils.SingletonKey
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class MenuPresenter(private val view: MenuMVPView, private val db: AppDatabase) {
    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffectForCategories()
            val categories = FirebaseService.getListCategories(db)
            view.showCategories(categories)
            view.hideShimmerEffectForCategories()
        }
    }

    fun getAllCategories(): List<Category> {
        return db.categoryDao().getAll()
    }

    fun refreshData(adapter: CategoryAdapter) {
        view.showShimmerEffectForCategories()
        val categories = db.categoryDao().getAll()
        adapter.updateData(categories)
        view.hideShimmerEffectForCategories()
    }
}