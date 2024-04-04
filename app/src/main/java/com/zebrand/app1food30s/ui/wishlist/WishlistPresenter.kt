package com.zebrand.app1food30s.ui.wishlist

import com.zebrand.app1food30s.data.entity.Product
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
                    view.refreshWishlistState(wishlistedProductIds)
                }
            } catch (e: Exception) {
                // Handle error, potentially by informing the view of the failure
            }
        }
    }

    fun toggleWishlist(product: Product) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val isNowWishlisted = repository.toggleWishlist(product.id)
//            Log.d("Test00", "toggleWishlist: $isNowWishlisted")
            withContext(Dispatchers.Main) { view.updateWishlistItemStatus(product, isNowWishlisted) }
        } catch (e: Exception) {
//            withContext(Dispatchers.Main) { view.showError(e.message ?: "An error occurred") }
        }
    }
}