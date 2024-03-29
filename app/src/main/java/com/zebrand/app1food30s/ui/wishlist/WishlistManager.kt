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
    var wishlistedItems: MutableList<WishlistItem> = mutableListOf() // Changed to mutable list for easier item addition/removal
    private lateinit var userId: String

    fun initialize(userId: String) {
        this.userId = userId
    }

    suspend fun fetchWishlistForCurrentUser(): List<WishlistItem> = suspendCoroutine { continuation ->
        val wishlistRef = db.collection("wishlists").document(userId)
        wishlistRef.get().addOnSuccessListener { documentSnapshot ->
            val productIds = documentSnapshot.get("productIds") as? List<String> ?: listOf()
            val tasks = productIds.map { productId ->
                db.collection("products").document(productId).get()
            }

            Tasks.whenAllSuccess<DocumentSnapshot>(tasks).addOnSuccessListener { documents ->
                val wishlistItems = documents.mapNotNull { document ->
                    WishlistItem(
                        productId = document.id,
                        name = document.getString("name") ?: "",
                        price = document.getDouble("price") ?: 0.0,
                        image = document.getString("image") ?: ""
                    )
                }
                this.wishlistedItems = wishlistItems.toMutableList()
                continuation.resume(wishlistItems)
            }.addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
        }.addOnFailureListener { exception ->
            continuation.resumeWithException(exception)
        }
    }

    fun isProductWishlisted(productId: String): Boolean =
        wishlistedItems.any { it.productId == productId }

    fun setWishlist(items: List<WishlistItem>) {
        wishlistedItems = items.toMutableList()
        notifyWishlistChanged()
    }

    fun addToWishlist(item: WishlistItem) {
        if (!isProductWishlisted(item.productId)) {
            wishlistedItems.add(item)
            notifyWishlistChanged()
        }
    }

    fun removeFromWishlist(productId: String) {
        wishlistedItems.removeAll { it.productId == productId }
        notifyWishlistChanged()
    }

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

    suspend fun toggleProductInWishlist(item: WishlistItem): Boolean {
        val isWishlisted = isProductWishlisted(item.productId)
        if (isWishlisted) {
            removeFromWishlist(item.productId)
        } else {
            addToWishlist(item)
        }
        return !isWishlisted
    }

    interface WishlistChangeListener {
        fun onWishlistChanged(wishlistedItems: List<WishlistItem>)
    }
}

