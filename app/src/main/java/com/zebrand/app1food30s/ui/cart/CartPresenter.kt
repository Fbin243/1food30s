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

class CartPresenter(private val view: CartMVPView, private val userId: String, context: Context) : CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private var cartRef: DocumentReference? = null
    private val repository = CartRepository(FirebaseFirestore.getInstance())

    init {
        setUpCart()
    }

    private fun setUpCart() {
        repository.getCartRef(userId) { ref ->
            cartRef = ref
            loadCart()
        }
    }


    private fun loadCart() {
        cartRef?.let { ref ->
            repository.loadCart(ref, onResult = { detailedCartItems ->
                if (detailedCartItems != null) {
                    if (detailedCartItems.isNotEmpty()) {
                        view.loadCart(detailedCartItems)
                    } else {
                        // TODO: this is called when checkout clears cart
                        view.displayEmptyCart()
                    }
                }
            }, onError = { error ->
                view.displayError(error)
            })
        } ?: run {
            view.displayError("Cart reference not found.")
        }
    }


    fun removeFromCart(productRef: DocumentReference) {
        cartRef?.let { ref ->
            repository.removeFromCart(ref, productRef) { isSuccess, isEmpty ->
                if (isSuccess) {
                    if (isEmpty) {
                        // If the operation was successful and the cart is now empty, display the empty cart UI
                        CoroutineScope(Dispatchers.Main).launch {
                            view.displayEmptyCart()
                        }
                    } else {
                        // If the operation was successful but the cart is not empty, perform other UI updates as necessary
                    }
                } else {
                    // If the operation failed, display an error
                    CoroutineScope(Dispatchers.Main).launch {
                        view.displayError("Failed to remove item")
                    }
                }
            }
        }
    }

//    fun removeFromCart(cartRef: DocumentReference, productRef: DocumentReference) {
//        repository.removeFromCart(cartRef, productRef) { isSuccess, isEmpty ->
//            if (isSuccess) {
//                if (isEmpty) {
//                    // If the operation was successful and the cart is now empty, display the empty cart UI
//                    CoroutineScope(Dispatchers.Main).launch {
//                        view.displayEmptyCart()
//                    }
//                } else {
//                    // If the operation was successful but the cart is not empty, perform other UI updates as necessary
//                }
//            } else {
//                // If the operation failed, display an error
//                CoroutineScope(Dispatchers.Main).launch {
//                    view.displayError("Failed to remove item")
//                }
//            }
//        }
//    }

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

//    private fun listenToCartChanges() {
//        repository.listenToCartChanges(userId, onResult = { detailedCartItems, _ ->
//            if (detailedCartItems != null) {
//                view.displayCartItems(detailedCartItems)
//            } else {
//                view.displayError("Failed to fetch cart details.")
//            }
//        }, onError = { error ->
//            view.displayError(error)
//        })
//    }
}