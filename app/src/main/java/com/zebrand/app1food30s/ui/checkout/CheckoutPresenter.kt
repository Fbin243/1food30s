package com.zebrand.app1food30s.ui.checkout

import com.google.firebase.firestore.DocumentReference
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
import org.w3c.dom.Document
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

    private fun placeOrder(cartId: String, idAccount: DocumentReference, address: String, note: String, shippingFee: Double, completion: (Boolean, String) -> Unit) {
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
                    reviewed = false // Defaulting to false
                )
            }

            val order = Order(
                id = UUID.randomUUID().toString(),
                idAccount = idAccount,
                items = orderItems.toMutableList(),
                totalAmount = totalPrice,
                orderStatus = "Pending", // Consider using constants or enum
                date = Date(), // Current date
                cancelReason = null, // No cancel reason at order creation
                shippingAddress = address,
                shippingFee = shippingFee,
                paymentStatus = "Unpaid", // Consider starting with "Unpaid" or similar status
                note = note
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
                            completion(true, order.id) // Directly calling completion callback without nested launch
                        } else {
//                            Log.d("Test00", "Failed to clear cart for cartId: $cartId")
                            // Clear cart failed
                            completion(false, order.id) // Directly calling completion callback without nested launch
                        }
                    }
                }
                .addOnFailureListener { e ->
//                    Log.e("Test00", "Order placement failed: ${order.id}", e)
                    // Operation failed
                    completion(false, order.id) // Directly calling completion callback without nested launch
                }
        }
    }

    fun onPlaceOrderClicked(cartId: String, idAccount: DocumentReference, address: String, note: String, shippingFee: Double) {
        placeOrder(cartId, idAccount, address, note, shippingFee) { success, orderId ->
            if (success) {
                view.navigateToOrderConfirmation(true, orderId)
            } else {
//                Log.e("Test00", "Order placement failed.")
                // You can handle the failure by invoking a different method in the view to show an error message, or pass false to navigateToOrderConfirmation if it's set up to handle failure.
                view.navigateToOrderConfirmation(false, orderId)
            }
        }
    }
}
