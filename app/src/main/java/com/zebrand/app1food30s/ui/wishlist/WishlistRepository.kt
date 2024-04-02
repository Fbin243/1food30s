package com.zebrand.app1food30s.ui.wishlist

interface WishlistRepository {
    suspend fun toggleProductInWishlist(productId: String): Boolean
    suspend fun isProductInWishlist(productId: String): Boolean
//    suspend fun getWishlistItems(): List<WishlistItem>
    suspend fun removeFromWishlist(productId: String): Boolean
    suspend fun addToWishlist(productId: String): Boolean
}