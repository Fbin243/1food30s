package com.zebrand.app1food30s.ui.home

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class HomePresenter(
    private val view: HomeMVPView,
    private val fireStore: FirebaseFirestore,
    private val fireStorage: FirebaseStorage
) {

    suspend fun getDataAndDisplay() {
        coroutineScope {
            view.showShimmerEffect()
            val categoriesDeferred = async { getListCategories() }
            val productsDeferred = async { getListProducts() }
            val offersDeferred = async { getListOffers() }

            val categories = categoriesDeferred.await()
            val products = productsDeferred.await()
            val offers = offersDeferred.await()

            view.hideShimmerEffect()
            view.showCategories(categories)
            view.showProductsLatestDishes(products, offers)
            view.showProductsBestSeller(products, offers)
            view.showOffers(offers)
        }
    }

    suspend fun getListCategories(): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("categories").get().await()
                Log.i("TAG", "getListCategories: T đã gọi xong categories")
                querySnapshot.documents.map { document ->
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val date = document.getDate("date")
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()

                    Category(
                        id,
                        name,
                        imageUrl,
                        0,
                        date
                    )
                }
            } catch (e: Exception) {
                Log.e("getListCategories", "Error getting products", e)
                emptyList()
            }
        }
    }

    suspend fun getListProducts(): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("products").get().await()
                Log.i("TAG", "getListProducts: T đã gọi xong products")
                querySnapshot.documents.map { document ->
                    val id = document.getString("id") ?: ""
                    val idCategory = document.getDocumentReference("idCategory")
                    val idOffer = document.getDocumentReference("idOffer")
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val price = document.getDouble("price") ?: 0.0
                    val description = document.getString("description") ?: ""
                    val stock = document.getDouble("stock") ?: 0
                    val sold = document.getDouble("sold") ?: 0
                    val date = document.getDate("date")

                    Product(
                        id,
                        idCategory,
                        idOffer,
                        name,
                        imageUrl,
                        price,
                        description,
                        stock.toInt(),
                        sold.toInt(),
                        null,
                        date
                    )
                }.take(4)
            } catch (e: Exception) {
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
    }

    suspend fun getListOffers(): List<Offer> {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot = fireStore.collection("offers").get().await()
                Log.i("TAG", "getListOffers: T đã gọi xong offers")
                querySnapshot.documents.map { document ->
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val image = document.getString("image") ?: ""
                    val imageUrl = fireStorage.reference.child(image).downloadUrl.await().toString()
                    val discountRate = document.getDouble("discountRate") ?: 0.0
                    val numProduct = document.getDouble("numProduct") ?: 0
                    val date = document.getDate("date")

                    Offer(
                        id,
                        name,
                        imageUrl,
                        discountRate.toInt(),
                        numProduct.toInt(),
                        date
                    )
                }.take(2)
            } catch (e: Exception) {
                Log.e("getListOffers", "Error getting products", e)
                emptyList()
            }
        }
    }
}