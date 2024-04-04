package com.zebrand.app1food30s.ui.wishlist

import android.util.Log
import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.FirebaseUtils.getWishlistItemByProductId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistPresenter(
    private val view: WishlistMVPView,
    private val repository: WishlistRepository
) {

    init {
        repository.initialize()
    }

    fun fetchAndUpdateWishlistState() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val wishlistItems = repository.fetchWishlistForCurrentUser()
                val wishlistedProductIds = wishlistItems.map { it.productId }.toSet()
                withContext(Dispatchers.Main) {
                    view.updateWishlist(wishlistedProductIds)
                }
            } catch (e: Exception) {
                // Handle error, potentially by informing the view of the failure
            }
        }
    }

//    fun addToWishlist(productId: String) = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val success = repository.addToWishlist(productId)
//            if (success) withContext(Dispatchers.Main) {
////                view.showAddSuccessMessage()
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) { view.showError(e.message ?: "An error occurred") }
//        }
//    }
//
//    fun removeFromWishlist(productId: String) = CoroutineScope(Dispatchers.IO).launch {
//        try {
//            val success = repository.removeFromWishlist(productId)
//            if (success) withContext(Dispatchers.Main) { view.showRemoveSuccessMessage() }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) { view.showError(e.message ?: "An error occurred") }
//        }
//    }

    fun toggleWishlist(product: Product) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val isNowWishlisted = repository.toggleWishlist(product.id)
//            Log.d("Test00", "toggleWishlist: $isNowWishlisted")
            withContext(Dispatchers.Main) { view.showWishlistUpdated(product, isNowWishlisted) }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { view.showError(e.message ?: "An error occurred") }
        }
    }
}