package com.zebrand.app1food30s.data

data class CartItem(
    val productId: String = "",
    var quantity: Int = 0
)

data class Cart(
    var id: String = "",
    var accountId: String? = null,
    var items: MutableList<CartItem> = mutableListOf()
) {
    fun addItem(item: CartItem) {
        val existingItem = items.find { it.productId == item.productId }
        if (existingItem != null) {
            existingItem.quantity += item.quantity
        } else {
            items.add(item)
        }
    }

    fun removeItem(productId: String) {
        items.removeAll { it.productId == productId }
    }

    fun updateItemQuantity(productId: String, quantity: Int) {
        val existingItem = items.find { it.productId == productId }
        if (existingItem != null) {
            // Update only if the item exists in the cart
            existingItem.quantity = quantity
        }
    }

    fun clearCart() {
        items.clear()
    }

    fun getTotalQuantity(): Int {
        return items.sumOf { it.quantity }
    }

    // Example: Add a method to get total price if prices are available
    // Assuming you have a map of productId to price for simplicity
    fun getTotalPrice(productPrices: Map<String, Double>): Double {
        return items.sumOf { item ->
            val price = productPrices[item.productId] ?: 0.0
            (price * item.quantity)
        }
    }
}
