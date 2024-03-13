package com.zebrand.app1food30s.data.service

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.model.Product

class ProductService {
    fun getProductById(productId: String, callback: (Product?) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        // Log.d("ProductService", "Attempting to fetch product with ID: $productId")
        db.collection("products").document(productId).get().addOnSuccessListener { documentSnapshot ->
            val product = documentSnapshot.toObject(Product::class.java)
            // Log.d("ProductService", "Product found: ${product}")
            callback(product)
        }.addOnFailureListener {
            // Log.e("ProductService", "Error fetching product with ID: $productId", it)
            callback(null)
        }
    }
}