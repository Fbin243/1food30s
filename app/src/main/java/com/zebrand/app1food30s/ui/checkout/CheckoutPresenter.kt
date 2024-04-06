package com.zebrand.app1food30s.ui.checkout

import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.entity.CartItem
import com.zebrand.app1food30s.data.entity.Order
import com.zebrand.app1food30s.data.entity.OrderItem
import com.zebrand.app1food30s.ui.cart.CartRepository
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBCartRef
import com.zebrand.app1food30s.utils.FireStoreUtils.mDBOrderRef
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date
import java.util.UUID

class CheckoutPresenter(private val view: CheckoutMVPView, private val cartRepository: CartRepository) : CoroutineScope by CoroutineScope(Dispatchers.Main) {
    private var cartItems: List<CartItem> = emptyList()
    private var totalPrice: Double = 0.0

    fun loadCartData(cartId: String) {
        launch {
            // TODO
            val cartRef = mDBCartRef.document(cartId)
            cartRepository.fetchProductDetailsForCartItems(cartRef) { detailedCartItemsResult ->
                if (detailedCartItemsResult != null) {
                    cartItems = detailedCartItemsResult
                    totalPrice = detailedCartItemsResult.sumOf { it.productPrice * it.quantity }
                    view.displayCartItems(cartItems, totalPrice)
                } else {
                    view.displayError("Failed to fetch cart details")
                }
            }
        }
    }

    private fun placeOrder(cartId: String, completion: (Boolean) -> Unit) {
        // Log.d("Test00", "placeOrder: Starting order placement.")
        launch {
            val orderItems = cartItems.map { cartItem ->
                OrderItem(
                    // TODO
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

            mDBOrderRef.document(order.id)
                .set(order)
                .addOnSuccessListener {
//                    Log.d("Test00", "Order placement successful: ${order.id}")
                    cartRepository.placeOrderAndClearCart(cartId, cartItems) { success ->
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
                view.navigateToOrderConfirmation(true)
            } else {
//                Log.e("Test00", "Order placement failed.")
                // You can handle the failure by invoking a different method in the view to show an error message, or pass false to navigateToOrderConfirmation if it's set up to handle failure.
                view.navigateToOrderConfirmation(false)
            }
        }
    }
}
