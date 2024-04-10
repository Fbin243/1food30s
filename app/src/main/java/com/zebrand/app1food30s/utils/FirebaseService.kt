package com.zebrand.app1food30s.utils

import android.util.Log
import com.google.firebase.firestore.toObject
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.Review
import com.zebrand.app1food30s.data.entity.User
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBWishlistRef
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object FirebaseService {
    private var firstTimeGetProducts: Boolean = true
    private var firstTimeGetCategories: Boolean = true
    private var firstTimeGetOffers: Boolean = true
    suspend fun getListCategories(db: AppDatabase): List<Category> {
        return withContext(Dispatchers.IO) {
            try {
                if(firstTimeGetCategories) {
                    db.categoryDao().deleteAll()
                    firstTimeGetCategories = false
                }
                var categories = db.categoryDao().getAll()
                if (categories.isEmpty()) {
                    val deferred = CompletableDeferred<List<Category>>()
                    FirebaseUtils.fireStore.collection("categories")
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                value?.documents?.map { document ->
                                    val category = document.toObject<Category>()!!
                                    category.image =
                                        FirebaseUtils.fireStorage.reference.child(category.image).downloadUrl.await()
                                            .toString()
                                    db.categoryDao().insert(category)
                                }
                                categories = db.categoryDao().getAll()
                                deferred.complete(categories)
                            }
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
                if(firstTimeGetProducts) {
                    db.productDao().deleteAll()
                    firstTimeGetProducts = false
                }
                var products = db.productDao().getAll()
                if (products.isEmpty()) {
                    val deferred = CompletableDeferred<List<Product>>()
                    FirebaseUtils.fireStore.collection("products")
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }
//                                Log.i("TAG123", "getListProducts: ${value?.documents}")
                            CoroutineScope(Dispatchers.IO).launch {
                                value?.documents?.map { document ->
                                    val product = document.toObject<Product>()!!

                                    product.image =
                                        FirebaseUtils.fireStorage.reference.child(product.image).downloadUrl.await()
                                            .toString()
                                    db.productDao().insert(product)

                                }
                                products = db.productDao().getAll()
                                deferred.complete(products)
                            }
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
                if(firstTimeGetOffers) {
                    db.offerDao().deleteAll()
                    firstTimeGetOffers = false
                }
                var offers = db.offerDao().getAll()
                if (offers.isEmpty()) {
                    val deferred = CompletableDeferred<List<Offer>>()
                    FirebaseUtils.fireStore.collection("offers")
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }
                            CoroutineScope(Dispatchers.IO).launch {
                                value?.documents?.map { document ->
                                    val offer = document.toObject<Offer>()!!
                                    offer.image =
                                        FirebaseUtils.fireStorage.reference.child(offer.image).downloadUrl.await()
                                            .toString()
                                    db.offerDao().insert(offer)
                                }
                                offers = db.offerDao().getAll()
                                deferred.complete(offers)
                            }

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

    suspend fun getOneProductByID(db: AppDatabase, idProduct: String): Product? {
        return withContext(Dispatchers.IO) {
            try {
                var product = db.productDao().getOneById(idProduct)
                if (product == null) {
                    val document =
                        FirebaseUtils.fireStore.collection("products").document(idProduct).get()
                            .await()
                    if(!document.exists()) {
                        return@withContext null
                    }
                    product = document.toObject<Product>()!!
                    product.image =
                        FirebaseUtils.fireStorage.reference.child(product.image).downloadUrl.await()
                            .toString()
                }
                product
            } catch (e: Exception) {
                Log.e("getOneProductByID", "Error getting products", e)
                null
            }
        }
    }

    suspend fun getUserWishlist(userId: String): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val document = FirebaseUtils.fireStore.collection("wishlists").document(userId).get().await()
                val productIdsRaw = document.get("productIds")

                // Safely attempt to cast to List<String>
                if (productIdsRaw is List<*>) {
                    return@withContext productIdsRaw.filterIsInstance<String>()
                } else {
                    // If the field is not a list, return an empty list
                    emptyList()
                }
            } catch (e: Exception) {
                Log.e("getUserWishlist", "Error getting wishlist for user $userId", e)
                emptyList()
            }
        }
    }

    suspend fun getListReviewsOfProduct(idProduct: String): List<Review> {
        return withContext(Dispatchers.IO) {
            try {
                val reviews = FireStoreUtils.mDBReviewRef.whereEqualTo("idProduct",
                    FireStoreUtils.mDBProductRef.document(idProduct)
                ).get().await().toObjects(Review::class.java)
                val reviewsList = reviews.map { review ->
                    val user = review.idAccount!!.get().await()?.toObject<User>()
                    review.avatar = FirebaseUtils.fireStorage.reference.child(user!!.avatar).downloadUrl.await().toString()
                    review.name = user.firstName + user.lastName
                    review
                }
                reviewsList
            } catch (e: Exception) {
                Log.e("getListReviewsOfProduct", "Error getting reviews of product $idProduct", e)
                emptyList()
            }
        }
    }
}