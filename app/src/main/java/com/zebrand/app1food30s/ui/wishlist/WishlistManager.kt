package com.zebrand.app1food30s.ui.wishlist

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.entity.Wishlist
import com.zebrand.app1food30s.data.entity.WishlistItem
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBWishlistRef
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object WishlistManager {
    private val db by lazy { FirebaseFirestore.getInstance() }
    var wishlistedItems: MutableList<WishlistItem> = mutableListOf() // Changed to mutable list for easier item addition/removal
    private lateinit var userId: String

    private fun isProductWishlisted(productId: String): Boolean =
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

