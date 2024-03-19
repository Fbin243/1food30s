package com.zebrand.app1food30s.ui.product_detail

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.toObject
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.ui.base.DataPresenter
import com.zebrand.app1food30s.utils.FirebaseUtil
import kotlinx.coroutines.tasks.await

class ProductDetailPresenter(
    private val view: ProductDetailMVPView
) : DataPresenter() {
    private val fireStore = FirebaseUtil.fireStore
    private val fireStorage = FirebaseUtil.fireStorage
    suspend fun getProductDetail(idProduct: String) {
        try {
            view.showShimmerEffect()
            val product = getOneProductByID(idProduct)!!
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
                fireStore.collection("products").whereEqualTo("idCategory", idCategory)
                    .whereNotEqualTo("id", idProduct).get()
                    .await()
            val relatedProducts = querySnapshot.toObjects(Product::class.java).map { product ->
                product.image =
                    fireStorage.reference.child(product.image).downloadUrl.await().toString()
                product
            }

            val offers = getListOffers()
            view.showRelatedProducts(relatedProducts, offers)
        } catch (e: Exception) {
            Log.i("Error", "getRelatedProductsByCategory: ${e}")
        }
    }
}