package com.zebrand.app1food30s.ui.wishlist

import com.zebrand.app1food30s.data.Product
import com.zebrand.app1food30s.data.WishlistItem

interface WishlistMVPView {
    // Shows the updated wishlist with the specific product added or removed
    fun showWishlistUpdated(product: Product, isAdded: Boolean)
    fun showRemoveSuccessMessage()
    fun showError(message: String)

    // Displays a list of wishlist items to the user
    fun showWishlistItems(items: List<WishlistItem>)

//    // Shows a message to the user, can be used for errors or informational messages
//    fun showMessage(message: String)
//
//    // Shows a loading indicator while the wishlist is being updated
//    fun showLoading()
//
//    // Hides the loading indicator once the update is complete
//    fun hideLoading()
}
