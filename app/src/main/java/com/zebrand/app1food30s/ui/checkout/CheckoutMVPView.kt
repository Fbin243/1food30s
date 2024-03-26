package com.zebrand.app1food30s.ui.checkout

import com.zebrand.app1food30s.data.DetailedCartItem

interface CheckoutMVPView {
    fun displayCartItems(detailedCartItems: List<DetailedCartItem>, totalPrice: Double)
    fun displayError(error: String)
    fun navigateToOrderConfirmation(showOrderConfirmation: Boolean)
}
