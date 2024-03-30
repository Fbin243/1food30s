package com.zebrand.app1food30s.utils

import android.util.Log
import com.google.firebase.firestore.toObject
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseService {
    suspend fun getListCategories(db: AppDatabase): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                var categories = db.categoryDao().getAll()
                if (categories.isEmpty()) {
                    val deferred = CompletableDeferred<List<Category>>()
                    FirebaseUtils.fireStore.collection("categories").addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        db.categoryDao().deleteAll()
                        value?.documents?.map { document ->
                            val category = document.toObject<Category>()!!
                            runBlocking {
                                category.image =
                                    FirebaseUtils.fireStorage.reference.child(category.image).downloadUrl.await()
                                        .toString()
                                db.categoryDao().insert(category)
                            }
                        }
                        categories = db.categoryDao().getAll()
                        deferred.complete(categories)
                    }
                    categories = deferred.await()
                }
                categories
            } catch (e: Exception) {
                Log.e("getListCategories", "Error getting products", e)
                emptyList()
            }
        }
    }

    suspend fun getListProducts(db: AppDatabase): List<Product> {
        return withContext(Dispatchers.IO) {
            try {
                var products = db.productDao().getAll()
                if (products.isEmpty()) {
                    val deferred = CompletableDeferred<List<Product>>()
                    FirebaseUtils.fireStore.collection("products").addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        db.productDao().deleteAll()
                        value?.documents?.map { document ->
                            val product = document.toObject<Product>()!!
                            runBlocking {
                                product.image =
                                    FirebaseUtils.fireStorage.reference.child(product.image).downloadUrl.await()
                                        .toString()
                                db.productDao().insert(product)
                            }
                        }
                        products = db.productDao().getAll()
                        deferred.complete(products)
                    }
                    products = deferred.await()
                }
                products
            } catch (e: Exception) {
                Log.e("getListProducts", "Error getting products", e)
                emptyList()
            }
        }
    }

    suspend fun getListOffers(db: AppDatabase): List<Offer> {
        return withContext(Dispatchers.IO) {
            try {
                var offers = db.offerDao().getAll()
                if (offers.isEmpty()) {
                    val deferred = CompletableDeferred<List<Offer>>()
                    FirebaseUtils.fireStore.collection("offers").addSnapshotListener { value, error ->
                        if (error != null) {
                            return@addSnapshotListener
                        }
                        db.offerDao().deleteAll()
                        value?.documents?.map { document ->
                            val offer = document.toObject<Offer>()!!
                            runBlocking {
                                offer.image =
                                    FirebaseUtils.fireStorage.reference.child(offer.image).downloadUrl.await()
                                        .toString()
                                db.offerDao().insert(offer)
                            }
                        }
                        offers = db.offerDao().getAll()
                        deferred.complete(offers)
                    }
                    offers = deferred.await()
                }
                offers
            } catch (e: Exception) {
                Log.e("getListOffers", "Error getting products", e)
                emptyList()
            }
        }
    }

    suspend fun getOneProductByID(idProduct: String): Product? {
        return withContext(Dispatchers.IO) {
            try {
                val querySnapshot =
                    FirebaseUtils.fireStore.collection("products").whereEqualTo("id", idProduct).get().await()
                val product = querySnapshot.toObjects(Product::class.java)[0]
                product.image =
                    FirebaseUtils.fireStorage.reference.child(product.image).downloadUrl.await().toString()
                product
            } catch (e: Exception) {
                Log.e("getOneProductByID", "Error getting products", e)
                null
            }
        }
    }
}