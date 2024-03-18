package com.zebrand.app1food30s.ui.checkout

import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.ui.cart.CartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckoutPresenter(private val view: CheckoutInterface) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val cartRepository = CartRepository(FirebaseFirestore.getInstance())

    fun loadCartData(cartId: String) {
        cartRepository.fetchProductDetailsForCartItems(cartId) { detailedCartItems, totalPrice ->
            if (detailedCartItems != null) {
                view.displayCartItems(detailedCartItems, totalPrice)
            } else {
                view.displayError("Failed to fetch cart details")
            }
        }
    }
}
