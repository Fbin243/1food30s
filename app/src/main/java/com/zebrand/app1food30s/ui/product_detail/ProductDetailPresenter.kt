package com.zebrand.app1food30s.ui.product_detail

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.toObject
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.FireStoreUtils
import com.zebrand.app1food30s.utils.FirebaseService
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class ProductDetailPresenter(
    private val view: ProductDetailMVPView, private val db: AppDatabase
) {
    suspend fun getProductDetail(idProduct: String): Boolean {
        try {
            view.showShimmerEffects()
            val product = FirebaseService.getOneProductByID(db, idProduct)
                ?: throw Exception("Product not found")
            // Get category
            val category = product.idCategory!!.get().await().toObject<Category>()
            val offer = db.offerDao().getOneById(product.idOffer?.path)
            // Get related products
            getRelatedProductsByCategory(product.idCategory!!, product.id)
            // Finish get data
            view.showProductDetail(product, category!!, offer)
            view.hideShimmerEffects()
            return true
        } catch (e: Exception) {
            Log.i("Error", "getProductDetail: $e")
            return false
        }
    }

    private suspend fun getRelatedProductsByCategory(
        idCategory: DocumentReference,
        idProduct: String
    ) {
        try {
            val querySnapshot =
                FirebaseUtils.fireStore.collection("products")
                    .whereEqualTo("idCategory", idCategory)
                    .whereNotEqualTo("id", idProduct).get()
                    .await()
            val relatedProducts = querySnapshot.toObjects(Product::class.java).map { product ->
                product.image =
                    FirebaseUtils.fireStorage.reference.child(product.image).downloadUrl.await()
                        .toString()
                product
            }

            Log.i("TAG", "getRelatedProductsByCategory: ${relatedProducts}")

            val offers = FirebaseService.getListOffers(db)
            view.showRelatedProducts(relatedProducts.toMutableList(), offers.toMutableList())
        } catch (e: Exception) {
            Log.e("Error", "getRelatedProductsByCategory: $e")
        }
    }

    private suspend fun get() {
    }

//    suspend fun fetchRelatedProductsAndOffers(idCategory: DocumentReference, idProduct: String, onResult: (List<Product>, List<Offer>) -> Unit) {
//        try {
//            val querySnapshot = FirebaseUtils.fireStore.collection("products")
//                .whereEqualTo("idCategory", idCategory)
//                .whereNotEqualTo("id", idProduct).get().await()
//            val relatedProducts = querySnapshot.toObjects(Product::class.java).map { product ->
//                product.image = FirebaseUtils.fireStorage.reference.child(product.image).downloadUrl.await().toString()
//                product
//            }
//            val offers = FirebaseUtils.getListOffers()
//            onResult(relatedProducts, offers)
//        } catch (e: Exception) {
//            Log.e("ProductDetailPresenter", "Error fetching related products or offers", e)
//        }
//    }
}