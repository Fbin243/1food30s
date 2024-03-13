package com.zebrand.app1food30s.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zebrand.app1food30s.data.model.CartItem
import com.zebrand.app1food30s.data.model.CheckoutItem
import com.zebrand.app1food30s.data.model.Product
import com.zebrand.app1food30s.data.service.ProductService
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicInteger

class ProductViewModel : ViewModel() {
    private val productService = ProductService()
    private val _product = MutableLiveData<Product?>()
    val product: LiveData<Product?> = _product
    private val _checkoutItems = MutableLiveData<List<CheckoutItem>>()
    val checkoutItems: LiveData<List<CheckoutItem>> = _checkoutItems

    fun loadProduct(productId: String) {
        viewModelScope.launch {
            productService.getProductById(productId) { product ->
                _product.postValue(product)
            }
        }
    }

    fun prepareCheckoutItems(cartItems: List<CartItem>) {
        val checkoutItemsList = mutableListOf<CheckoutItem>()
        val fetchCount = AtomicInteger(cartItems.size)

        cartItems.forEach { cartItem ->
            productService.getProductById(cartItem.productId) { product ->
                product?.let {
                    checkoutItemsList.add(
                        CheckoutItem(
                            productName = it.name,
                            productPrice = it.price,
                            quantity = cartItem.quantity
                        )
                    )
                }
                if (fetchCount.decrementAndGet() == 0) {
                    // All fetches are done
                    _checkoutItems.postValue(checkoutItemsList)
                }
            }
        }
    }
}
