package com.zebrand.app1food30s.ui.product_detail

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import kotlinx.coroutines.tasks.await

class ProductDetailPresenter(
    private val view: ProductDetailMVPView, private val fireStore: FirebaseFirestore,
    private val fireStorage: FirebaseStorage
) {
    suspend fun getProductDetail(idProduct: String) {
        try {
            view.showShimmerEffect()
            val querySnapshot = fireStore.collection("products").whereEqualTo("id", idProduct).get().await()
            val product = querySnapshot.toObjects(Product::class.java)[0]
            product.image = fireStorage.reference.child(product.image).downloadUrl.await().toString()
            // Get category
            val category = product.idCategory!!.get().await().toObject<Category>()
            val offer = product.idOffer?.get()?.await()?.toObject<Offer>()
            // Finish get data
            view.hideShimmerEffect()
            view.showProductDetail(product, category!!, offer)
        } catch (e: Exception) {
            Log.i("Error", "getProductDetail: ${e}")
        }
    }
}