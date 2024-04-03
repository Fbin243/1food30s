package com.zebrand.app1food30s.ui.checkout

import com.zebrand.app1food30s.data.entity.CartItem

interface CheckoutMVPView {
    fun displayCartItems(cartItems: List<CartItem>, totalPrice: Double)
    fun displayError(error: String)
    fun navigateToOrderConfirmation(showOrderConfirmation: Boolean)
}
