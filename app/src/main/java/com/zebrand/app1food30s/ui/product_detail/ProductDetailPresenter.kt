package com.zebrand.app1food30s.ui.product_detail

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.toObject
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.utils.FirebaseUtils
import kotlinx.coroutines.tasks.await

class ProductDetailPresenter(
    private val view: ProductDetailMVPView
) {
    suspend fun getProductDetail(idProduct: String) {
        try {
            view.showShimmerEffect()
            val product = FirebaseUtils.getOneProductByID(idProduct)!!
            // Get category
            val category = product.idCategory!!.get().await().toObject<Category>()
            val offer = product.idOffer?.get()?.await()?.toObject<Offer>()
            // Get related products
            getRelatedProductsByCategory(product.idCategory, product.id)
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

            val offers = FirebaseUtils.getListOffers()
            view.showRelatedProducts(relatedProducts, offers)
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