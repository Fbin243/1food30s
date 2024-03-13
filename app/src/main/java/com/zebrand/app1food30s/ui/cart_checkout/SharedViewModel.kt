package com.zebrand.app1food30s.ui.cart_checkout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zebrand.app1food30s.adapter.CartItemAdapter
import com.zebrand.app1food30s.data.Product

class SharedViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<CartItemAdapter.CartItem>>(emptyList())
    val cartItems: LiveData<List<CartItemAdapter.CartItem>> = _cartItems

    init {
//        Log.d("SharedViewModel", "ViewModel instance hash code: ${hashCode()}")
    }


    fun addToCart(product: Product) {
        val cartItemsList = _cartItems.value.orEmpty()
        val existingCartItemIndex = cartItemsList.indexOfFirst { it.product.id == product.id }

        if (existingCartItemIndex >= 0) {
            // Product exists in cart, check if we can update its quantity
            val existingCartItem = cartItemsList[existingCartItemIndex]
            if (existingCartItem.quantity < existingCartItem.product.stock) {
                // Increase quantity only if it's less than stock
                val updatedCartItem = existingCartItem.copy(quantity = existingCartItem.quantity + 1)
                val updatedCartItemsList = cartItemsList.toMutableList().apply {
                    set(existingCartItemIndex, updatedCartItem)
                }
                _cartItems.value = updatedCartItemsList
//                Log.d("SharedViewModel", "Product ID: ${product.id} exists, updated quantity to ${updatedCartItem.quantity}")
            } else {
//                Log.d("SharedViewModel", "Product ID: ${product.id} reached max stock. Cannot increase quantity.")
            }
        } else {
            // Product does not exist in cart, add as new cart item with quantity set to 1 or stock if stock is 0 or 1
            val initialQuantity = if (product.stock > 0) 1 else product.stock
            val newCartItem = CartItemAdapter.CartItem(product, initialQuantity)
            _cartItems.value = cartItemsList + newCartItem
//            Log.d("SharedViewModel", "Added new product ID: ${product.id} to cart with quantity $initialQuantity")
        }
    }


    fun updateCartItem(cartItem: CartItemAdapter.CartItem) {
        val updatedItems = _cartItems.value?.map {
            if (it.product.id == cartItem.product.id) {
//                Log.d("SharedViewModel", "Updating quantity for existing product ID: ${cartItem.product.id}")
                cartItem
            } else it
        } ?: emptyList()
        _cartItems.value = updatedItems
//        Log.d("SharedViewModel", _cartItems.value.toString())
    }

    fun removeFromCart(cartItem: CartItemAdapter.CartItem) {
        val updatedItems = _cartItems.value?.filterNot { it == cartItem } ?: emptyList()
        _cartItems.value = updatedItems
    }
}