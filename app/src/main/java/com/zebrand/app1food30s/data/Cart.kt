package com.zebrand.app1food30s.data

import com.google.firebase.firestore.DocumentReference

data class CartItem(
    val productId: DocumentReference? = null,
    var quantity: Int = 0,
)

data class Cart(
    var id: String = "",
    var accountId: DocumentReference? = null,
    var items: MutableList<CartItem> = mutableListOf()
)

data class DetailedCartItem(
    val productId: DocumentReference? = null,
    val productCategory: String,
    val productName: String,
    val productPrice: Double,
    val productImage: String,
    val productStock: Int,
    var quantity: Int,
)
