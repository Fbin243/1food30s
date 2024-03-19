package com.zebrand.app1food30s.ui.checkout

import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.DetailedCartItem
import com.zebrand.app1food30s.ui.cart.CartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CheckoutPresenter(private val view: CheckoutInterface, private val cartRepository: CartRepository) : CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private var detailedCartItems: List<DetailedCartItem> = emptyList()
    private var totalPrice: Double = 0.0

    fun loadCartData(cartId: String) {
        launch {
            cartRepository.fetchProductDetailsForCartItems(cartId) { detailedCartItemsResult, totalPriceResult ->
                if (detailedCartItemsResult != null) {
                    detailedCartItems = detailedCartItemsResult
                    totalPrice = totalPriceResult
                    view.displayCartItems(detailedCartItems, totalPrice)
                } else {
                    view.displayError("Failed to fetch cart details")
                }
            }
        }
    }

    fun placeOrder(cartId: String, completion: () -> Unit) {
        // Since this involves network/database operations, ensure this runs on an appropriate background thread if needed
        cartRepository.placeOrderAndClearCart(cartId, detailedCartItems) { success ->
            if (success) {
                // Switch back to the Main thread for UI operations
                launch(Dispatchers.Main) {
                    completion()
                }
            } else {
                launch(Dispatchers.Main) {
                }
            }
        }
    }

}
