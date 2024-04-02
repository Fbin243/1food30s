package com.zebrand.app1food30s.data

data class Wishlist(
    var id: String = "",
    val items: List<WishlistItem> = listOf()
)

data class WishlistItem(
    var productId: String,
    val name: String,
    val price: Double,
    val image: String, // URL or resource identifier
)