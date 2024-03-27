package com.zebrand.app1food30s.ui.wishlist

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.WishlistItem
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object WishlistManager {
    private val db by lazy { FirebaseFirestore.getInstance() }
    var wishlistedItems: List<WishlistItem> = emptyList()
    private lateinit var userId: String

    fun initialize(userId: String) {
        this.userId = userId
    }

    suspend fun fetchWishlistForCurrentUser(): List<WishlistItem> = suspendCoroutine { continuation ->
        val wishlistRef = db.collection("wishlists").document(userId)
        wishlistRef.get().addOnSuccessListener { documentSnapshot ->
            val productIds: List<String> = documentSnapshot.get("productIds") as? List<String> ?: listOf()
            val tasks = productIds.map { productId ->
                db.collection("products").document(productId).get()
            }

            Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
                val wishlistItems = documents.mapNotNull { document ->
                    document.toObject(WishlistItem::class.java)?.apply { this.productId = document.id }
                }
                wishlistedItems = wishlistItems
                continuation.resume(wishlistItems)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

    fun isProductWishlisted(productId: String): Boolean {
        return wishlistedItems.any { it.productId == productId }
    }

    fun setWishlist(items: List<WishlistItem>) {
        wishlistedItems = items
        notifyWishlistChanged()
    }

    fun addToWishlist(item: WishlistItem) {
        wishlistedItems = wishlistedItems + item
        notifyWishlistChanged()
    }

    fun removeFromWishlist(productId: String) {
        wishlistedItems = wishlistedItems.filterNot { it.productId == productId }
        notifyWishlistChanged()
    }

    // Optionally, if you need to observe changes to the wishlist
    private var listeners: MutableList<WishlistChangeListener> = mutableListOf()

    fun addListener(listener: WishlistChangeListener) {
        listeners.add(listener)
    }

    fun removeListener(listener: WishlistChangeListener) {
        listeners.remove(listener)
    }

    private fun notifyWishlistChanged() {
        listeners.forEach { it.onWishlistChanged(wishlistedItems) }
    }

    interface WishlistChangeListener {
        fun onWishlistChanged(wishlistedItems: List<WishlistItem>)
    }
}
