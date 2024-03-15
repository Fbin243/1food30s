package com.zebrand.app1food30s.ui.cart_checkout

import com.zebrand.app1food30s.data.model.CartItem
import com.zebrand.app1food30s.data.model.CheckoutItem
import com.zebrand.app1food30s.data.service.ProductService

interface CheckoutView {
    fun displayProducts(checkoutItemsList: List<CheckoutItem>)
    fun displayTotalPrice(totalPrice: Double)
    fun showError(error: String)
}

class CheckoutPresenter(private val view: CheckoutView) {
    private val productService = ProductService()

    fun prepareCheckoutItems(cartItems: List<CartItem>) {
        // Simulate asynchronous fetching and calculating
        val checkoutItemsList = mutableListOf<CheckoutItem>()
        var total = 0.0

        cartItems.forEach { cartItem ->
            productService.getProductById(cartItem.productId) { product ->
                if (product != null) {
                    total += product.price * cartItem.quantity
                    checkoutItemsList.add(CheckoutItem(product.name, product.price, cartItem.quantity))
                }
            }
        }

        // This is a simplified synchronous approach; you might need to handle async operations differently
        view.displayProducts(checkoutItemsList)
        view.displayTotalPrice(total)
    }
}