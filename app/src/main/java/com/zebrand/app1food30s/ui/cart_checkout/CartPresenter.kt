package com.zebrand.app1food30s.ui.cart_checkout

import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.model.Cart
import com.zebrand.app1food30s.data.model.CartItem
import com.zebrand.app1food30s.data.service.ProductService

interface CartView {
    fun displayCartItems(cartItems: List<CartItem>)
    fun displayTotalPrice(totalPrice: Double)
    fun displayError(error: String)
}

class CartPresenter(private val view: CartView) {
    private val db = FirebaseFirestore.getInstance()
    private val cartId = "mdXn8lvirHaAogStOY1K"
    private val productService = ProductService()

    fun listenToCartChanges() {
        db.collection("carts").document(cartId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    view.displayError(e.message ?: "Unknown error")
                    return@addSnapshotListener
                }

                val cart = snapshot?.toObject(Cart::class.java)
                cart?.items?.let { items ->
                    view.displayCartItems(items)
                } ?: view.displayError("Cart not found")
            }
    }


    private fun calculateTotalPrice(items: List<CartItem>) {
        var totalPrice = 0.0
        items.forEach { cartItem ->
            productService.getProductById(cartItem.productId) { product ->
                if (product != null) {
                    totalPrice += product.price * cartItem.quantity
                }
                // Since fetching product details is asynchronous, you might need to adjust how and when you call displayTotalPrice.
                view.displayTotalPrice(totalPrice)
            }
        }
    }
}