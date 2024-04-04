package com.zebrand.app1food30s.ui.wishlist

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

    fun addToWishlist(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        // This would now update the local list and potentially sync with the backend
        val item = getWishlistItemByProductId(productId)
        if (item != null) {
            WishlistManager.addToWishlist(item)
        }
        withContext(Dispatchers.Main) {
            // Assuming we have a way to verify addition was successful
            // view.showAddSuccessMessage() // Adjust as needed based on your application logic
        }
    }

    fun removeFromWishlist(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        // This would now update the local list and potentially sync with the backend
        WishlistManager.removeFromWishlist(productId)
        withContext(Dispatchers.Main) {
            // Assuming we have a way to verify removal was successful
            view.showRemoveSuccessMessage()
        }
    }

    fun toggleWishlist(product: Product) = CoroutineScope(Dispatchers.IO).launch {
        val item = getWishlistItemByProductId(product.id)
        val result = item?.let { WishlistManager.toggleProductInWishlist(it) }
        withContext(Dispatchers.Main) {
            if (result != null) {
                view.showWishlistUpdated(product, result)
            }
        }
    }
}