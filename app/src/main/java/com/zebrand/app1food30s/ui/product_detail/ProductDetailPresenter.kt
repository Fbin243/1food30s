package com.zebrand.app1food30s.ui.product_detail

import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import com.zebrand.app1food30s.data.Category
import com.zebrand.app1food30s.data.Offer
import com.zebrand.app1food30s.data.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductDetailPresenter(
    private val view: ProductDetailMVPView, private val fireStore: FirebaseFirestore,
    private val fireStorage: FirebaseStorage
) {
    suspend fun getProductDetail(idProduct: String) {
        try {
            view.showShimmerEffect()
            val querySnapshot =
                fireStore.collection("products").whereEqualTo("id", idProduct).get().await()
            val product = querySnapshot.toObjects(Product::class.java)[0]
            product.image =
                fireStorage.reference.child(product.image).downloadUrl.await().toString()
            // Get category
            val category = product.idCategory!!.get().await().toObject<Category>()
            val offer = product.idOffer?.get()?.await()?.toObject<Offer>()
            // Get related product
            // Finish get data
            getRelatedProductsByCategory(product.idCategory, product.id)
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

    private suspend fun getListOffers(): List<Offer> {
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