package com.zebrand.app1food30s.ui.cart

import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.data.entity.CartItem

interface CartMVPView {
    fun loadCart(cartItems: List<CartItem>)
    fun displayCartItems(cartItems: List<CartItem>)
    fun displayError(error: String)
    fun refreshCart(productRef: DocumentReference)
    fun displayEmptyCart()
}
