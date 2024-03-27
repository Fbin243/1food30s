package com.zebrand.app1food30s.ui.wishlist

import com.zebrand.app1food30s.data.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WishlistPresenter(
    private val view: WishlistMVPView,
    private val repository: WishlistRepository // Now, the repository is initialized with userId beforehand
) {
    fun loadWishlistItems() = CoroutineScope(Dispatchers.IO).launch {
//        val items = repository.getWishlistItems() // 'userId' is managed within the repository
//        withContext(Dispatchers.Main) {
//            view.showWishlistItems(items)
//        }
    }

    fun addToWishlist(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        // Attempt to add product to wishlist
        val added = repository.addToWishlist(productId)
        withContext(Dispatchers.Main) {
            if (added) {
//                view.showAddSuccessMessage()
            } else {
                view.showError("Could not add item to wishlist.")
            }
        }
    }

    fun removeFromWishlist(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        // Attempt to remove product from wishlist
        val removed = repository.removeFromWishlist(productId)
        withContext(Dispatchers.Main) {
            if (removed) {
                view.showRemoveSuccessMessage()
            } else {
                view.showError("Could not remove item from wishlist.")
            }
        }
    }

    fun toggleWishlist(product: Product) = CoroutineScope(Dispatchers.IO).launch {
        val result = repository.toggleProductInWishlist(product.id)
        withContext(Dispatchers.Main) {
            view.showWishlistUpdated(product, result)
        }
    }

    // You might not need this method if your repository handles checking wishlist status directly
    private fun checkProductInWishlist(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        // Check if product is in wishlist
        val isInWishlist = repository.isProductInWishlist(productId)
        withContext(Dispatchers.Main) {
            // You could use this check to update UI or as part of other logic
        }
    }
}