package com.zebrand.app1food30s.ui.checkout

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.DetailedCartItem
import com.zebrand.app1food30s.data.Order
import com.zebrand.app1food30s.data.OrderItem
import com.zebrand.app1food30s.ui.cart.CartRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private fun placeOrder(cartId: String, completion: (Boolean) -> Unit) {
        // Log.d("Test00", "placeOrder: Starting order placement.")
        // Assuming network/database operations might be needed
        launch {
            val orderItems = detailedCartItems.map { cartItem ->
                OrderItem(
                    productId = FirebaseFirestore.getInstance().document("products/${cartItem.productId?.id}"),
                    category = cartItem.productCategory,
                    discount = 0.0, // TODO: Update this accordingly
                    name = cartItem.productName,
                    image = cartItem.productImage,
                    price = cartItem.productPrice,
                    quantity = cartItem.quantity,
                    isReviewed = false // Defaulting to false
                )
            }

            val order = Order(
                id = UUID.randomUUID().toString(),
                //idAccount = accountId, // TODO: Link to user's account
                items = orderItems.toMutableList(),
                totalAmount = totalPrice,
                orderStatus = "Pending", // Consider using constants or enum
                date = Date(), // Current date
                cancelReason = null, // No cancel reason at order creation
                shippingAddress = "shippingAddress", // TODO: Fetch from user input
                paymentStatus = "Unpaid", // Consider starting with "Unpaid" or similar status
                note = "note" // TODO: Fetch from user input
            )

//            Log.d("Test00", "Order: Prepared to set in Firestore with ID ${order.id}")

            // Push the order to Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("orders").document(order.id)
                .set(order)
                .addOnSuccessListener {
//                    Log.d("Test00", "Order placement successful: ${order.id}")
                    cartRepository.placeOrderAndClearCart(cartId, detailedCartItems) { success ->
                        if (success) {
//                            Log.d("Test00", "Cart cleared successfully for cartId: $cartId")
                            // Clear cart was successful
                            completion(true) // Directly calling completion callback without nested launch
                        } else {
//                            Log.d("Test00", "Failed to clear cart for cartId: $cartId")
                            // Clear cart failed
                            completion(false) // Directly calling completion callback without nested launch
                        }
                    }
                }
                .addOnFailureListener { e ->
//                    Log.e("Test00", "Order placement failed: ${order.id}", e)
                    // Operation failed
                    completion(false) // Directly calling completion callback without nested launch
                }
        }
    }

//    private fun placeOrder(cartId: String, completion: (Boolean) -> Unit) {
//        Log.d("Test00", "Simulating order placement.")
//
//        // Simulating a delay to mimic network/database operations
//        launch {
//            delay(1000) // Simulate network delay
//
//            Log.d("Test00", "Simulated order placement successful.")
//
//            // Always invoking completion with true to simulate success
//            completion(true)
//        }
//    }

    fun onPlaceOrderClicked(cartId: String) {
        placeOrder(cartId) { success ->
            if (success) {
//                Log.d("Test00", "Order placement successful.")
                view.navigateToOrderConfirmation(true)
            } else {
//                Log.e("Test00", "Order placement failed.")
                // You can handle the failure by invoking a different method in the view to show an error message, or pass false to navigateToOrderConfirmation if it's set up to handle failure.
                view.navigateToOrderConfirmation(false)
            }
        }
    }

    interface SimpleCallback {
        fun onSuccess()
        fun onFailure(error: String)
    }

    fun testCallback(callback: SimpleCallback) {
        launch {
            // Simulate an asynchronous operation, like a network call with a delay
            delay(2000)

            // Randomly decide whether the operation succeeds or fails
            if (Math.random() > 0.5) {
                // Simulate success
                callback.onSuccess()
            } else {
                // Simulate failure
                callback.onFailure("Operation failed due to network error.")
            }
        }
    }
}
