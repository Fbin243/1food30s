package com.zebrand.app1food30s.ui.product_detail

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.toObject
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Category
import com.zebrand.app1food30s.data.entity.Offer
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.FirebaseService
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class ProductDetailPresenter(
    private val view: ProductDetailMVPView, private val db: AppDatabase
) {
    suspend fun getProductDetail(idProduct: String) {
        try {
            view.showShimmerEffect()
            val product = FirebaseService.getOneProductByID(db, idProduct)!!
            // Get category
            val category = product.idCategory!!.get().await().toObject<Category>()
            val offer = product.idOffer?.get()?.await()?.toObject<Offer>()
            val idCategory = product.idCategory!!
            // Get related products
            getRelatedProductsByCategory(idCategory, product.id)
            // Finish get data
            view.showProductDetail(product, category!!, offer)
            view.hideShimmerEffect()
        } catch (e: Exception) {
            Log.i("Error", "getProductDetail: ${e}")
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
            Log.i("Error", "getRelatedProductsByCategory: ${e}")
        }
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