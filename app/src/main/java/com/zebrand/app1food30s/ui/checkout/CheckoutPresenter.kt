package com.zebrand.app1food30s.ui.checkout

import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.DetailedCartItem
import com.zebrand.app1food30s.data.Order
import com.zebrand.app1food30s.data.OrderItem
import com.zebrand.app1food30s.ui.cart.CartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class CheckoutPresenter(private val view: CheckoutMVPView, private val cartRepository: CartRepository) : CoroutineScope by CoroutineScope(Dispatchers.Main) {
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

    fun placeOrder(cartId: String, completion: (Boolean) -> Unit) {
        // Assuming network/database operations might be needed
        launch {
            val orderItems = detailedCartItems.map { cartItem ->
                // Assuming DocumentReference conversion is handled elsewhere
                OrderItem(
                    product = FirebaseFirestore.getInstance().document("products/${cartItem.productId}"),
                    quantity = cartItem.quantity,
                    isReviewed = false // Defaulting to false
                )
            }

            val order = Order(
                id = UUID.randomUUID().toString(),
                idAccount = null, // Set this according to your user management system
                items = orderItems.toMutableList(),
                totalAmount = totalPrice,
                orderStatus = "Pending",
                date = Date(), // Current date
                cancelReason = null
            )

            // Push the order to Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("orders").document(order.id)
                .set(order)
                .addOnSuccessListener {
                    // Operation successful
                    cartRepository.placeOrderAndClearCart(cartId, detailedCartItems) { success ->
                        if (success) {
                            // Clear cart was successful
                            launch(Dispatchers.Main) {
                                completion(true)
                            }
                        } else {
                            // Clear cart failed
                            launch(Dispatchers.Main) {
                                completion(false)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Operation failed
                    launch(Dispatchers.Main) {
                        completion(false)
                    }
                }
        }
    }
}
