package com.zebrand.app1food30s.ui.wishlist

import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.utils.FirebaseUtils.getWishlistItemByProductId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistPresenter(
    private val view: WishlistMVPView,
) {
    fun loadWishlistItems() = CoroutineScope(Dispatchers.IO).launch {
        val items = WishlistManager.fetchWishlistForCurrentUser()
        withContext(Dispatchers.Main) {
            view.showWishlistItems(items)
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