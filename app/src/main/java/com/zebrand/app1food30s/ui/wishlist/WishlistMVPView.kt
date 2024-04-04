package com.zebrand.app1food30s.ui.wishlist

import com.zebrand.app1food30s.data.entity.Product
import com.zebrand.app1food30s.data.entity.WishlistItem

interface WishlistMVPView {
    fun updateWishlist(wishlistedProductIds: Set<String>)

    fun showWishlistUpdated(product: Product, isAdded: Boolean)
    fun showRemoveSuccessMessage()
    fun showError(message: String)
}
