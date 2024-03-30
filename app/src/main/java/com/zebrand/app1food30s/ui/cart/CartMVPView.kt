package com.zebrand.app1food30s.ui.cart

import com.google.firebase.firestore.DocumentReference
import com.zebrand.app1food30s.data.entity.DetailedCartItem

interface CartMVPView {
    fun displayCartItems(detailedCartItems: List<DetailedCartItem>)
    fun displayError(error: String)
    fun refreshCart(productRef: DocumentReference)
}
