package com.zebrand.app1food30s.ui.cart_checkout

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.zebrand.app1food30s.data.Product

class SharedViewModel : ViewModel() {
    private val _cartItems = MutableLiveData<List<Product>>(emptyList())
    val cartItems: LiveData<List<Product>> = _cartItems

    fun addToCart(product: Product) {
        // Log.d("addToCart", "addToCart()")
        val currentItems = _cartItems.value ?: emptyList()
        _cartItems.value = currentItems + product
        // Log.d("addToCart", _cartItems.value.toString())
    }
}