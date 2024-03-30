package com.zebrand.app1food30s.ui.menu

import android.util.Log
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

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