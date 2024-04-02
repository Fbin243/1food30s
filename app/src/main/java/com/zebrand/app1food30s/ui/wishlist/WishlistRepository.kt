package com.zebrand.app1food30s.ui.wishlist

import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.Wishlist
import com.zebrand.app1food30s.data.WishlistItem

interface WishlistRepository {
    suspend fun toggleProductInWishlist(productId: String): Boolean
    suspend fun isProductInWishlist(productId: String): Boolean
//    suspend fun getWishlistItems(): List<WishlistItem>
    suspend fun removeFromWishlist(productId: String): Boolean
    suspend fun addToWishlist(productId: String): Boolean
}