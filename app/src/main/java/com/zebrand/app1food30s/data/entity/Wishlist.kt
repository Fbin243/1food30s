package com.zebrand.app1food30s.data.entity

import com.google.firebase.firestore.DocumentReference

data class Wishlist(
    var id: String = "",
    var userId: DocumentReference? = null,
    val items: List<WishlistItem> = listOf()
)

data class WishlistItem(
    var productId: String,
    val name: String,
    val price: Double,
    val image: String,
)