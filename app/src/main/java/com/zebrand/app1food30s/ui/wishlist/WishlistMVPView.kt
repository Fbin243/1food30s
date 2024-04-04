package com.zebrand.app1food30s.ui.wishlist

import com.zebrand.app1food30s.data.entity.Product

interface WishlistMVPView {
    fun refreshWishlistState(wishlistedProductIds: Set<String>)

    fun updateWishlistItemStatus(product: Product, isAdded: Boolean)
//    fun showRemoveSuccessMessage()
//    fun showError(message: String)
}
