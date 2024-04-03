package com.zebrand.app1food30s.ui.cart

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.zebrand.app1food30s.data.AppDatabase
import com.zebrand.app1food30s.data.entity.Cart
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class CartPresenter(private val view: CartMVPView, private val userId: String, private val context: Context) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private var cartRef: DocumentReference? = null
    private val repository = CartRepository(FirebaseFirestore.getInstance(), AppDatabase.getInstance(context))

    init {
        fetchCartReference()
    }

    private fun fetchCartReference() {
        repository.getCartRef(userId) { ref ->
            cartRef = ref
            if (cartRef != null) {
                listenToCartChanges()
            } else {
                view.displayError("Cart not found")
            }
        }
    }

    fun loadCart() {
        repository.loadCart(userId, onResult = { detailedCartItems, _ ->
            if (detailedCartItems != null) {
                view.loadCart(detailedCartItems)
            } else {
                view.displayError("Failed to fetch cart details.")
            }
        }, onError = { error ->
            view.displayError(error)
        })
    }

    // TODO: totalPrice
    fun listenToCartChanges() {
        repository.listenToCartChanges(userId, onResult = { detailedCartItems, _ ->
            if (detailedCartItems != null) {
                view.displayCartItems(detailedCartItems)
            } else {
                view.displayError("Failed to fetch cart details.")
            }
        }, onError = { error ->
            view.displayError(error)
        })
    }

    fun removeFromCart(productRef: DocumentReference) {
        cartRef?.let { ref ->
            repository.removeFromCart(ref, productRef) { success ->
                if (success) {
                    // TODO
                    // Update UI accordingly
                } else {
                    view.displayError("Failed to remove item")
                }
            }
        }
    }

    fun updateCartItemQuantity(productRef: DocumentReference, newQuantity: Int) {
        cartRef?.let { ref ->
            repository.updateCartItemQuantity(ref, productRef, newQuantity) { success ->
                if (success) {
                    // TODO
                    // Update UI accordingly
                } else {
                    view.displayError("Failed to update quantity")
                }
            }
        }
    }
}